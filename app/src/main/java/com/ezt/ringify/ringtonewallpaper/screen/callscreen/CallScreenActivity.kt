package com.ezt.ringify.ringtonewallpaper.screen.callscreen

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ezt.ringify.ringtonewallpaper.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallScreenBinding
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.FlashVibrationManager
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.VibrationType
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.service.MyInCallService
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity

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

            findViewById<TextView>(R.id.callerTime).visibility = View.VISIBLE
            findViewById<TextView>(R.id.callerTime).text = timeFormatted
            callTimerHandler.postDelayed(this, 1000)
        }
    }

    private var backgroundUrl: String? = null
    private var cancelImage: String? = null
    private var answerImage: String? = null
    private var avatarImage: String? = null


    private val telecomCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
                finish() // close the screen
            }
        }
    }
    private val prefs by lazy { getSharedPreferences("callscreen_prefs", Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val call = MyInCallService.activeCall
        call?.registerCallback(telecomCallback)

        backgroundUrl = intent.getStringExtra("BACKGROUND")
        cancelImage = intent.getStringExtra("CANCEL")
        answerImage = intent.getStringExtra("ANSWER")
        avatarImage = intent.getStringExtra("AVATAR")

        val isFlashEnabled = prefs.getBoolean("FLASH_ENABLE", true)
        val isVibrationEnabled = prefs.getBoolean("VIBRATION_ENABLE", true)

        // Get enum types
        val flashTypeLabel = prefs.getString("FLASH_TYPE", "None") ?: "None"
        val vibrationTypeLabel = prefs.getString("VIBRATION_TYPE", "None") ?: "None"

        val flashType = FlashType.fromLabel(flashTypeLabel) ?: FlashType.NONE
        val vibrationType = VibrationType.fromLabel(vibrationTypeLabel) ?: VibrationType.NONE
        flashVibrationManager.startFlashAndVibration(
            isFlashEnabled,
            flashType,
            isVibrationEnabled,
            vibrationType
        )
        Log.d(
            "CallScreenActivity",
            "Intent received -> background: $backgroundUrl, cancel: $cancelImage, answer: $answerImage"
        )
        countPhoneCallingTime()
        binding.callAccept.setImageDrawable(null)
        binding.callEnd.setImageDrawable(null)

        binding.apply {
            callerNameLabel.text = getDisplayNameOrNumber(MyInCallService.activeCall)
            callEnd.setOnClickListener {
                val call = MyInCallService.activeCall
                if (call != null) {
                    call.disconnect()
                    finish()
                } else {
                    Log.e("CallScreen", "No active call to reject")
                }
            }

            callAccept.setOnClickListener {
                val call = MyInCallService.activeCall
                if (call != null) {
                    call.answer(VideoProfile.STATE_AUDIO_ONLY)
                } else {
                    Log.e("CallScreen", "No active call to answer")
                }
            }

            val placeholderDrawable =
                ContextCompat.getDrawable(this@CallScreenActivity, R.drawable.default_callscreen)
            Glide.with(this@CallScreenActivity)
                .load(CallScreenEditorActivity.Companion.backgroundUrl)
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


            Glide.with(this@CallScreenActivity)
                .load(avatarImage)
                .placeholder(R.drawable.default_cs_avt)
                .error(R.drawable.default_cs_avt)
                .into(avatar)

            Glide.with(this@CallScreenActivity)
                .load(answerImage)
                .placeholder(R.drawable.icon_start_call)
                .error(R.drawable.icon_start_call)
                .into(callAccept)

            Glide.with(this@CallScreenActivity)
                .load(cancelImage)
                .placeholder(R.drawable.icon_end_call)
                .error(R.drawable.icon_end_call)
                .into(callEnd)
        }
    }

    private fun countPhoneCallingTime() {
        MyInCallService.activeCall?.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                if (state == Call.STATE_ACTIVE) {
                    callStartTime = System.currentTimeMillis()
                    callTimerHandler.post(callTimerRunnable)
                    flashVibrationManager.stopFlashAndVibration() // Stop when answered or call ended
                } else if (state == Call.STATE_DISCONNECTED) {
                    callTimerHandler.removeCallbacks(callTimerRunnable)
                    finish()
                } else if (state == Call.STATE_DISCONNECTED) {
                    flashVibrationManager.stopFlashAndVibration() // Stop when answered or call ended
                }
            }
        })
    }

    private lateinit var flashVibrationManager: FlashVibrationManager
    private fun getDisplayNameOrNumber(call: Call?): String {
        if (call == null) return "Unknown"

        val handle: Uri? = call.details.handle
        val number = handle?.schemeSpecificPart ?: return "Unknown"

        // Query contact name from Contacts
        val name = getContactNameFromNumber(this, number)
        return name ?: number // fallback to number
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


    private fun stopCallTimer() {
        callTimerHandler.removeCallbacks(callTimerRunnable)
    }



    override fun onDestroy() {
        super.onDestroy()
        MyInCallService.activeCall?.unregisterCallback(telecomCallback)
        stopCallTimer()
        flashVibrationManager.stopFlashAndVibration()
    }

    companion object {
        fun getStartIntent(
            context: Context,
            background: String?,
            cancel: String?,
            answer: String?,
            avatar: String?
        ): Intent {
            val openAppCallIntent = Intent(context, CallScreenActivity::class.java)
            openAppCallIntent.apply {
                putExtra("BACKGROUND", background)
                putExtra("CANCEL", cancel)
                putExtra("ANSWER", answer)
                putExtra("AVATAR", avatar)
            }
            openAppCallIntent.action = Intent.ACTION_VIEW
            openAppCallIntent.flags =
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            return openAppCallIntent
        }
    }

}
