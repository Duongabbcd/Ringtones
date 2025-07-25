package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import java.io.File


@UnstableApi
object CacheUtil {
    private var simpleCache: SimpleCache? = null

    fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "exo_cache")
            val cacheSize: Long = 100 * 1024 * 1024 // 100MB
            val evictor = LeastRecentlyUsedCacheEvictor(cacheSize)
            val databaseProvider = StandaloneDatabaseProvider(context)

            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return simpleCache!!
    }

    fun release() {
        simpleCache?.release()
        simpleCache = null
    }
}

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null

    @OptIn(UnstableApi::class)
    fun getPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            // Optional: Custom LoadControl
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    2000,  // minBufferMs
                    5000,  // maxBufferMs
                    1000,  // bufferForPlaybackMs
                    2000   // bufferForPlaybackAfterRebufferMs
                )
                .build()

            exoPlayer = ExoPlayer.Builder(context.applicationContext)
                .setLoadControl(loadControl)
                .build().apply {
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ONE
                }

            Log.d("PlayerManager", "Created new ExoPlayer instance")
        }

        return exoPlayer!!
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        Log.d("PlayerManager", "Released ExoPlayer")
    }
}