package com.ezt.ringify.ringtonewallpaper.screen.callscreen

import android.telecom.Call
import android.telecom.InCallService
import android.content.Intent
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

        val intent = Intent(this, CallScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("BACKGROUND", background)
            putExtra("CANCEL", cancel)
            putExtra("ANSWER", answer)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        if (call == currentCall) {
            currentCall = null
            activeCall = null
        }
    }
}
