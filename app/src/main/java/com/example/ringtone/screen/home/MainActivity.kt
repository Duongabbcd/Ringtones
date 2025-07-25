package com.example.ringtone.screen.home

import android.content.Intent
import android.os.Bundle
import com.admob.max.dktlibrary.AdmobUtils
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityMainBinding
import kotlin.system.exitProcess
import com.example.ringtone.R
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.screen.home.dialog.NotificationDialog
import com.example.ringtone.screen.home.subscreen.callscreen.CallScreenFragment
import com.example.ringtone.screen.home.subscreen.ringtone.RingtoneFragment
import com.example.ringtone.screen.home.subscreen.wallpaper.WallpaperFragment
import com.example.ringtone.screen.ringtone.search.SearchRingtoneActivity
import com.example.ringtone.screen.setting.SettingActivity
import com.example.ringtone.screen.wallpaper.search.SearchWallpaperActivity
import com.example.ringtone.utils.RingtonePlayerRemote
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RingtonePlayerRemote.currentPlayingRingtone = Ringtone.EMPTY_RINGTONE
        binding.searchButton.setOnClickListener {
            if(selectedTab == 0) {
                startActivity(Intent(this, SearchRingtoneActivity::class.java))
            } else {
                startActivity(Intent(this, SearchWallpaperActivity::class.java))
            }

        }

        binding.appName.setOnClickListener {
            val notificationDialog = NotificationDialog(this)
            notificationDialog.show()
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.ringTone -> {
                    selectedTab = 0
                    displayScreen()
                }

                R.id.wallPaper -> {
                    selectedTab = 1
                    displayScreen()
                }

                R.id.callScreen -> {
                    selectedTab = 2
                    displayScreen()
                }

                R.id.setting -> {
                    selectedTab = 3
                    displayScreen()
                }

                else -> {
                    selectedTab = 0
                    displayScreen()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()

        internetConnected = AdmobUtils.isNetworkConnected(this@MainActivity)
        println("onResume: $internetConnected")
        displayScreen()
    }

    private fun displayScreen() {
        println("displayScreen: $selectedTab")
        when (selectedTab) {
            0 -> {
                openFragment(RingtoneFragment.Companion.newInstance())
            }

            1 -> {
                openFragment(WallpaperFragment.Companion.newInstance())

            }

            2 -> {
                openFragment(CallScreenFragment.Companion.newInstance())
            }

            3 -> {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            else -> {
                openFragment(RingtoneFragment.Companion.newInstance())
            }
        }
    }


    companion object {
        var selectedTab = 0
        var isChangeTheme = false
        var count = 0
        var displayMode = DisplayMode.WALLPAPER

        var internetConnected = false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        exitProcess(-1)
    }
}

enum class DisplayMode() {
    WALLPAPER,
    SETTING,
    CALLSCREEN,
    RINGTONE
}