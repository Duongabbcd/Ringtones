package com.ezt.ringify.ringtonewallpaper.screen.home

import android.content.Intent
import android.os.Bundle
import com.admob.max.dktlibrary.AdmobUtils
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityMainBinding
import kotlin.system.exitProcess
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.home.dialog.NotificationDialog
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.RingtoneFragment
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.wallpaper.WallpaperFragment
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.setting.SettingActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search.SearchWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var countOpen = Common.getCountOpenApp(this)
        if (countOpen < 2) {
            countOpen++
            Common.setCountOpenApp(this, countOpen)
        }

        selectedTab = savedInstanceState?.getInt("selectedTab") ?: 0

        RingtonePlayerRemote.currentPlayingRingtone = Ringtone.EMPTY_RINGTONE
        binding.searchButton.setOnClickListener {
            if(selectedTab == 0) {
                startActivity(Intent(this, SearchRingtoneActivity::class.java))
            } else {
                startActivity(Intent(this, SearchWallpaperActivity::class.java))
            }

        }
        displayScreen()
        binding.appName.setOnClickListener {
            val notificationDialog = NotificationDialog(this)
            notificationDialog.show()
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            binding.topFeedback.gone()
            binding.searchButton.visible()
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
                    binding.topFeedback.visible()
                    binding.searchButton.gone()
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
        if (RemoteConfig.BANNER_COLLAP_ALL_070625 != "0") {
            AdsManager.showAdBanner(
                this,
                BANNER_HOME,
                binding.frBanner,
                binding.view,
                isCheckTestDevice = false
            ) {}
        }

        internetConnected = AdmobUtils.isNetworkConnected(this@MainActivity)
        println("onResume: $internetConnected")

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

    override fun onDestroy() {
        super.onDestroy()
        selectedTab = 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedTab", selectedTab)
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