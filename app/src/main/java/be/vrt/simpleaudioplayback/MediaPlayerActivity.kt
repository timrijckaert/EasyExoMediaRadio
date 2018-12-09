package be.vrt.simpleaudioplayback

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.atomic.AtomicBoolean

class MediaPlayerActivity : AppCompatActivity() {

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            println(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

        }
    }

    private fun checkStateIsPlaying(state: PlaybackStateCompat?) = state?.let { it.state == PlaybackStateCompat.STATE_PLAYING } ?: false

    private lateinit var mediaController: MediaControllerCompat
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaBrowser.sessionToken.also { token ->
                MediaControllerCompat(
                        this@MediaPlayerActivity,
                        token
                )

                mediaController = MediaControllerCompat(this@MediaPlayerActivity, mediaBrowser.sessionToken)
                MediaControllerCompat.setMediaController(this@MediaPlayerActivity, mediaController)
            }

            mediaController.registerCallback(mediaControllerCallback)
        }

        override fun onConnectionSuspended() {
            //The service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            //The service has refused our connection
        }
    }

    private val mediaBrowser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
                this,
                ComponentName(this, MediaPlaybackService::class.java),
                connectionCallback,
                null
        )
    }

    private val mediaPlaybackController
        get() = mediaController.transportControls

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        studioBrussel.setOnClickListener {
            mediaPlaybackController.prepareFromMediaId(
                    "https://live-vrt.akamaized.net/groupc/live/f404f0f3-3917-40fd-80b6-a152761072fe/live.isml/.m3u8",
                    Bundle().apply {
                        putString(MediaPlaybackService.EXTRA_TITLE, "Studio Brussel")
                        putString(MediaPlaybackService.EXTRA_DESC, "Life is your mother")
                    }
            )
        }
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(mediaControllerCallback)
        mediaBrowser.disconnect()
    }
}
