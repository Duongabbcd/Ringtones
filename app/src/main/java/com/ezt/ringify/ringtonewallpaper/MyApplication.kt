package com.ezt.ringify.ringtonewallpaper

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.admob.max.dktlibrary.application.AdsApplication
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : AdsApplication(), Application.ActivityLifecycleCallbacks{
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    private val activityStartTimes = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        instance = this // ✅ Fix: Set instance here

        FirebaseApp.initializeApp(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val deviceId = Common.getDeviceId(this)
        val secret = "abcadhjgashjd1231" // TODO: Replace with your actual secret
        val jwt = Common.generateJwt(deviceId, secret)
        println("MyApplication: $jwt")
        RingtoneRepository.TOKEN = jwt

        registerActivityLifecycleCallbacks(this)
    }

    override fun onCreateApplication() {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val lang = Common.getPreLanguage(this)
        println("onActivityCreated: $lang")
        Common.setLocale(this@MyApplication, lang)
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            println("App entered foreground")
        }
    }

    override fun onActivityResumed(activity: Activity) {
        val name = activity::class.java.simpleName
        activityStartTimes[name] = System.currentTimeMillis()
    }

    override fun onActivityPaused(activity: Activity) {
        //do nothing
    }

    override fun onActivityStopped(activity: Activity) {
        val name = activity::class.java.simpleName
        val startTime = activityStartTimes.remove(name)

        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            if (startTime != null) {
                val durationMs = System.currentTimeMillis() - startTime
                Log.d("onActivityStopped", "$name was active for ${durationMs}ms")
                // Optionally, send to Firebase:
                analyticsLogger.logScreenExit(name, durationMs)
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        //do nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        val name = activity::class.java.simpleName
        val startTime = activityStartTimes.remove(name)

        if (startTime != null) {
            val durationMs = System.currentTimeMillis() - startTime
            Log.d("onActivityDestroyed", "$name was active for ${durationMs}ms")
            // Optionally, send to Firebase:
            analyticsLogger.logScreenExit(name, durationMs)
        }
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
                    logTroasFirebaseAdRevenueEvent(currentTroasCache, currency)
                    editor.putFloat("TroasCache", 0f) //reset TroasCache
                } else {
                    editor.putFloat("TroasCache", currentTroasCache)
                }
                editor.apply()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private fun logTroasFirebaseAdRevenueEvent(tRoasCache: Float, currency: String) {
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