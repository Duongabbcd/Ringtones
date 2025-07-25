package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import java.io.File

@UnstableApi
object ExoPlayerCache {
    private var cache: SimpleCache? = null

    fun getCache(context: Context): Cache {
        if (cache == null) {
            val cacheDir = File(context.cacheDir, "media_cache")
            cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100 MB
            )
        }
        return cache!!
    }

    fun buildCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val upstreamFactory = OkHttpDataSource.Factory(OkHttpClient())
        return CacheDataSource.Factory()
            .setCache(getCache(context))
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
}
