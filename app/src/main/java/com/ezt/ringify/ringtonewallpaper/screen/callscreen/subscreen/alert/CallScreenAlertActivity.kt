package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallscreenAlertBinding
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashVibrationManager
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.VibrationType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.type.AllTypeAlertActivity
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity

class CallScreenAlertActivity :
    BaseActivity<ActivityCallscreenAlertBinding>(ActivityCallscreenAlertBinding::inflate) {

    private lateinit var prefs: SharedPreferences
    private lateinit var flashVibrationManager: FlashVibrationManager

    private var isFlashEnabled = false
    private var isVibrationEnabled = false
    private var isPlayed = false

    companion object {
        var flashTypeValue: String = "None"
        var vibrationValue: String = "None"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flashVibrationManager = FlashVibrationManager(this)
        prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)

        loadInitialSettings()

        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@CallScreenAlertActivity,
                    "INTER_CALLSCREEN"
                )
            }

            flashSwitcher.setOnClickListener {
                isFlashEnabled = !isFlashEnabled
                updateSwitchUI()
            }

            vibrationSwitcher.setOnClickListener {
                isVibrationEnabled = !isVibrationEnabled
                updateSwitchUI()
            }

            flashType.setOnClickListener {
                openTypeSelector("Flash", flashTypeValue)
            }

            vibrationType.setOnClickListener {
                openTypeSelector("Vibration", vibrationValue)
            }

            applyBtn.setOnClickListener {
                saveSettings()
                Toast.makeText(this@CallScreenAlertActivity, resources.getString(R.string.setting_saved), Toast.LENGTH_SHORT).show()
            }

            player.setOnClickListener {
                togglePlayPreview()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUIFromValues()
        loadBanner(this, BANNER_HOME)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@CallScreenAlertActivity, "INTER_CALLSCREEN")
    }

    private fun loadInitialSettings() {
        isFlashEnabled = prefs.getBoolean("FLASH_ENABLE", false)
        isVibrationEnabled = prefs.getBoolean("VIBRATION_ENABLE", false)
        flashTypeValue = prefs.getString("FLASH_TYPE", "None") ?: "None"
        vibrationValue = prefs.getString("VIBRATION_TYPE", "None") ?: "None"
    }

    private fun saveSettings() {
        prefs.edit()
            .putBoolean("FLASH_ENABLE", isFlashEnabled)
            .putBoolean("VIBRATION_ENABLE", isVibrationEnabled)
            .putString("FLASH_TYPE", flashTypeValue)
            .putString("VIBRATION_TYPE", vibrationValue)
            .apply()
    }

    private fun updateUIFromValues() {
        val default = getString(R.string.default_title)
        binding.flashTitle.text = if (flashTypeValue == "None") default else flashTypeValue
        binding.vibrationTitle.text = if (vibrationValue == "None") default else vibrationValue
        updateSwitchUI()
    }

    private fun updateSwitchUI() {
        binding.flashSwitcher.setImageResource(if (isFlashEnabled) R.drawable.switch_enabled else R.drawable.switch_disabled)
        binding.vibrationSwitcher.setImageResource(if (isVibrationEnabled) R.drawable.switch_enabled else R.drawable.switch_disabled)
    }

    private fun openTypeSelector(alertType: String, currentValue: String) {
        val intent = Intent(this, AllTypeAlertActivity::class.java).apply {
            putExtra("alertType", alertType)
            putExtra("currentValue", currentValue)
        }
        startActivity(intent)
    }

    private fun togglePlayPreview() {
        val flash = FlashType.fromLabel(flashTypeValue) ?: FlashType.NONE
        val vibration = VibrationType.fromLabel(vibrationValue) ?: VibrationType.NONE

        if (!isFlashEnabled && !isVibrationEnabled) {
            Toast.makeText(this, resources.getString(R.string.enable_function), Toast.LENGTH_SHORT).show()
            return
        }
        if (flash == FlashType.NONE && vibration == VibrationType.NONE) {
            Toast.makeText(this, resources.getString(R.string.select_testing_value), Toast.LENGTH_SHORT).show()
            return
        }

        isPlayed = !isPlayed
        binding.player.setImageResource(
            if (isPlayed) R.drawable.icon_pause_alert else R.drawable.icon_play_alert
        )

        if (isPlayed) {
            flashVibrationManager.startFlashAndVibration(true, flash, true, vibration) {
                println("onComplete: is here")
                isPlayed = false
                binding.player.setImageResource(R.drawable.icon_play_alert) // <- correct icon
            }
            binding.player.setImageResource(R.drawable.icon_pause_alert) // update immediately for user feedback
        } else {
            flashVibrationManager.stopFlashAndVibration()
            isPlayed = false
            binding.player.setImageResource(R.drawable.icon_play_alert)
        }
    }
}
