package com.ezt.ringify.ringtonewallpaper.screen.callscreen

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.VideoProfile
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.ezt.ringify.ringtonewallpaper.R
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallScreenBinding


class CallScreenActivity :
    BaseActivity<ActivityCallScreenBinding>(ActivityCallScreenBinding::inflate) {

    // Handler and timer runnable for call duration
    private val callTimerHandler = Handler(Looper.getMainLooper())
    private var callStartTime = 0L
    private val callTimerRunnable = object : Runnable {
        override fun run() {
            val elapsed = System.currentTimeMillis() - callStartTime
            val seconds = (elapsed / 1000) % 60
            val minutes = (elapsed / (1000 * 60)) % 60
            val timeFormatted = String.format("%02d:%02d", minutes, seconds)
            // Show timer as contentDescription on background image (accessible and visible in logs)
            binding.callScreenImage.contentDescription = "Call in Progress - $timeFormatted"
            callTimerHandler.postDelayed(this, 1000)
        }
    }

    // Load your background image from intent extras or fallback here
    val backgroundUrl = intent.getStringExtra("BACKGROUND")
    val answerImage = intent.getStringExtra("ANSWER")
    val cancelImage = intent.getStringExtra("CANCEL")

    private var callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            when (state) {
                Call.STATE_ACTIVE -> {
                    runOnUiThread {
                        binding.btnAnswer.visibility = View.GONE
                        Glide.with(this@CallScreenActivity).load(cancelImage)
                            .placeholder(R.drawable.icon_red_fail).error(R.drawable.icon_red_fail)
                            .into(binding.btnEndCall)
                        startCallTimer()
                    }
                }

                Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> {
                    closeWithFadeOut()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_screen)

        println("CallScreenActivity: is here")
        val call = MyInCallService.activeCall
        call?.registerCallback(callCallback)


        val displayNameOrNumber = getDisplayNameOrNumber(call)
        binding.txtName.text = displayNameOrNumber

        binding.btnAnswer.setOnClickListener {
            call?.let {
                if (it.state == Call.STATE_RINGING) {
                    it.answer(VideoProfile.STATE_AUDIO_ONLY)
                    binding.btnAnswer.isEnabled = false
                    binding.btnAnswer.alpha = 0.5f // visually indicate disabled state
                }
            }
        }

        binding.btnEndCall.setOnClickListener {
            call?.let {
                it.disconnect()
                binding.btnEndCall.isEnabled = false
                binding.btnEndCall.alpha = 0.5f
            }
        }


        if (!backgroundUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(backgroundUrl)
                .placeholder(R.drawable.default_callscreen)
                .error(R.drawable.default_callscreen)
                .into(binding.callScreenImage)
        } else {
            binding.callScreenImage.setImageResource(R.drawable.default_callscreen)
        }

        if (!answerImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(answerImage)
                .placeholder(R.drawable.icon_tick)
                .error(R.drawable.icon_tick)
                .into(binding.btnAnswer)
        } else {
            binding.btnAnswer.setImageResource(R.drawable.icon_tick)
        }

        if (!cancelImage.isNullOrEmpty()) {
            Glide.with(this)
                .load(cancelImage)
                .placeholder(R.drawable.icon_red_fail)
                .error(R.drawable.icon_red_fail)
                .into(binding.btnEndCall)
        } else {
            binding.btnEndCall.setImageResource(R.drawable.icon_red_fail)
        }
    }

    private fun startCallTimer() {
        callStartTime = System.currentTimeMillis()
        callTimerHandler.post(callTimerRunnable)
    }

    private fun stopCallTimer() {
        callTimerHandler.removeCallbacks(callTimerRunnable)
    }

    private fun closeWithFadeOut() {
        val rootView = findViewById<View>(android.R.id.content) // root view of activity
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                finish()
                overridePendingTransition(0, android.R.anim.fade_out)
            }
        })
        rootView.startAnimation(fadeOut)
    }

    private fun getDisplayNameOrNumber(call: Call?): String {
        if (call == null) return "Unknown"

        val handle: Uri? = call.details.handle
        val number = handle?.schemeSpecificPart ?: return "Unknown"

        val name = getContactNameFromNumber(this, number)
        return name ?: number // fallback to number if no name found
    }

    private fun getContactNameFromNumber(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCallTimer()
        MyInCallService.activeCall?.unregisterCallback(callCallback)
    }
}
