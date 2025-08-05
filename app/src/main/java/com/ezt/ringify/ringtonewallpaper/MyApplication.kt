package com.ezt.ringify.ringtonewallpaper

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.admob.max.dktlibrary.application.AdsApplication
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : AdsApplication(), Application.ActivityLifecycleCallbacks{
    override fun onCreate() {
        super.onCreate()
        instance = this // ✅ Fix: Set instance here

        FirebaseApp.initializeApp(this)
        val deviceId = Common.getDeviceId(this)
        val secret = "abcadhjgashjd1231" // TODO: Replace with your actual secret
        val jwt = Common.generateJwt(deviceId, secret)
        println("MyApplication: $jwt")
        RingtoneRepository.TOKEN = jwt
    }

    override fun onCreateApplication() {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val lang = Common.getPreLanguage(this)
        println("onActivityCreated: $lang")
        Common.setLocale(this@MyApplication, lang)
    }

    override fun onActivityStarted(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
        TODO("Not yet implemented")
    }

    companion object {
        private var instance: MyApplication? = null

        lateinit var mFirebaseAnalytics: FirebaseAnalytics


        fun getInstance(): MyApplication {
            return instance!!
        }

        @JvmStatic
        fun initROAS(revenue: Long, currency: String) {
            try {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(instance)
                val editor: SharedPreferences.Editor = sharedPref.edit()
                val currentImpressionRevenue = revenue / 1000000
                // make sure to divide by 10^6
                val previousTroasCache: Float = sharedPref.getFloat(
                    "TroasCache",
                    0F
                ) //Use App Local storage to store cache of tROAS
                val currentTroasCache = (previousTroasCache + currentImpressionRevenue).toFloat()
                //check whether to trigger  tROAS event
                if (currentTroasCache >= 0.01) {
                    LogTroasFirebaseAdRevenueEvent(currentTroasCache, currency)
                    editor.putFloat("TroasCache", 0f) //reset TroasCache
                } else {
                    editor.putFloat("TroasCache", currentTroasCache)
                }
                editor.commit()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private fun LogTroasFirebaseAdRevenueEvent(tRoasCache: Float, currency: String) {
            try {
                val bundle = Bundle()
                bundle.putDouble(
                    FirebaseAnalytics.Param.VALUE,
                    tRoasCache.toDouble()
                ) //(Required)tROAS event must include Double Value
                bundle.putString(
                    FirebaseAnalytics.Param.CURRENCY,
                    currency
                ) //put in the correct currency
                mFirebaseAnalytics.logEvent("Daily_Ads_Revenue", bundle)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }


        @JvmStatic
        fun trackingEvent(event: String) {
            try {
                val params = Bundle()
                mFirebaseAnalytics.logEvent(event, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}