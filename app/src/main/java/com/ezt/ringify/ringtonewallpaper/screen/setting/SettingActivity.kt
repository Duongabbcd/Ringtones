package com.ezt.ringify.ringtonewallpaper.screen.setting

import android.content.Intent
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySettingBinding
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity
import com.ezt.ringify.ringtonewallpaper.screen.language.LanguageActivity
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.setting.PhoneSettingActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common

class SettingActivity: BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener {
                MainActivity.selectedTab = 0
                SearchRingtoneActivity.backToScreen(this@SettingActivity)
            }

            languageOption.setOnClickListener {
                startActivity(Intent(this@SettingActivity, LanguageActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            }

            settingOption.setOnClickListener {
                startActivity(Intent(this@SettingActivity, PhoneSettingActivity::class.java))
            }

            rateOption.setOnClickListener {
//                Common.showRate(this@SettingActivity)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        SearchRingtoneActivity.backToScreen(this@SettingActivity)
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