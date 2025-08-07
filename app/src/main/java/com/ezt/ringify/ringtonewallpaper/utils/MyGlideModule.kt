package com.ezt.ringify.ringtonewallpaper.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class MyGlideModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val okHttpClient = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(30, TimeUnit.SECONDS)  // Increase connect timeout
            .readTimeout(30, TimeUnit.SECONDS)     // Increase read timeout
            .writeTimeout(30, TimeUnit.SECONDS)    // Increase write timeout
            .build()

        // Replace Glide's default loader with OkHttp loader using your client
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okHttpClient)
        )
    }
}