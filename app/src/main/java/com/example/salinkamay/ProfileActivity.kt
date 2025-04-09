package com.example.salinkamay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Ensure scroll starts at the top
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)
        scrollView.post { scrollView.scrollTo(0, 0) }


        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_profile // highlight current tab
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // Prevents going back to previous screens
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                    true
                }
                R.id.nav_camera -> {
                    startActivity(Intent(this, TranslateActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Already on Profile, so do nothing
                    true
                }
                else -> false
            }
        }

        setupVideos()
    }


    private fun setupVideos() {
        // Unique IDs for each video
        val videoIds = listOf(
            R.id.video_view_1,
            R.id.video_view_2,
            R.id.video_view_3,
            R.id.video_view_4,
            R.id.video_view_5
        )

        val playIds = listOf(
            R.id.btn_play_1,
            R.id.btn_play_2,
            R.id.btn_play_3,
            R.id.btn_play_4,
            R.id.btn_play_5
        )

        val pauseIds = listOf(
            R.id.btn_pause_1,
            R.id.btn_pause_2,
            R.id.btn_pause_3,
            R.id.btn_pause_4,
            R.id.btn_pause_5
        )

        val rewindIds = listOf(
            R.id.btn_rewind_1,
            R.id.btn_rewind_2,
            R.id.btn_rewind_3,
            R.id.btn_rewind_4,
            R.id.btn_rewind_5
        )

        val forwardIds = listOf(
            R.id.btn_forward_1,
            R.id.btn_forward_2,
            R.id.btn_forward_3,
            R.id.btn_forward_4,
            R.id.btn_forward_5
        )

        val videoFiles = listOf("vid_1", "vid_2", "vid_3", "vid_4", "vid_5")

        for (i in videoIds.indices) {
            val videoView = findViewById<VideoView>(videoIds[i])
            val playButton = findViewById<ImageButton>(playIds[i])
            val pauseButton = findViewById<ImageButton>(pauseIds[i])
            val rewindButton = findViewById<ImageButton>(rewindIds[i])
            val forwardButton = findViewById<ImageButton>(forwardIds[i])

            try {
                val videoUri = Uri.parse("android.resource://$packageName/raw/${videoFiles[i]}")
                videoView.setVideoURI(videoUri)

                playButton.setOnClickListener {
                    if (!videoView.isPlaying) {
                        videoView.start()
                        playButton.animate().alpha(0f).setDuration(300).start() // Hide button
                    }
                }

                pauseButton.setOnClickListener {
                    if (videoView.isPlaying) {
                        videoView.pause()
                        playButton.animate().alpha(1f).setDuration(300).start() // Show button
                    }
                }

                rewindButton.setOnClickListener {
                    videoView.seekTo((videoView.currentPosition - 10000).coerceAtLeast(0))
                }

                forwardButton.setOnClickListener {
                    videoView.seekTo((videoView.currentPosition + 10000).coerceAtMost(videoView.duration))
                }

                videoView.setOnCompletionListener {
                    playButton.animate().alpha(1f).setDuration(300).start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading video: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
