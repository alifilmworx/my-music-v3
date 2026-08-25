package com.mymusic.app.playback

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Native equivalent of the web app's Media Session API wiring (setupMediaSessionHandlersOnce,
 * syncMediaSessionPosition, etc). Media3 handles almost all of that automatically just by
 * attaching a session to the player - the lock screen and notification controls, play/pause/
 * next/prev, and the seek position all sync for free, with far less code than the web version
 * needed to hand-wire the same behavior through the browser's more limited Media Session API.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build().apply {
            setHandleAudioBecomingNoisy(true) // pause automatically on headphone unplug
            repeatMode = Player.REPEAT_MODE_OFF
        }
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
