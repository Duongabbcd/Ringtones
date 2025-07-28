package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service

import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil

class VideoWallpaperService : WallpaperService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("VideoWallpaperService", "onCreate")
    }
    override fun onCreateEngine(): Engine {
        Log.d("VideoWallpaperService", "onCreateEngine")
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var exoPlayer: ExoPlayer? = null
        private lateinit var surfaceHolder: SurfaceHolder
        private var currentUrl: String? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Log.d("VideoEngine", "onCreate")
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            surfaceHolder = holder
            Log.d("VideoEngine", "onSurfaceCreated")

            stopVideo()
            startVideo()
        }

        @OptIn(UnstableApi::class)


        private fun startVideo() {
            val prefs = getSharedPreferences("video_wallpaper", MODE_PRIVATE)
            val videoUrl = prefs.getString("video_url", null)
            Log.d("VideoWallpaperService", "Retrieved wallpaper URL: $videoUrl")

            if (videoUrl.isNullOrEmpty()) return
            currentUrl = videoUrl
            val dataSourceFactory = DefaultHttpDataSource.Factory()

            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

            val mediaSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(videoUrl))

            exoPlayer = ExoPlayer.Builder(applicationContext).build().apply {
                setMediaSource(mediaSource, true)

                repeatMode = Player.REPEAT_MODE_ONE

                setVideoSurfaceHolder(surfaceHolder)
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

                prepare()
                playWhenReady = true

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
            Log.d("VideoEngine", "onVisibilityChanged: $visible")

            if (visible) {
                val prefs = getSharedPreferences("video_wallpaper", MODE_PRIVATE)
                val newUrl = prefs.getString("video_url", null)
                if (exoPlayer == null || currentUrl != newUrl) {
                    stopVideo()
                    startVideo()
                } else {
                    exoPlayer?.play()
                }
            } else {
                exoPlayer?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d("VideoEngine", "onSurfaceDestroyed")
            stopVideo()
        }

        private fun stopVideo() {
            exoPlayer?.apply {
                stop()
                release()
            }
            exoPlayer = null
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d("VideoEngine", "onDestroy")
            stopVideo()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d("VideoEngine", "onSurfaceChanged: $width x $height")

            holder?.let {
                surfaceHolder = it
                exoPlayer?.setVideoSurfaceHolder(surfaceHolder)
            }
        }
    }
}
