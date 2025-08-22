package com.ezt.ringify.ringtonewallpaper.screen.setting

import android.content.Intent
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySettingBinding
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.language.LanguageActivity
import com.ezt.ringify.ringtonewallpaper.screen.setting.dialog.ShowRateDialog
import com.ezt.ringify.ringtonewallpaper.utils.Common.composeEmail
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.openPrivacy

class SettingActivity: BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this, BANNER_HOME)
        binding.apply {
            backBtn.setOnClickListener {
                MainActivity.selectedTab = 0
                startActivity(Intent(this@SettingActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                })
            }

            languageOption.setOnClickListener {
                startActivity(Intent(this@SettingActivity, LanguageActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            }

            settingOption.setOnClickListener {
                startActivity(Intent(this@SettingActivity, PhoneSettingActivity::class.java))
            }

            rateOption.setOnClickListener {
                val dialog = ShowRateDialog(this@SettingActivity)
                dialog.show()
            }

            policyOption.setOnClickListener {
                this@SettingActivity.openPrivacy()
            }

            feedbackOption.setOnClickListener {
                this@SettingActivity.composeEmail(
                    getString(R.string.contact_email),
                    getString(R.string.email_feedback_title, BuildConfig.VERSION_NAME)
                )
            }


//            rateOption.gone()
//            feedbackOption.gone()
//            policyOption.gone()
            shareOption.gone()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@SettingActivity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        })
    }
}