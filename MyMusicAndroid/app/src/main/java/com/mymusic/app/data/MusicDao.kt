package com.mymusic.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // --- Liked / Disliked ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun like(track: LikedTrackEntity)

    @Query("DELETE FROM liked_tracks WHERE trackId = :trackId")
    suspend fun unlike(trackId: Long)

    @Query("SELECT trackId FROM liked_tracks")
    fun likedIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun dislike(track: DislikedTrackEntity)

    @Query("DELETE FROM disliked_tracks WHERE trackId = :trackId")
    suspend fun undislike(trackId: Long)

    @Query("SELECT trackId FROM disliked_tracks")
    fun dislikedIds(): Flow<List<Long>>

    // --- Playlists ---
    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlists ORDER BY id ASC")
    fun allPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(ref: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun removeTrackFromAllPlaylists(trackId: Long)

    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId = :playlistId")
    fun trackIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Query("SELECT playlistId FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun playlistIdsContaining(trackId: Long): List<Long>
}
