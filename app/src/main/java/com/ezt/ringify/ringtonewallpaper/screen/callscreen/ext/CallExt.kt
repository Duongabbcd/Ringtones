package com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext

import android.os.Build
import android.telecom.Call
import kotlin.collections.contains

private val OUTGOING_CALL_STATES =
    arrayOf(Call.STATE_CONNECTING, Call.STATE_DIALING, Call.STATE_SELECT_PHONE_ACCOUNT)

@Suppress("DEPRECATION")
fun Call?.getStateCompat(): Int {
    return when {
        this == null -> Call.STATE_DISCONNECTED
        isSPlus() -> details.state
        else -> state
    }
}

fun Call?.getCallDuration(): Int {
    return if (this != null) {
        val connectTimeMillis = details.connectTimeMillis
        if (connectTimeMillis == 0L) {
            return 0
        }
        ((System.currentTimeMillis() - connectTimeMillis) / 1000).toInt()
    } else {
        0
    }
}


fun Call.isOutgoing(): Boolean {
    return if (isQPlus()) {
        details.callDirection == Call.Details.DIRECTION_OUTGOING
    } else {
        OUTGOING_CALL_STATES.contains(getStateCompat())
    }
}

fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

enum class FlashType(val label: String) {
    NONE("None"),
    ONCE("Flash Once"),
    FAST_BLINK("Fast Blink"),
    SLOW_BLINK("Slow Blink"),
    SOS("SOS Signal");

    companion object {
        fun fromLabel(label: String): FlashType? {
            return entries.find { it.label == label }
        }
    }
}

enum class VibrationType(val label: String) {
    NONE("None"),
    SHORT("Short"),
    LONG("Long"),
    DOUBLE("Double Pulse"),
    HEARTBEAT("Heartbeat Pattern");

    companion object {
        fun fromLabel(label: String): VibrationType? {
            return VibrationType.entries.find { it.label == label }
        }
    }
}

