package com.ezt.ringify.ringtonewallpaper.screen.setting

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPhoneSettingBinding
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner

class PhoneSettingActivity :
    BaseActivity<ActivityPhoneSettingBinding>(ActivityPhoneSettingBinding::inflate) {
    private val isTiramisuOrAbove by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    private var currentWallpaper: Bitmap? = null

    private fun checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // On Android 13+ you don't need READ_EXTERNAL_STORAGE for wallpapers
            getWallpaperDrawable()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                getWallpaperDrawable()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("onRequestPermissionsResult: $requestCode and ${grantResults[0] == PackageManager.PERMISSION_GRANTED}")
        if (requestCode == STORAGE_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getWallpaperDrawable()
        } else {
            println("Permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestStoragePermission()
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            if (isTiramisuOrAbove) {
                binding.notificationSwitcher.isEnabled = true // You can toggle this if needed
            } else {
                binding.notificationSwitcher.isEnabled = false // You can toggle this if needed
            }

            resetRingtoneBtn.setOnClickListener {
                getAndSetDefaultRingtone(this@PhoneSettingActivity)

            }

            resetWallpaperBtn.setOnClickListener {
                getWallpaperDrawable()
            }
            updateNotificationSwitchUI()
        }
    }

    private fun updateNotificationSwitchUI() {
        val isGranted = if (isTiramisuOrAbove) {
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
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadBanner(this, BANNER_HOME)
    }

    fun getAndSetDefaultRingtone(context: Context) {
        // Get the current default ringtone URI
        val currentUri: Uri = RingtoneManager.getActualDefaultRingtoneUri(
            context, RingtoneManager.TYPE_RINGTONE
        )
        println("resetRingtoneBtn: $currentUri")

        // Set the default ringtone to the current one again (example)
        RingtoneManager.setActualDefaultRingtoneUri(
            context, RingtoneManager.TYPE_RINGTONE, currentUri
        )
    }

    @SuppressLint("MissingPermission")
    private fun getWallpaperDrawable() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(this)
            val drawable = wallpaperManager.drawable
            println("Got wallpaper drawable: $drawable")
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1001
    }
}