package com.iptvplayer.app.ui.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.common.util.UnstableApi
import com.iptvplayer.app.data.model.Channel
import com.iptvplayer.app.databinding.ActivityPlayerBinding
import com.iptvplayer.app.utils.Constants

@UnstableApi
class PlayerActivity : FragmentActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var currentChannel: Channel? = null
    private var controlsVisible = true
    private val hideControlsRunnable = Runnable { hideControls() }

    companion object {
        fun start(context: Context, channel: Channel) {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                putExtra(Constants.EXTRA_CHANNEL, channel)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentChannel = intent.getSerializableExtra(Constants.EXTRA_CHANNEL) as? Channel
        setupPlayer()
        setupControls()
        currentChannel?.let { playChannel(it) }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> showBuffering(true)
                        Player.STATE_READY -> {
                            showBuffering(false)
                            hideControlsDelayed()
                        }
                        Player.STATE_ENDED -> finish()
                        Player.STATE_IDLE -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    showBuffering(false)
                    Toast.makeText(
                        this@PlayerActivity,
                        "Playback error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        binding.playerView.player = player
        binding.playerView.controllerAutoShow = false
        binding.playerView.useController = false
    }

    private fun setupControls() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnFavorite.setOnClickListener {
            // Toggle favorite handled via intent back to main
        }
        binding.btnInfo.setOnClickListener { toggleControls() }

        binding.root.setOnClickListener { toggleControls() }
    }

    private fun playChannel(channel: Channel) {
        binding.channelName.text = channel.name
        binding.channelGroup.text = channel.group

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("IPTVPlayer/1.0 (Android TV)")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)

        val mediaItem = MediaItem.fromUri(channel.url)

        player?.apply {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }

        showBuffering(true)
    }

    private fun showBuffering(show: Boolean) {
        binding.bufferingIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun toggleControls() {
        if (controlsVisible) {
            hideControls()
        } else {
            showControls()
        }
    }

    private fun showControls() {
        controlsVisible = true
        binding.controlsOverlay.visibility = View.VISIBLE
        binding.controlsOverlay.animate().alpha(1f).setDuration(200).start()
        hideControlsDelayed()
    }

    private fun hideControls() {
        controlsVisible = false
        binding.controlsOverlay.animate().alpha(0f).setDuration(200)
            .withEndAction { binding.controlsOverlay.visibility = View.GONE }.start()
    }

    private fun hideControlsDelayed() {
        binding.root.removeCallbacks(hideControlsRunnable)
        binding.root.postDelayed(hideControlsRunnable, 3_000)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                toggleControls()
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (controlsVisible) {
                    hideControls()
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                player?.let {
                    if (it.isPlaying) it.pause() else it.play()
                }
                true
            }
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                showControls()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
}
