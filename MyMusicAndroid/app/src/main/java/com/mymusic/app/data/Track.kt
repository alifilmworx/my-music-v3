package com.mymusic.app.data

import android.net.Uri

/**
 * Mirrors the track shape from the web app (title/artist/album/genre/dateAdded/artUri),
 * but every field here comes straight from Android's MediaStore instead of parsed ID3
 * tags off a manually picked file - the OS already extracted all of this when the file
 * was indexed, which is why native scanning is both more complete and less code.
 */
data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String?,
    val folder: String,
    val dateAdded: Long,       // seconds since epoch, straight from MediaStore
    val duration: Long,        // ms
    val contentUri: Uri,       // playable URI, e.g. content://media/external/audio/media/123
    val albumArtUri: Uri?      // content://media/external/audio/albumart/{albumId}, may not resolve on newer Android - see MediaStoreScanner
)
