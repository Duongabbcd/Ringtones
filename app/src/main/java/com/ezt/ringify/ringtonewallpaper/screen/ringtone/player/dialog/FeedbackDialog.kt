package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.DialogFeedbackBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet
import com.ezt.ringify.ringtonewallpaper.utils.Common.composeEmail
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone

class FeedbackDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogFeedbackBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(R.color.transparent)
    }

    private var title: String = ""

    fun setRingtoneFeedback(ringtone: Ringtone) {
        binding.apply {
            ringToneName.text = "${ringtone.name}"

            firstTitle.text = context.resources.getString(R.string.feedback_1)
            secondTitle.text = context.resources.getString(R.string.feedback_2)
            thirdTitle.text = context.resources.getString(R.string.feedback_3)
        }
    }

    fun setCallScreenFeedback() {
        binding.apply {
            ringToneName.text = context.resources.getString(R.string.callScreen)
            firstTitle.text = context.resources.getString(R.string.call_screen_feedback_1)
            secondTitle.text = context.resources.getString(R.string.call_screen_feedback_2)
            feedback3.gone()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            okBtn.setOnClickListener {
                context.composeEmail(
                    context.getString(R.string.contact_email),
                    title
                )
                dismiss()
            }

            feedback1.setOnClickListener {
                title = firstTitle.text.toString()
                SortBottomSheet.updateDisplayIcons(listOf(firstIcon), listOf(secondIcon, thirdIcon))
            }

            feedback2.setOnClickListener {
                title = secondTitle.text.toString()
                SortBottomSheet.updateDisplayIcons(listOf(secondIcon), listOf(firstIcon, thirdIcon))
            }

            feedback3.setOnClickListener {
                title = thirdTitle.text.toString()
                SortBottomSheet.updateDisplayIcons(listOf(thirdIcon), listOf(secondIcon, firstIcon))
            }
        }
    }
}