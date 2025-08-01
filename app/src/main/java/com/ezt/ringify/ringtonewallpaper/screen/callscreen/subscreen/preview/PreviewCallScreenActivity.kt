package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.endCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.startCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.backgroundUrl

class PreviewCallScreenActivity :
    BaseActivity<ActivityPreviewCallscreenBinding>(ActivityPreviewCallscreenBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        val background = prefs.getString("BACKGROUND", "") ?: ""
        if (background.isNotEmpty()) {
            backgroundUrl = background
        }

        val start = prefs.getString("ANSWER", "") ?: ""
        if (start.isNotEmpty()) {
            startCall = start
        }
        val end = prefs.getString("CANCEL", "") ?: ""
        if (end.isNotEmpty()) {
            endCall = end
        }
        val avatar = prefs.getString("AVATAR", "") ?: ""
        if (avatar.isNotEmpty()) {
            avatarUrl = avatar
        }

        println("videoUrl: $avatarUrl")


        binding.apply {
            val placeholderDrawable = ContextCompat.getDrawable(
                this@PreviewCallScreenActivity,
                R.drawable.default_callscreen
            )
            Glide.with(this@PreviewCallScreenActivity)
                .load(backgroundUrl)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        binding.callScreenImage.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        binding.callScreenImage.background = placeholder ?: placeholderDrawable
                    }
                })

            Glide.with(this@PreviewCallScreenActivity).load(avatarUrl)
                .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
                .into(binding.avatar)

            Glide.with(this@PreviewCallScreenActivity).load(endCall)
                .placeholder(R.drawable.icon_end_call).error(R.drawable.icon_end_call)
                .into(binding.callEnd)
            Glide.with(this@PreviewCallScreenActivity).load(startCall)
                .placeholder(R.drawable.icon_start_call).error(R.drawable.icon_start_call)
                .into(binding.callAccept)

            closeBtn.setOnClickListener { finish() }
        }
    }
}