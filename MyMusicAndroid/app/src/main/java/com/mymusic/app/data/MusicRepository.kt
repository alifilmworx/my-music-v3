package com.mymusic.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MusicRepository(context: Context) {
    private val scanner = MediaStoreScanner(context)
    private val dao = MusicDatabase.get(context).musicDao()

    /** Re-scans MediaStore fresh every call - cheap enough to run on every app open/resume,
     *  which is exactly how a newly downloaded song "just appears" with no manual step. */
    suspend fun scanLibrary(): List<Track> = withContext(Dispatchers.IO) { scanner.scan() }

    fun likedIds(): Flow<List<Long>> = dao.likedIds()
    fun dislikedIds(): Flow<List<Long>> = dao.dislikedIds()
    suspend fun like(trackId: Long) = dao.like(LikedTrackEntity(trackId))
    suspend fun unlike(trackId: Long) = dao.unlike(trackId)
    suspend fun dislike(trackId: Long) = dao.dislike(DislikedTrackEntity(trackId))
    suspend fun undislike(trackId: Long) = dao.undislike(trackId)

    fun allPlaylists(): Flow<List<PlaylistEntity>> = dao.allPlaylists()
    suspend fun createPlaylist(name: String): Long = dao.createPlaylist(PlaylistEntity(name = name))
    suspend fun deletePlaylist(id: Long) = dao.deletePlaylist(id)
    fun trackIdsForPlaylist(id: Long): Flow<List<Long>> = dao.trackIdsForPlaylist(id)
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) =
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) =
        dao.removeTrackFromPlaylist(playlistId, trackId)
    suspend fun playlistIdsContaining(trackId: Long): List<Long> = dao.playlistIdsContaining(trackId)
    suspend fun removeTrackFromAllPlaylists(trackId: Long) = dao.removeTrackFromAllPlaylists(trackId)
}
