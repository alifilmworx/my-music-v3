package com.mymusic.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room equivalent of the web app's `liked` Set persisted to localStorage */
@Entity(tableName = "liked_tracks")
data class LikedTrackEntity(@PrimaryKey val trackId: Long)

@Entity(tableName = "disliked_tracks")
data class DislikedTrackEntity(@PrimaryKey val trackId: Long)

/** A custom playlist - mirrors customPlaylists from the web app */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/** Join table: which tracks belong to which playlist. A track can be in many playlists,
 *  and removing it from one never touches the others - same rule as the web version. */
@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: Long
)
