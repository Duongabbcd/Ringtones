package com.ezt.ringify.ringtonewallpaper.screen.setting

import android.Manifest
import android.app.Dialog
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPhoneSettingBinding
import com.ezt.ringify.ringtonewallpaper.databinding.DialogResetBinding
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Utils
import java.io.IOException

class PhoneSettingActivity :
    BaseActivity<ActivityPhoneSettingBinding>(ActivityPhoneSettingBinding::inflate) {
    private var isNotificationEnabled: Boolean = false
    private lateinit var settingsResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this)
        Log.d(
            TAG,
            "PhoneSettingActivity: ${
                ContextCompat.checkSelfPermission(
                    this@PhoneSettingActivity, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }"
        )

        settingsResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Re-check permission after returning from Settings
            displayInitialSwitchUI()
        }

        displayInitialSwitchUI()
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            cacheTitle.text = Utils.getCacheSize(this@PhoneSettingActivity)


            binding.notificationSwitcher.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@PhoneSettingActivity, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Toggle normally
                        isNotificationEnabled = !isNotificationEnabled
                        saveAndUpdateSwitchUI()
                    } else {
                        // Show dialog
                        showGoToSettingsDialog()
                    }
                } else {
                    // OS < Android 13 → toggle normally
                    isNotificationEnabled = !isNotificationEnabled
                    saveAndUpdateSwitchUI()
                }
            }

            resetRingtoneBtn.setOnClickListener {
                val dialog = ResetDialog(this@PhoneSettingActivity, "ringtone") { result ->
                    if (result) {
                        val defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(
                            this@PhoneSettingActivity,
                            RingtoneManager.TYPE_RINGTONE
                        )
                        val defaultNotification = RingtoneManager.getActualDefaultRingtoneUri(
                            this@PhoneSettingActivity,
                            RingtoneManager.TYPE_NOTIFICATION
                        )

                        Log.d(TAG, "resetRingtoneBtn 1: $defaultRingtone")
                        Log.d(TAG, "resetRingtoneBtn 2: $defaultNotification")

                        RingtoneHelper.setAsSystemRingtone(
                            this@PhoneSettingActivity,
                            defaultRingtone,
                            false
                        )
                        RingtoneHelper.setAsSystemRingtone(
                            this@PhoneSettingActivity,
                            defaultNotification,
                            true
                        )

                        try {
                            // Optionally notify the user
                            Toast.makeText(
                                this@PhoneSettingActivity,
                                resources.getString(R.string.ringtone_1),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@PhoneSettingActivity,
                                resources.getString(R.string.ringtone_2),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                dialog.show()
            }


            resetWallpaperBtn.setOnClickListener {
                val dialog = ResetDialog(this@PhoneSettingActivity) { result ->
                    if (result) {
                        val wallpaperManager =
                            WallpaperManager.getInstance(this@PhoneSettingActivity)

                        try {
                            wallpaperManager.clear()
                            // Optionally notify the user
                            Toast.makeText(
                                this@PhoneSettingActivity,
                                resources.getString(R.string.wallpaper_1),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@PhoneSettingActivity,
                                resources.getString(R.string.wallpaper_2),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                dialog.show()
            }

            clearCacheBtn.setOnClickListener {
                val dialog = ResetDialog(this@PhoneSettingActivity, "cache") { result ->
                    try {
                        val cacheDir = this@PhoneSettingActivity.cacheDir
                        if (cacheDir != null && cacheDir.isDirectory) {
                            cacheDir.deleteRecursively()
                        }
                        cacheTitle.text = Utils.getCacheSize(this@PhoneSettingActivity)
                        Toast.makeText(
                            this@PhoneSettingActivity,
                            resources.getString(R.string.cache_1),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@PhoneSettingActivity,
                            resources.getString(R.string.cache_2),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                dialog.show()
            }
        }
    }

    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this@PhoneSettingActivity) { result ->
            if (result) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun displayInitialSwitchUI() {
        if (isTiramisuOrAbove) {
            var currentStatus = Common.getNotificationEnable(this)
            var granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED && currentStatus
            binding.notificationSwitcher.setImageResource(
                if (granted) R.drawable.switch_enabled else R.drawable.switch_disabled
            )
        } else {
            var currentStatus = Common.getNotificationEnable(this)
            binding.notificationSwitcher.setImageResource(
                if (currentStatus) R.drawable.switch_enabled else R.drawable.switch_disabled
            )
        }
    }

    private fun saveAndUpdateSwitchUI() {
        Common.setNotificationEnable(this, isNotificationEnabled)
        binding.notificationSwitcher.setImageResource(
            if (isNotificationEnabled) R.drawable.switch_enabled else R.drawable.switch_disabled
        )
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        val TAG = PhoneSettingActivity::class.java.simpleName

        val isTiramisuOrAbove by lazy {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        }
    }
}

class ResetDialog(
    context: Context,
    title: String = "wallpaper",
    private val onClickListener: (Boolean) -> Unit
) :
    Dialog(context) {
    private val binding by lazy { DialogResetBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(R.color.transparent)
    }

    private val question = when (title) {
        "ringtone" -> context.resources.getString(R.string.reset_ringtone_question)
        "cache" -> context.resources.getString(R.string.reset_cache_question)
        "wallpaper" -> context.resources.getString(R.string.reset_wallpaper_question)
        else -> context.resources.getString(R.string.reset_wallpaper_question)
    }

    private val title = when (title) {
        "ringtone" -> context.resources.getString(R.string.ringtone_title)
        "cache" -> context.resources.getString(R.string.reset_cache)
        "wallpaper" -> context.resources.getString(R.string.reset_wallpaper)
        else -> context.resources.getString(R.string.reset_wallpaper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            confirmQuestion.text = question
            confirmTitle.text = title
            okBtn.setOnClickListener {
                onClickListener(true)
                dismiss()
            }

            cancelBtn.setOnClickListener {
                onClickListener(false)
                dismiss()
            }

        }
    }

}