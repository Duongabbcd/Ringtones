package com.ezt.ringify.ringtonewallpaper.screen.callscreen

import android.telecom.Call
import android.telecom.InCallService
import android.content.Intent
import android.net.Uri
import android.telecom.CallAudioState
import android.util.Log

class MyInCallService : InCallService() {

    private var currentCall: Call? = null

    companion object {
        var activeCall: Call? = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        currentCall = call
        activeCall = call

        Log.d("MyInCallService", "onCallAdded: ${call.details}")

        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)

        val background = prefs.getString("BACKGROUND", null)
        val cancel = prefs.getString("CANCEL", null)
        val answer = prefs.getString("ANSWER", null)

        println("onCallAdded 0 : $background")
        println("onCallAdded 1 : $cancel")
        println("onCallAdded 2 : $answer")


        if (call.state == Call.STATE_RINGING) {
//
//            if (CallThemeApplication.spManager.vibrateState) {
//                vibrateHelper.vibrateDevice()
//            }
//
//            if (CallThemeApplication.spManager.splashState) {
//                flashHelper.startFlashLight()
//            }

            startActivity(CallScreenActivity.getStartIntent(this, background, cancel, answer))
        }

        call.registerCallback(callCallback)

    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (activeCall == call) {
            activeCall = null
        }
        CallController.onCallRemoved(call)
        call.unregisterCallback(callCallback)
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)

            if (state == Call.STATE_DIALING) {
                // Handle outgoing call state here
                val phoneNumber = call.details.handle.schemeSpecificPart
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            if (state == Call.STATE_DISCONNECTED) {
//                if (!MyApplication.spManager.vibrateState) {
//                    vibrateHelper.stopVibration()
//                }
//
//                if (!CallThemeApplication.spManager.splashState) {
//                    flashHelper.stopFlashLight()
//                }
            }
            if (state == Call.STATE_ACTIVE) {
//                if (!CallThemeApplication.spManager.vibrateState) {
//                    vibrateHelper.stopVibration()
//                }
//
//                if (!CallThemeApplication.spManager.splashState) {
//                    flashHelper.stopFlashLight()
//                }

            }
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
    }
}
