package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview

import android.os.Bundle
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity.Companion.endCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity.Companion.startCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity.Companion.videoUrl

class PreviewCallScreenActivity : BaseActivity<ActivityPreviewCallscreenBinding>(ActivityPreviewCallscreenBinding::inflate){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        val background = prefs.getString("BACKGROUND", "") ?: ""
        if(background.isNotEmpty()) {
            videoUrl = background
        }

              val start = prefs.getString("ANSWER", "") ?: ""
        if(start.isNotEmpty()) {
            startCall = start
        }
              val end = prefs.getString("CANCEL", "") ?: ""
        if(end.isNotEmpty()) {
            endCall = end
        }
        val avatar = prefs.getString("AVATAR", "") ?: ""
        if(avatar.isNotEmpty()) {
            avatarUrl = avatar
        }

        println("videoUrl: $avatarUrl")


        binding.apply {

            Glide.with(this@PreviewCallScreenActivity).load(videoUrl)
                .placeholder(R.drawable.default_callscreen).error(R.drawable.default_callscreen)
                .into(binding.callScreenImage)
            Glide.with(this@PreviewCallScreenActivity).load(avatarUrl)
                .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
                .into(binding.avatar)

            Glide.with(this@PreviewCallScreenActivity).load(endCall)
                .placeholder(R.drawable.icon_red_fail).error(R.drawable.icon_red_fail)
                .into(binding.callEnd)
            Glide.with(this@PreviewCallScreenActivity).load(startCall)
                .placeholder(R.drawable.icon_tick).error(R.drawable.icon_tick)
                .into(binding.callAccept)

            closeBtn.setOnClickListener { finish() }
        }
    }
}