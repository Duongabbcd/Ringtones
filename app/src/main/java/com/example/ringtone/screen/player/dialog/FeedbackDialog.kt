package com.example.ringtone.screen.player.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.ringtone.databinding.DialogFeedbackBinding
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.screen.ringtone.bottomsheet.SortBottomSheet

class FeedbackDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogFeedbackBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
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