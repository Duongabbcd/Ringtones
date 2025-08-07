package com.ezt.ringify.ringtonewallpaper.screen.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityMainBinding
import kotlin.system.exitProcess
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.BannerAds
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.home.dialog.NotificationDialog
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.RingtoneFragment
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.wallpaper.WallpaperFragment
import com.ezt.ringify.ringtonewallpaper.screen.language.LanguageActivity
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
        showNotificationDialog(countOpen)
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

    private fun showNotificationDialog(countOpen: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && countOpen == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                return
            } else {
                val dialog = NotificationDialog(this) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        100
                    )
                }
                dialog.show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        displayScreen()
        loadBanner(this, BANNER_HOME)
    }


    private fun displayScreen() {
        println("displayScreen: $selectedTab")
        when (selectedTab) {
            0 -> {
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_RINGTONE,
                    adUnit = InterAds.INTER_RINGTONE
                )
                openFragment(RingtoneFragment.Companion.newInstance())
            }

            1 -> {
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_WALLPAPER,
                    adUnit = InterAds.INTER_WALLPAPER
                )
                openFragment(WallpaperFragment.Companion.newInstance())

            }

            2 -> {
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_CALLSCREEN,
                    adUnit = InterAds.INTER_CALLSCREEN
                )
                openFragment(CallScreenFragment.Companion.newInstance())
            }

            3 -> {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            else -> {
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_RINGTONE,
                    adUnit = InterAds.INTER_RINGTONE
                )
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

        fun loadBanner(activity: AppCompatActivity, banner: String = BANNER_HOME) {
            println("RemoteConfig.BANNER_COLLAP_ALL_070625: ${RemoteConfig.BANNER_COLLAP_ALL_070625}")
            if (RemoteConfig.BANNER_COLLAP_ALL_070625 != "0") {
                BannerAds.initBannerAds(activity, banner)
            }

        }
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