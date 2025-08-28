package com.ezt.ringify.ringtonewallpaper.remote.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsLogger @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun logTagClick(tagId: Int, tagName: String, eventName: String) {

        val bundle = Bundle().apply {
            putLong("tag_id", tagId.toLong())
            putString("tag_name", tagName)
        }
        firebaseAnalytics.logEvent(eventName.lowercase(), bundle)
    }

    fun logScreenGo(screenName: String, prevScreenName: String, prevScreenDuration: Long) {
        val bundle = Bundle().apply {
            putString("screen_name", screenName)
            putString("prev_screen_name", prevScreenName)
            putLong("prev_screen_duration", prevScreenDuration)
        }
        firebaseAnalytics.logEvent("screen_go", bundle)
    }
}