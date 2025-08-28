package com.ezt.ringify.ringtonewallpaper.screen.home

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.BannerAds
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.ads.new.RewardAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityMainBinding
import com.ezt.ringify.ringtonewallpaper.databinding.BottomSheetExitAppBinding
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.home.dialog.NotificationDialog
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.RingtoneFragment
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.wallpaper.WallpaperFragment
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.FeedbackDialog
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.setting.SettingActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search.SearchWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger
    private var now = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Common.setNotificationEnable(this, true)
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        loadBanner(this, BANNER_HOME)
        var countOpen = Common.getCountOpenApp(this)

        if (countOpen <= 1) {
            showNotificationDialog()
            Common.setCountOpenApp(this, 2)
        }

        checkIfNewWeek()
        RewardAds.initRewardAds(this)


        selectedTab = savedInstanceState?.getInt("selectedTab") ?: 0

        RingtonePlayerRemote.currentPlayingRingtone = Ringtone.EMPTY_RINGTONE
        binding.searchButton.setOnClickListener {
            if(selectedTab == 0) {
                analyticsLogger.logTagClick(100, "Search Ringtone", "search_ringtone_clicked")
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo("search_ringtone_screen", "main_screen", duration)
                startActivity(Intent(this, SearchRingtoneActivity::class.java))
            } else {
                analyticsLogger.logTagClick(101, "Search Wallpaper", "search_wallpaper_clicked")
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo("search_wallpaper_screen", "main_screen", duration)
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
                    binding.topFeedback.setOnClickListener {
                        val dialog = FeedbackDialog(this)
                        dialog.setCallScreenFeedback()
                        dialog.show()
                    }
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

    private fun checkIfNewWeek() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        val lastSavedWeek = prefs.getInt("last_week", -1)
        val lastSavedYear = prefs.getInt("last_year", -1)

        val isNewWeek = lastSavedWeek != currentWeek || lastSavedYear != currentYear

        if (isNewWeek) {
            Common.setAllNewRingtones(this, list = emptyList())
            Common.setAllEditorChoices(this, list = emptyList())
            Common.setAllWeeklyTrendingRingtones(this, list = emptyList())
            // It's a new week!
            Log.d(TAG, "✅ New week started!")

            // Save current week and year
            prefs.edit()
                .putInt("last_week", currentWeek)
                .putInt("last_year", currentYear)
                .apply()
        } else {
            Log.d(TAG, "📅 Still the same week.")
        }
    }

    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>
    private var openedNotificationSettings = false

    private fun showNotificationDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ → runtime permission check
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                Common.setNotificationEnable(this, true)
            } else {
                val dialog = NotificationDialog(this) {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (!dialog.isShowing) dialog.show()
            }

        } else {
            // Android < 13 → system setting check
            val notificationsEnabled =
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            if (notificationsEnabled) {
                Common.setNotificationEnable(this, true)
            } else {
                val dialog = NotificationDialog(this) {
                    openedNotificationSettings = true
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                    startActivity(intent)
                }
                if (!dialog.isShowing) dialog.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // If we just came back from settings, check again before showing dialog
        if (openedNotificationSettings) {
            openedNotificationSettings = false
            val notificationsEnabled =
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            if (notificationsEnabled) {
                Common.setNotificationEnable(this, true)
            }
        }
        displayScreen()
    }


    private fun displayScreen() {
        Log.d(TAG, "displayScreen: $selectedTab")
        when (selectedTab) {
            0 -> {
                binding.appName.setImageResource(R.drawable.icon_app_name)
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_RINGTONE,
                    adUnit = InterAds.INTER_RINGTONE
                )
                openFragment(RingtoneFragment.Companion.newInstance())
            }

            1 -> {
                binding.appName.setImageResource(R.drawable.icon_wallpaper_name)
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_WALLPAPER,
                    adUnit = InterAds.INTER_WALLPAPER
                )
                openFragment(WallpaperFragment.Companion.newInstance())

            }

            2 -> {
                binding.appName.setImageResource(R.drawable.icon_callscreen_name)
                InterAds.preloadInterAds(
                    this@MainActivity,
                    alias = InterAds.ALIAS_INTER_CALLSCREEN,
                    adUnit = InterAds.INTER_CALLSCREEN
                )
                openFragment(CallScreenFragment.Companion.newInstance())
            }

            3 -> {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo("setting_screen", "main_screen", duration)
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
        private var TAG = MainActivity::class.java.simpleName

        fun loadBanner(activity: AppCompatActivity, banner: String = BANNER_HOME) {
            println("RemoteConfig.BANNER_COLLAP_ALL_070625: ${RemoteConfig.BANNER_ALL}")
            if (RemoteConfig.BANNER_ALL != "0") {
                BannerAds.initBannerAds(activity, banner)
            }

        }
    }

    override fun onBackPressed() {
        val dialogBinding = BottomSheetExitAppBinding.inflate(layoutInflater)
        val dialog = Dialog(this)

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true) // optional, can cancel by tapping outside

        // Optional: customize dialog appearance
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.apply {
            textCancel.setOnClickListener {
                dialog.dismiss()
            }
            textExit.setOnClickListener {
                moveTaskToBack(true)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}