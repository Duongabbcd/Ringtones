package com.example.ringtone.screen.wallpaper.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.net.HttpURLConnection
import java.net.URL

class SlideshowWallpaperService : WallpaperService() {

    companion object {
        var imageUrls: List<String> = emptyList()
    }

    override fun onCreateEngine(): Engine {
        return SlideshowEngine()
    }

    inner class SlideshowEngine : Engine() {
        private var currentImageIndex = 0
        private val handler = Handler(Looper.getMainLooper())
        private var running = true
        private val slideshowInterval = 5000L // 5 seconds

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            startSlideshow()
        }

        private fun startSlideshow() {
            handler.post(object : Runnable {
                override fun run() {
                    if (!running || imageUrls.isEmpty()) return

                    val url = imageUrls[currentImageIndex]

                    Glide.with(applicationContext)
                        .asBitmap()
                        .load(url)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                drawBitmap(resource)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                //do nothing
                            }

                        })

                    currentImageIndex = (currentImageIndex + 1) % imageUrls.size
                    handler.postDelayed(this, slideshowInterval)
                }
            })
        }

        private fun drawBitmap(bitmap: Bitmap) {
            val canvas = surfaceHolder?.lockCanvas() ?: return
            canvas.drawColor(Color.BLACK)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, canvas.width, canvas.height, true)
            canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
            surfaceHolder.unlockCanvasAndPost(canvas)
        }

        override fun onDestroy() {
            running = false
            handler.removeCallbacksAndMessages(null)
            super.onDestroy()
        }
    }
}

