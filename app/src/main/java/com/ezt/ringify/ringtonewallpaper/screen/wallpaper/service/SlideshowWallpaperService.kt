package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service

import android.graphics.Bitmap
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

class SlideshowWallpaperService : WallpaperService() {

    companion object {
        var imageUrls: List<String> = emptyList()
        var setupSlideShowInterval: Long? = null
    }

    override fun onCreateEngine(): Engine {
        return SlideshowEngine()
    }

    inner class SlideshowEngine : Engine() {
        private var currentImageIndex = 0
        private val handler = Handler(Looper.getMainLooper())
        private var running = true
        private var slideshowInterval = 5000L // 5 seconds

        private var currentBitmap: Bitmap? = null
        private var previousBitmap: Bitmap? = null
        private val transitionDuration = 800L // 800ms fade
        private val frameDelay = 16L // ~60 FPS

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            startSlideshow()
        }

        private fun startSlideshow() {
            slideshowInterval = setupSlideShowInterval ?: 5000L
            handler.post(object : Runnable {
                override fun run() {
                    if (!running || imageUrls.isEmpty()) return

                    val url = imageUrls[currentImageIndex]

                    // Keep current as previous for transition
                    previousBitmap = currentBitmap

                    Glide.with(applicationContext)
                        .asBitmap()
                        .load(url)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                currentBitmap = Bitmap.createScaledBitmap(
                                    resource,
                                    surfaceHolder.surfaceFrame.width(),
                                    surfaceHolder.surfaceFrame.height(),
                                    true
                                )
                                startFadeTransition()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })

                    currentImageIndex = (currentImageIndex + 1) % imageUrls.size
                    handler.postDelayed(this, slideshowInterval)
                }
            })
        }

        private fun startFadeTransition() {
            val startTime = System.currentTimeMillis()

            fun animateFrame() {
                val elapsed = System.currentTimeMillis() - startTime
                val progress = (elapsed / transitionDuration.toFloat()).coerceAtMost(1f)
                drawCrossroadFrame(progress)

                if (progress < 1f) {
                    handler.postDelayed(::animateFrame, frameDelay)
                }
            }

            animateFrame()
        }

        private fun drawCrossroadFrame(alphaProgress: Float) {
            val canvas = surfaceHolder?.lockCanvas() ?: return

            try {
                canvas.drawColor(Color.BLACK)

                val paint = Paint()

                previousBitmap?.let {
                    paint.alpha = ((1f - alphaProgress) * 255).toInt()
                    canvas.drawBitmap(it, 0f, 0f, paint)
                }

                currentBitmap?.let {
                    paint.alpha = (alphaProgress * 255).toInt()
                    canvas.drawBitmap(it, 0f, 0f, paint)
                }
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        override fun onDestroy() {
            running = false
            handler.removeCallbacksAndMessages(null)
            super.onDestroy()
        }
    }
}

