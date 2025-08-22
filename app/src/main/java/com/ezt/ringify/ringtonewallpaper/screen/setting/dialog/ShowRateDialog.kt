package com.ezt.ringify.ringtonewallpaper.screen.setting.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.DialogShowRateBinding
import com.ezt.ringify.ringtonewallpaper.screen.setting.customview.CustomRatingBar
import com.ezt.ringify.ringtonewallpaper.utils.Common.composeEmail
import com.ezt.ringify.ringtonewallpaper.utils.Common.rateApp

class ShowRateDialog(context: Context) : Dialog(context) {
    private val binding by lazy { DialogShowRateBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    var userRate: Int = 5
        set(value) {
            field = value

            binding.apply {
                rateUsButton.isEnabled = userRate > 0
                if (userRate > 0) {
                    rateUsButton.setBackgroundResource(R.drawable.background_radius_12)
                } else {
                    rateUsButton.setBackgroundResource(R.drawable.background_radius_12_gray)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            userRate = 5
            ratingBar.setMaxRating(5)
            ratingBar.setRating(5)
            ratingBar.setRatingChangeListener(object : CustomRatingBar.RatingChangeListener {
                override fun onRatingChanged(rating: Int) {
                    userRate = rating
                }
            })

            rateUsButton.setOnClickListener {
                if (userRate in 1..4) {
                    context.composeEmail(
                        context.getString(R.string.contact_email),
                        context.getString(R.string.email_feedback_title, BuildConfig.VERSION_NAME)
                    )
                } else {
                    requestReview()
                }
                dismiss()
            }
        }
    }

    private fun requestReview() {
        context.rateApp()
    }
}