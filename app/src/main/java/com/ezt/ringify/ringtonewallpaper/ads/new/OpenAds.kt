package com.ezt.ringify.ringtonewallpaper.ads.new

import android.app.Activity
import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.ezt.ringify.ringtonewallpaper.ads.helper.Prefs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

object OpenAds {

    private const val OPEN_TEST_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val OPEN_ID_DEFAULT = "your-ad-id"

    private var appOpenAd: AppOpenAd? = null
    private var isOpenShowingAd = false
    val disableClasses: ArrayList<Class<*>> = arrayListOf()

    private var loadTimeOpenAd: Long = 0

    fun initOpenAds(context: Context, callback: () -> Unit) {
        println("initOpenAds: $appOpenAd and isOpenAdsCanUse:  $${isOpenAdsCanUse()}")
        if (appOpenAd == null || !isOpenAdsCanUse()) {
            appOpenAd = null
            AppOpenAd.load(
                context,
                if (BuildConfig.DEBUG) OPEN_TEST_ID else OPEN_ID_DEFAULT,
                getAdRequest(),
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        println("onAdLoaded: $appOpenAd and $ad")
                        appOpenAd?.setOnPaidEventListener { adValue ->
                            try {
                                MyApplication.initROAS(adValue.valueMicros, adValue.currencyCode)
                                val adRevenue =
                                    AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB).apply {
                                        setRevenue(
                                            adValue.valueMicros / 1_000_000.0,
                                            adValue.currencyCode
                                        )
                                    }
                                Adjust.trackAdRevenue(adRevenue)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        loadTimeOpenAd = Date().time
                        callback?.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        println("onAdFailedToLoad: $loadAdError")
                        appOpenAd = null
                        callback?.invoke()
                    }
                }
            )
        } else {
            callback?.invoke()
        }
    }

    private fun isOpenAdsCanUse(): Boolean {
        val dateDifference = Date().time - loadTimeOpenAd
        val numMilliSecondsPerHour = 3600000
        return dateDifference < numMilliSecondsPerHour * 4
    }

    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    fun isCanShowOpenAds(): Boolean {
        val result = appOpenAd != null && !isOpenShowingAd
        println("isCanShowOpenAds: $appOpenAd and $isOpenShowingAd")
        return result
    }

    fun showOpenAds(context: Activity, callback: () -> Unit) {
        try {
            val prefs = Prefs(MyApplication.getInstance())
            if (prefs.premium || prefs.isRemoveAd) {
                callback.invoke()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val condition = System.currentTimeMillis() - lastTimeShowAds > InterAds.TIME_DELAY
        println("showOpenAds: $condition and ${isCanShowOpenAds()}")
        if (condition) {
            if (isCanShowOpenAds()) {
                appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        callback.invoke()
                    }

                    override fun onAdShowedFullScreenContent() {
                        isOpenShowingAd = true
                    }

                    override fun onAdDismissedFullScreenContent() {
                        isOpenShowingAd = false
                        appOpenAd = null
                        initOpenAds(context) {

                        }
                        InterAds.startDelay()
                        startDelay()
                        callback.invoke()

                    }
                }
                appOpenAd?.show(context)
            } else {
                callback.invoke()

            }
        } else {
            callback.invoke()

        }
    }

    private var lastTimeShowAds = 0L

    fun startDelay() {
        lastTimeShowAds = System.currentTimeMillis()
    }

    fun disableAdsOpenForActivity(activityClass: Class<*>) {
        if (!disableClasses.contains(activityClass)) {
            disableClasses.add(activityClass)
        }
    }

    fun enableAdsOpenForActivity(activityClass: Class<*>) {
        disableClasses.remove(activityClass)
    }
}