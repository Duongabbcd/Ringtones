package com.example.ringtone.screen.home

import android.content.Intent
import android.os.Bundle
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityMainBinding
import kotlin.system.exitProcess
import com.example.ringtone.R
import com.example.ringtone.screen.home.subscreen.first_screen.RingtoneFragment
import com.example.ringtone.screen.search.SearchActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
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
        displayScreen()
    }

    private fun displayScreen() {
        println("displayScreen: $selectedTab")
        when (selectedTab) {
            0 -> {
                openFragment(RingtoneFragment.Companion.newInstance())
            }

            1 -> {
                openFragment(WallpaperFragment.newInstance())

            }

            2 -> {
                openFragment(CallScreenFragment.newInstance())
            }

            3 -> {
                openFragment(SettingFragment.newInstance())
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