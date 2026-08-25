package com.mymusic.app.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File

/**
 * This is the whole point of going native: one query against MediaStore returns every
 * audio file on the device that Android has indexed - no folder to choose, no directory
 * walking, nothing manual. It runs after READ_MEDIA_AUDIO (or READ_EXTERNAL_STORAGE on
 * older versions) has been granted, and again automatically whenever the app is reopened,
 * so newly downloaded songs just show up.
 */
class MediaStoreScanner(private val context: Context) {

    fun scan(): List<Track> {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // full file path - used to derive the folder name,
            // same "top-level folder under the root" logic as the web version's Folders tab
            MediaStore.Audio.Media.IS_MUSIC
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC" // newest first, same as the web app

        context.contentResolver.query(collection, projection, selection, null, sortOrder)
            ?.use { cursor: Cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val genreCol = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE) // may be -1 on some versions
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val albumId = cursor.getLong(albumIdCol)
                    val path = cursor.getString(dataCol) ?: ""
                    val folder = deriveFolderName(path)

                    tracks.add(
                        Track(
                            id = id,
                            title = cursor.getString(titleCol) ?: "Unknown title",
                            artist = cursor.getString(artistCol) ?: "Unknown artist",
                            album = cursor.getString(albumCol) ?: "Unknown album",
                            genre = if (genreCol >= 0) cursor.getString(genreCol) else null,
                            folder = folder,
                            dateAdded = cursor.getLong(dateCol),
                            duration = cursor.getLong(durationCol),
                            contentUri = contentUri,
                            albumArtUri = albumArtUriFor(albumId)
                        )
                    )
                }
            }
        return tracks
    }

    /** Same "top-level folder under the music root" idea as the web app's Folders tab -
     *  Downloads, Telegram, etc. show up as their own groups. */
    private fun deriveFolderName(fullPath: String): String {
        if (fullPath.isEmpty()) return "Unknown folder"
        val parent = File(fullPath).parentFile ?: return "Unknown folder"
        // walk up to the folder just under the common public music roots so nested
        // subfolders still group under their real top-level parent (e.g. Music/Telegram/x.mp3)
        val knownRoots = listOf("Music", "Download", "Telegram", "WhatsApp", "storage")
        var node: File? = parent
        var lastGoodName = parent.name
        while (node != null && node.parentFile != null) {
            if (node.name in knownRoots) break
            lastGoodName = node.name
            node = node.parentFile
        }
        return lastGoodName.ifBlank { "Unknown folder" }
    }

    /** Pre-Android 10 album art still resolves via this legacy content URI; on newer
     *  versions this may fail silently per-track, which is fine - the UI falls back to
     *  the plain note icon the same way the web app does when ID3 had no embedded art. */
    private fun albumArtUriFor(albumId: Long): Uri? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
        } else {
            ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
        }
    }
}
