package com.ezt.ringify.ringtonewallpaper.screen.home.dialog

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.ezt.ringify.ringtonewallpaper.databinding.DialogNotificationBinding

class NotificationDialog(
    private val activity: AppCompatActivity,
    private val onPermissionRequest: () -> Unit
) : Dialog(activity) {

    private val binding by lazy { DialogNotificationBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.okBtn.setOnClickListener {
            dismiss()
            onPermissionRequest() // Trigger permission request from activity
        }
    }
}