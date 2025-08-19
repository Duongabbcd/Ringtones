package com.ezt.ringify.ringtonewallpaper.remote.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
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

    fun logScreenView(screenName: String, screenClass: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

}