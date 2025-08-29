package com.ezt.ringify.ringtonewallpaper.screen.callscreen

import android.annotation.SuppressLint
import android.telecom.Call
import android.telecom.VideoProfile
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext.getStateCompat
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.collections.removeAll
import kotlin.let

class CallController {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var call: Call? = null
        private val calls = mutableListOf<Call>()
        private val listeners = CopyOnWriteArraySet<CallControllerListener>()

        fun onCallAdded(call: Call) {
            this.call = call
            calls.add(call)
            for (listener in listeners) {
                listener.onPrimaryCallChanged(call)
            }
            call.registerCallback(object : Call.Callback() {
                override fun onStateChanged(call: Call, state: Int) {
                    updateState()
                }

                override fun onDetailsChanged(call: Call, details: Call.Details) {
                    updateState()
                }

                override fun onConferenceableCallsChanged(
                    call: Call,
                    conferenceableCalls: MutableList<Call>
                ) {
                    updateState()
                }
            })
        }

        fun onCallRemoved(call: Call) {
            calls.remove(call)
            updateState()

            for (listener in listeners) {
                listener.onCallEnded(call)
            }
        }

        private fun updateState() {

            for (listener in listeners) {
                listener.onStateChanged()
                call?.let { listener.onPrimaryCallChanged(it) }
            }

            calls.removeAll { it.getStateCompat() == Call.STATE_DISCONNECTED }
        }

        fun getPrimaryCall(): Call? {
            return call
        }

        fun accept() {
            call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }

        fun reject() {
            if (call == null) {
                val state = getState()
                if (state == Call.STATE_RINGING) {
                    call!!.reject(false, null)
                } else if (state == Call.STATE_DISCONNECTED && state == Call.STATE_DISCONNECTING) {
                    call!!.disconnect()
                }
            }
        }

        fun addListener(listener: CallControllerListener) {
            listeners.add(listener)
        }

        fun removeListener(listener: CallControllerListener) {
            listeners.remove(listener)
        }

        fun getState() = getPrimaryCall()?.getStateCompat()
    }
}

interface CallControllerListener {
    fun onStateChanged()
    fun onPrimaryCallChanged(call: Call)
    fun onCallEnded(call: Call)
}
