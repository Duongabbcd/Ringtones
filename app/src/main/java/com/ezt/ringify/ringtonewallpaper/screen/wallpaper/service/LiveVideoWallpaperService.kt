package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service

import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

class VideoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        Log.d("VideoWallpaperService", "Engine created")
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var exoPlayer: ExoPlayer? = null
        private lateinit var surfaceHolder: SurfaceHolder

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            Log.d("VideoEngine", "Surface created")
            startVideo()
        }

        @OptIn(UnstableApi::class)
        private fun startVideo() {
            val prefs = getSharedPreferences("video_wallpaper", MODE_PRIVATE)
            val videoUrl = prefs.getString("video_url", null)
            Log.d("VideoWallpaperService", "Retrieved wallpaper URL: $videoUrl")

            if (videoUrl.isNullOrEmpty()) return

            // Create a DataSource.Factory for HTTP requests
            val dataSourceFactory = DefaultHttpDataSource.Factory()

            // Create a ProgressiveMediaSource.Factory using the DataSource.Factory
            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

            // Create a MediaSource for the video
            val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(videoUrl))

            // Initialize ExoPlayer
            exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                // Set the media source
                setMediaSource(mediaSource)

                // Set repeat mode to loop the video indefinitely
                repeatMode = Player.REPEAT_MODE_ONE

                // Set the video surface holder for rendering
                setVideoSurfaceHolder(surfaceHolder)

                // Set the video scaling mode to fit the screen while maintaining aspect ratio
                videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT

                // Prepare and start playback
                prepare()
                playWhenReady = true

                // Add a listener to monitor playback state changes
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            Log.d("VideoEngine", "Playback started")
                        }
                    }
                })
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d("VideoEngine", "onVisibilityChanged = $visible")
            if (visible) {
                exoPlayer?.play()
            } else {
                exoPlayer?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopVideo()
        }

        private fun stopVideo() {
            exoPlayer?.release()
            exoPlayer = null
        }

        override fun onDestroy() {
            super.onDestroy()
            stopVideo()
        }
    }

}
