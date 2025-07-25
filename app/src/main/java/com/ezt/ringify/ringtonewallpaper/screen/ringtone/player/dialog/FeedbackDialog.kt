package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.databinding.DialogFeedbackBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet

class FeedbackDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogFeedbackBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(R.color.transparent)
    }

    fun setRingtoneFeedback(ringtone: Ringtone) {
        binding.apply {
            ringToneName.text = "${ringtone.name}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            okBtn.setOnClickListener {
                dismiss()
            }

            feedback1.setOnClickListener {
                SortBottomSheet.updateDisplayIcons(firstIcon, listOf(secondIcon, thirdIcon))
            }

            feedback2.setOnClickListener {
                SortBottomSheet.updateDisplayIcons(secondIcon, listOf(firstIcon, thirdIcon))
            }

            feedback3.setOnClickListener {
                SortBottomSheet.updateDisplayIcons(thirdIcon, listOf(secondIcon, firstIcon))
            }
        }
    }
}