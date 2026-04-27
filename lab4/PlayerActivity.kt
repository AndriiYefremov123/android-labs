package com.example.lab4

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)

        val uriString = intent.getStringExtra("uri") ?: ""
        val isVideo = intent.getBooleanExtra("isVideo", false)

        if (uriString.isEmpty()) {
            Toast.makeText(this, "Помилка: файл не знайдено", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initPlayer(uriString, isVideo)
    }

    private fun initPlayer(uriString: String, isVideo: Boolean) {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player


        val mediaItem = MediaItem.fromUri(uriString)
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Toast.makeText(
                    this@PlayerActivity,
                    "Помилка відтворення: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
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
}