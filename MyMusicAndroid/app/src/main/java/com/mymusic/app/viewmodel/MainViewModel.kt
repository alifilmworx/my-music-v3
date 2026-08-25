package com.mymusic.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.mymusic.app.data.MusicRepository
import com.mymusic.app.data.PlaylistEntity
import com.mymusic.app.data.Track
import com.mymusic.app.playback.PlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PlayerUiState(
    val library: List<Track> = emptyList(),
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val liked: Set<Long> = emptySet(),
    val disliked: Set<Long> = emptySet(),
    val playlists: List<PlaylistEntity> = emptyList(),
    val shuffleOn: Boolean = false,
    val repeatOn: Boolean = false,
    val hasPermission: Boolean = false
) {
    val currentTrack: Track? get() = queue.getOrNull(queueIndex)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = MusicRepository(application)
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state

    init {
        viewModelScope.launch {
            combine(repo.likedIds(), repo.dislikedIds(), repo.allPlaylists()) { liked, disliked, playlists ->
                Triple(liked.toSet(), disliked.toSet(), playlists)
            }.collect { (liked, disliked, playlists) ->
                _state.value = _state.value.copy(liked = liked, disliked = disliked, playlists = playlists)
            }
        }
        connectToPlaybackService()
    }

    private fun connectToPlaybackService() {
        val context = getApplication<android.app.Application>()
        val token = SessionToken(context, android.content.ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.value = _state.value.copy(isPlaying = isPlaying)
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val newIndex = _state.value.queue.indexOfFirst { it.id.toString() == mediaItem?.mediaId }
                    if (newIndex >= 0) _state.value = _state.value.copy(queueIndex = newIndex)
                }
            })
        }, MoreExecutors.directExecutor())
    }

    /** Re-scans MediaStore - called on first launch (after permission is granted) and every
     *  time the app returns to the foreground, so newly downloaded songs appear with zero
     *  manual steps, same intent as the web app's "no refresh needed" fixes. */
    fun refreshLibrary() {
        viewModelScope.launch {
            val tracks = repo.scanLibrary()
            _state.value = _state.value.copy(library = tracks, hasPermission = true)
        }
    }

    fun onPermissionDenied() {
        _state.value = _state.value.copy(hasPermission = false)
    }

    fun playQueue(tracks: List<Track>, startIndex: Int) {
        val c = controller ?: return
        c.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
        c.prepare()
        c.play()
        _state.value = _state.value.copy(queue = tracks, queueIndex = startIndex)
    }

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }

    fun toggleShuffle() {
        val on = !_state.value.shuffleOn
        controller?.shuffleModeEnabled = on
        _state.value = _state.value.copy(shuffleOn = on)
    }

    fun toggleRepeat() {
        val on = !_state.value.repeatOn
        controller?.repeatMode = if (on) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _state.value = _state.value.copy(repeatOn = on)
    }

    fun toggleLike(trackId: Long) = viewModelScope.launch {
        if (_state.value.liked.contains(trackId)) repo.unlike(trackId) else {
            repo.like(trackId); repo.undislike(trackId)
        }
    }

    fun toggleDislike(trackId: Long) = viewModelScope.launch {
        if (_state.value.disliked.contains(trackId)) repo.undislike(trackId) else {
            repo.dislike(trackId); repo.unlike(trackId)
            next()
        }
    }

    fun createPlaylist(name: String) = viewModelScope.launch { repo.createPlaylist(name) }
    fun deletePlaylist(id: Long) = viewModelScope.launch { repo.deletePlaylist(id) }
    fun addToPlaylist(playlistId: Long, trackId: Long) = viewModelScope.launch { repo.addTrackToPlaylist(playlistId, trackId) }
    fun removeFromPlaylist(playlistId: Long, trackId: Long) = viewModelScope.launch { repo.removeTrackFromPlaylist(playlistId, trackId) }

    private val _playlistTrackIds = MutableStateFlow<Map<Long, List<Long>>>(emptyMap())
    val playlistTrackIds: StateFlow<Map<Long, List<Long>>> = _playlistTrackIds

    /** Keeps every playlist's track-id list live, the same way customPlaylists[].trackIds
     *  worked in the web version - used both to render a playlist's contents and to know
     *  which playlists a given song already belongs to (for the Add/Remove to list menu). */
    fun watchPlaylist(playlistId: Long) {
        viewModelScope.launch {
            repo.trackIdsForPlaylist(playlistId).collect { ids ->
                _playlistTrackIds.value = _playlistTrackIds.value.toMutableMap().apply { put(playlistId, ids) }
            }
        }
    }

    suspend fun playlistIdsContaining(trackId: Long): List<Long> =
        _state.value.playlists.filter { pl -> playlistTrackIds.value[pl.id]?.contains(trackId) == true }.map { it.id }

    private fun Track.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(albumArtUri)
                    .build()
            )
            .build()
}
