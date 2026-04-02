package com.example.q2_mediaplayerapp

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.MediaController

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenFile: Button
    private lateinit var btnOpenUrl: Button
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnRestart: Button
    private lateinit var etVideoUrl: EditText
    private lateinit var videoView: VideoView

    private var mediaPlayer: MediaPlayer? = null
    private var selectedAudioUri: Uri? = null
    private var currentVideoUri: Uri? = null

    // Keeps track of what type of media is currently loaded
    private var currentMediaType: String = ""

    // File picker for selecting audio from device storage
    private val audioPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedAudioUri = uri
                currentMediaType = "AUDIO"

                // Release old MediaPlayer before creating a new one
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(this, uri)

                Toast.makeText(this, "Audio file selected", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenFile = findViewById(R.id.btnOpenFile)
        btnOpenUrl = findViewById(R.id.btnOpenUrl)
        btnPlay = findViewById(R.id.btnPlay)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)
        btnRestart = findViewById(R.id.btnRestart)
        etVideoUrl = findViewById(R.id.etVideoUrl)
        videoView = findViewById(R.id.videoView)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        btnOpenFile.setOnClickListener {
            // Open file picker for audio files only
            audioPickerLauncher.launch("audio/*")
        }

        btnOpenUrl.setOnClickListener {
            openVideoFromUrl()
        }

        btnPlay.setOnClickListener {
            playMedia()
        }

        btnPause.setOnClickListener {
            pauseMedia()
        }

        btnStop.setOnClickListener {
            stopMedia()
        }

        btnRestart.setOnClickListener {
            restartMedia()
        }
    }

    private fun openVideoFromUrl() {
        val url = etVideoUrl.text.toString().trim()

        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a video URL", Toast.LENGTH_SHORT).show()
            return
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(this, "Enter a valid URL starting with http or https", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            currentVideoUri = Uri.parse(url)
            currentMediaType = "VIDEO"

            // Stop audio if video is selected
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }

            videoView.setVideoURI(currentVideoUri)
            videoView.requestFocus()

            videoView.setOnPreparedListener { mp ->
                mp.isLooping = false
                Toast.makeText(this, "Video loaded successfully", Toast.LENGTH_SHORT).show()
            }

            videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(this, "Cannot play this video", Toast.LENGTH_SHORT).show()
                true
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playMedia() {
        when (currentMediaType) {
            "AUDIO" -> {
                if (mediaPlayer == null && selectedAudioUri != null) {
                    mediaPlayer = MediaPlayer.create(this, selectedAudioUri)
                }

                if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.start()
                } else if (mediaPlayer == null) {
                    Toast.makeText(this, "Please select an audio file first", Toast.LENGTH_SHORT).show()
                }
            }

            "VIDEO" -> {
                if (currentVideoUri != null) {
                    videoView.start()
                } else {
                    Toast.makeText(this, "Please open a video URL first", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                Toast.makeText(this, "Please open a file or URL first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pauseMedia() {
        when (currentMediaType) {
            "AUDIO" -> {
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                }
            }

            "VIDEO" -> {
                if (videoView.isPlaying) {
                    videoView.pause()
                }
            }

            else -> {
                Toast.makeText(this, "No media is loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopMedia() {
        when (currentMediaType) {
            "AUDIO" -> {
                if (mediaPlayer != null) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()
                    mediaPlayer = null

                    // Prepare the same audio again so it can be played later
                    if (selectedAudioUri != null) {
                        mediaPlayer = MediaPlayer.create(this, selectedAudioUri)
                    }
                }
            }

            "VIDEO" -> {
                if (currentVideoUri != null) {
                    videoView.pause()
                    videoView.seekTo(0)
                }
            }

            else -> {
                Toast.makeText(this, "No media is loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restartMedia() {
        when (currentMediaType) {
            "AUDIO" -> {
                if (mediaPlayer != null) {
                    mediaPlayer!!.seekTo(0)
                    mediaPlayer!!.start()
                }
            }

            "VIDEO" -> {
                if (currentVideoUri != null) {
                    videoView.seekTo(0)
                    videoView.start()
                }
            }

            else -> {
                Toast.makeText(this, "No media is loaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release MediaPlayer resources when activity is destroyed
        mediaPlayer?.release()
        mediaPlayer = null
    }
}