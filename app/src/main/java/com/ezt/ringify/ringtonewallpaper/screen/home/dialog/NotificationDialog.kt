package com.ezt.ringify.ringtonewallpaper.screen.home.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.databinding.DialogNotificationBinding

class NotificationDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogNotificationBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            okBtn.setOnClickListener {
                dismiss()
            }
        }
    }
}