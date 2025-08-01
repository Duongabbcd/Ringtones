package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallscreenAlertBinding
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashVibrationManager
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.VibrationType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.type.AllTypeAlertActivity


class CallScreenAlertActivity :
    BaseActivity<ActivityCallscreenAlertBinding>(ActivityCallscreenAlertBinding::inflate) {

    private var isFlashEnabled = false
    private var isVibrationEnabled = false
    private lateinit var flashVibrationManager: FlashVibrationManager

    private var isPlayed = false

    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flashVibrationManager = FlashVibrationManager(this)
        prefs = this.getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        binding.apply {

            backBtn.setOnClickListener {
                finish()
            }

            if (isPlayed) {
                player.setImageResource(R.drawable.icon_pause_alert)
            } else {
                player.setImageResource(R.drawable.icon_play_alert)
            }

            flashTitle.text =
                if (flashTypeValue.isEmpty()) resources.getString(R.string.default_title) else flashTypeValue
            vibrationTitle.text =
                if (vibrationValue.isEmpty()) resources.getString(R.string.default_title) else vibrationValue

            flashSwitcher.setOnClickListener {
                isFlashEnabled = !isFlashEnabled
                displayByCondition(isFlashEnabled, flashSwitcher)
            }

            vibrationSwitcher.setOnClickListener {
                isVibrationEnabled = !isVibrationEnabled
                displayByCondition(isVibrationEnabled, vibrationSwitcher)
            }


            flashType.setOnClickListener {
                startActivity(
                    Intent(
                        this@CallScreenAlertActivity,
                        AllTypeAlertActivity::class.java
                    ).apply {
                        putExtra("alertType", "Flash")
                    })
            }

            vibrationType.setOnClickListener {
                startActivity(
                    Intent(
                        this@CallScreenAlertActivity,
                        AllTypeAlertActivity::class.java
                    ).apply {
                        putExtra("alertType", "Vibration")
                    })
            }
        }
    }

    private fun displayByCondition(check: Boolean, view: ImageView) {
        val resourceImage = if (check) R.drawable.switch_enabled else R.drawable.switch_disabled
        view.setImageResource(resourceImage)
    }

    companion object {
        var vibrationValue = ""
        var flashTypeValue = ""
    }

    override fun onResume() {
        super.onResume()
        binding.apply {
            isFlashEnabled = prefs.getBoolean("FLASH_ENABLE", false)
            isVibrationEnabled = prefs.getBoolean("VIBRATION_ENABLE", false)

            val savedVibration =
                prefs.getString("VIBRATION_TYPE", "None")
                    ?: "None"
            val savedFlash =
                prefs.getString("FLASH_TYPE", "None")
                    ?: "None"

            val displayText1 =
                if (savedVibration == "None") resources.getString(R.string.default_title) else savedVibration
            val displayText2 =
                if (savedVibration == "None") resources.getString(R.string.default_title) else savedFlash

            vibrationTitle.text = if (vibrationValue == "") displayText1 else vibrationValue
            flashTitle.text = if (flashTypeValue == "") displayText2 else flashTypeValue
            displayByCondition(isFlashEnabled, flashSwitcher)
            displayByCondition(isFlashEnabled, vibrationSwitcher)


            applyBtn.setOnClickListener {
                prefs.edit()
                    .putBoolean("FLASH_ENABLE", isFlashEnabled)
                    .putBoolean("VIBRATION_ENABLE", isVibrationEnabled)
                    .putString("VIBRATION_TYPE", vibrationValue)
                    .putString("FLASH_TYPE", flashTypeValue)
                    .apply()
            }


            player.setOnClickListener {
                isPlayed = !isPlayed
                println("flashTypeValue: $flashTypeValue and $vibrationValue")
                println("flashTypeValue: $isFlashEnabled and $isVibrationEnabled")
                val flash = FlashType.fromLabel(flashTypeValue) ?: FlashType.NONE
                val vibration = VibrationType.fromLabel(vibrationValue) ?: VibrationType.NONE
                if (!isFlashEnabled && !isVibrationEnabled) {
                    Toast.makeText(
                        this@CallScreenAlertActivity,
                        "Did you enable all functions before testing?",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (flash == FlashType.NONE && vibration == VibrationType.NONE) {
                    Toast.makeText(
                        this@CallScreenAlertActivity, "Did you select value before testing?",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (isPlayed) {
                    player.setImageResource(R.drawable.icon_pause_alert)
                    flashVibrationManager.startFlashAndVibration(true, flash, true, vibration)

                } else {
                    player.setImageResource(R.drawable.icon_play_alert)
                    flashVibrationManager.stopFlashAndVibration()

                }
            }

        }
    }
}