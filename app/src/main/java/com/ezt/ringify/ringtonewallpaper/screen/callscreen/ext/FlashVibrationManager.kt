package com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

class FlashVibrationManager(private val context: Context) {

    private var flashlightHandler: Handler? = null
    private var vibrator: Vibrator? = null
    private var cameraIdForFlash: String? = null

    fun startFlashAndVibration(
        isFlashEnabled: Boolean,
        flashType: FlashType,
        isVibrationEnabled: Boolean,
        vibrationType: VibrationType
    ) {
        stopFlashAndVibration()
        println("startFlashAndVibration: isVibrationEnabled: $isFlashEnabled and isVibrationEnabled: $isVibrationEnabled")
        println("startFlashAndVibration: flashType: $flashType and vibrationType: $vibrationType")

        if (isVibrationEnabled && vibrationType != VibrationType.NONE) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = when (vibrationType) {
                VibrationType.SHORT -> VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )

                VibrationType.LONG -> VibrationEffect.createOneShot(
                    600,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )

                VibrationType.DOUBLE -> VibrationEffect.createWaveform(
                    longArrayOf(
                        0,
                        200,
                        100,
                        200
                    ), 0
                )

                VibrationType.HEARTBEAT -> VibrationEffect.createWaveform(
                    longArrayOf(
                        0,
                        100,
                        200,
                        300
                    ), 0
                )

                else -> null
            }
            vibrationEffect?.let {
                vibrator?.vibrate(it)
            }
        }

        if (isFlashEnabled && flashType != FlashType.NONE) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraIdForFlash = cameraManager.cameraIdList.firstOrNull {
                    cameraManager.getCameraCharacteristics(it)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                cameraIdForFlash?.let { camId ->
                    flashlightHandler = Handler(Looper.getMainLooper())
                    val runnable = object : Runnable {
                        var flashOn = false
                        var sosState = 0
                        val sosPattern = longArrayOf(
                            0, 200, 200, 200, 200, 200, 200, 600, 600, 200, 600, 200, 600, 200
                        )

                        override fun run() {
                            try {
                                when (flashType) {
                                    FlashType.ONCE -> {
                                        cameraManager.setTorchMode(camId, true)
                                        flashlightHandler?.postDelayed({
                                            cameraManager.setTorchMode(camId, false)
                                        }, 1000)
                                    }

                                    FlashType.FAST_BLINK -> {
                                        cameraManager.setTorchMode(camId, flashOn)
                                        flashOn = !flashOn
                                        flashlightHandler?.postDelayed(this, 300)
                                    }

                                    FlashType.SLOW_BLINK -> {
                                        cameraManager.setTorchMode(camId, flashOn)
                                        flashOn = !flashOn
                                        flashlightHandler?.postDelayed(this, 1000)
                                    }

                                    FlashType.SOS -> {
                                        val on = (sosState % 2 == 0)
                                        cameraManager.setTorchMode(camId, on)
                                        sosState = (sosState + 1) % sosPattern.size
                                        flashlightHandler?.postDelayed(this, sosPattern[sosState])
                                    }

                                    else -> {
                                        cameraManager.setTorchMode(camId, false)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FlashVibrationManager", "Flashlight error: ${e.message}")
                            }
                        }
                    }
                    flashlightHandler?.post(runnable)
                }
            } catch (e: Exception) {
                Log.e("FlashVibrationManager", "Error initializing flashlight: ${e.message}")
            }
        }
    }

    fun stopFlashAndVibration() {
        vibrator?.cancel()
        vibrator = null

        flashlightHandler?.removeCallbacksAndMessages(null)
        flashlightHandler = null

        if (cameraIdForFlash != null) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraManager.setTorchMode(cameraIdForFlash!!, false)
            } catch (e: Exception) {
                Log.e("FlashVibrationManager", "Error stopping flashlight: ${e.message}")
            }
        }
    }
}
