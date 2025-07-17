package com.example.ringtone.screen.setting

import android.content.Intent
import android.os.Bundle
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivitySettingBinding
import com.example.ringtone.screen.home.MainActivity

class SettingActivity: BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener {
                MainActivity.selectedTab = 0
                startActivity(Intent(this@SettingActivity, MainActivity::class.java))
            }
        }
    }
}