package com.ezt.ringify.ringtonewallpaper.screen.setting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPhoneSettingBinding
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity

class PhoneSettingActivity :
    BaseActivity<ActivityPhoneSettingBinding>(ActivityPhoneSettingBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@PhoneSettingActivity)
            }
            updateNotificationSwitchUI()
        }
    }

    private fun updateNotificationSwitchUI() {
        val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13, permission is granted by default
            true
        }

        val displayIcon = if (isGranted) R.drawable.switch_enabled else R.drawable.switch_disabled
        binding.notificationSwitcher.setImageResource(displayIcon)
        binding.notificationSwitcher.isEnabled = false // You can toggle this if needed
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@PhoneSettingActivity)
    }

    override fun onResume() {
        super.onResume()
        if (RemoteConfig.BANNER_COLLAP_ALL_070625 != "0") {
            AdsManager.showAdBanner(
                this,
                BANNER_HOME,
                binding.frBanner,
                binding.view,
                isCheckTestDevice = false
            ) {}
        }
    }

}