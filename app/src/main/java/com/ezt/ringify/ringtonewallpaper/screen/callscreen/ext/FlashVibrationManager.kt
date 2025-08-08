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
        vibrationType: VibrationType,
        onComplete: (() -> Unit)? = null
    ) {
        stopFlashAndVibration()
        println("startFlashAndVibration: isVibrationEnabled: $isFlashEnabled and isVibrationEnabled: $isVibrationEnabled")
        println("startFlashAndVibration: flashType: $flashType and vibrationType: $vibrationType")

        if (isVibrationEnabled) {
            playVibration(vibrationType)
        }

        if (isFlashEnabled) {
            playFlashType(flashType)
        }
    }

    fun playFlashType(flashType: FlashType) {
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
                                FlashType.DEFAULT -> {
                                    val wavePattern =
                                        listOf(100L, 200L, 100L, 400L, 100L, 200L) // ON-OFF pattern
                                    val on = sosState % 2 == 0
                                    cameraManager.setTorchMode(camId, on)
                                    flashlightHandler?.postDelayed(this, wavePattern[sosState])
                                    sosState = (sosState + 1) % wavePattern.size
                                }

                                FlashType.SOS -> {
                                    val on = sosState % 2 == 0
                                    cameraManager.setTorchMode(camId, on)
                                    flashlightHandler?.postDelayed(this, sosPattern[sosState])
                                    sosState = (sosState + 1) % sosPattern.size
                                }

                                FlashType.FAST_BLINK -> {
                                    cameraManager.setTorchMode(camId, flashOn)
                                    flashOn = !flashOn
                                    flashlightHandler?.postDelayed(this, 300L)
                                }

                                FlashType.SLOW_BLINK -> {
                                    cameraManager.setTorchMode(camId, flashOn)
                                    flashOn = !flashOn
                                    flashlightHandler?.postDelayed(this, 1000L)
                                }

                                FlashType.STROBE -> {
                                    cameraManager.setTorchMode(camId, flashOn)
                                    flashOn = !flashOn
                                    flashlightHandler?.postDelayed(this, 100L)
                                }

                                FlashType.PULSE -> {
                                    if (flashOn) {
                                        cameraManager.setTorchMode(camId, true)
                                        flashlightHandler?.postDelayed({
                                            cameraManager.setTorchMode(camId, false)
                                            flashOn = false
                                            flashlightHandler?.postDelayed(this, 700L)
                                        }, 400L)
                                    } else {
                                        flashOn = true
                                        flashlightHandler?.post(this)
                                    }
                                }

                                FlashType.TRIPLE -> {
                                    val triplePattern = listOf(
                                        150L,
                                        100L,
                                        150L,
                                        100L,
                                        150L,
                                        600L
                                    ) // ON-OFF-ON-OFF-ON-PAUSE
                                    val on = sosState % 2 == 0
                                    cameraManager.setTorchMode(camId, on)
                                    flashlightHandler?.postDelayed(this, triplePattern[sosState])
                                    sosState = (sosState + 1) % triplePattern.size
                                }


                                else -> {
                                    cameraManager.setTorchMode(camId, false)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("FlashManager", "Error toggling flash: ${e.message}")
                        }
                    }

                }
                flashlightHandler?.post(runnable)
            }
        } catch (e: Exception) {
            Log.e("FlashVibrationManager", "Error initializing flashlight: ${e.message}")
        }
    }

    fun playVibration(vibrationType: VibrationType) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = when (vibrationType) {
            VibrationType.DEFAULT -> {
                // Flash and vibrate in sync every 500ms
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 100, 100, 100, 100),
                    0
                )
            }

            VibrationType.SINGLE_CLICK -> {
                // Flash and vibrate in sync every 500ms
                VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 150),     // wait 0ms, vibrate 50ms, pause 150ms
                    intArrayOf(0, 180, 0),       // no vibration, max amplitude, no vibration
                    1                            // repeat from index 1 (start of click)
                )
            }

            VibrationType.SINGLE_HEAVY_CLICK -> {
                // Flash and vibrate in sync every 500ms
                VibrationEffect.createWaveform(
                    longArrayOf(0, 80, 150),     // wait 0ms, vibrate 50ms, pause 150ms
                    intArrayOf(0, 255, 0),       // no vibration, max amplitude, no vibration
                    1                            // repeat from index 1 (start of click)
                )
            }

            VibrationType.DOUBLE_CLICK -> {
                // Flash and vibrate in sync every 500ms
                VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 100, 50, 300),
                    intArrayOf(0, 100, 0, 100, 0),
                    1
                )
            }

            VibrationType.DOUBLE_HEAVY_CLICK -> {
                // Flash and vibrate in sync every 500ms
                VibrationEffect.createWaveform(
                    longArrayOf(0, 80, 100, 80, 300),
                    intArrayOf(0, 255, 0, 255, 0),
                    1
                )
            }


            VibrationType.RISING_ALERT -> {
                // Increasing vibration pattern: weak to strong
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 100, 200, 100, 300),
                    intArrayOf(0, 255, 0, 255, 0, 255),
                    1
                )
            }

            VibrationType.RHYTHMIC_TAP -> {
                // Tap-tap-tap feel
                VibrationEffect.createWaveform(
                    longArrayOf(0, 200, 100, 80, 300, 150),
                    0
                )
            }

            VibrationType.DOUBLE -> VibrationEffect.createWaveform(
                longArrayOf(0, 200, 100, 200),
                intArrayOf(0, 255, 0, 255),
                1
            )

            VibrationType.HEARTBEAT -> VibrationEffect.createWaveform(
                longArrayOf(0, 100, 200, 300),
                0
            )

            else -> null
        }
        vibrationEffect?.let {
            vibrator?.vibrate(it)
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
