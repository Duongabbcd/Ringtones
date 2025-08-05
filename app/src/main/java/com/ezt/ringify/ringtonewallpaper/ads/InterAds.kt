package com.ezt.ringify.ringtonewallpaper.ads

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.adjust.sdk.AdjustAdRevenue
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date
import kotlin.apply
import kotlin.jvm.functions.Function0
import kotlin.takeIf
import com.ezt.ringify.ringtonewallpaper.R

object InterAds {

    val TIME_DELAY = if (BuildConfig.DEBUG) 10_000L else 60_000L
    private const val INTER_SPLASH_DEFAULT = "your-ad-id"
    private const val INTER_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val INTER_ID_DEFAULT = "your-ad-id"

    private var adObserver = MutableLiveData<InterstitialAd?>(null)

    private var mInterstitialAd: InterstitialAd?
        get() = adObserver.value
        set(value) {
            adObserver.value = value
        }


    private var isLoading = false
    private var isShowing = false
    private var loadTimeAd: Long = 0

    private var loadingDialog: Dialog? = null

    fun initInterAds(ac: Context, callback: () -> Unit) {
        if (isCanLoadAds()) {
            mInterstitialAd = null
            isLoading = true
            InterstitialAd.load(
                ac,
                if (BuildConfig.DEBUG) INTER_TEST_ID else INTER_ID_DEFAULT,
                getAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        isLoading = false

                        mInterstitialAd = interstitialAd
                        mInterstitialAd?.onPaidEventListener = OnPaidEventListener { adValue ->
                            try {
                                MyApplication.Companion.initROAS(
                                    adValue.valueMicros,
                                    adValue.currencyCode
                                )
//                                val adRevenue =
//                                    AdjustAdRevenue(com.adjust.sdk.AdjustConfig.AD_REVENUE_ADMOB)
//                                    AdjustAdRevenue.setRevenue(
//                                    adValue.valueMicros / 1_000_000.0,
//                                    adValue.currencyCode
//                                )
//                                com.adjust.sdk.Adjust.trackAdRevenue(adRevenue)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        loadTimeAd = Date().time
                        callback.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoading = false
                        mInterstitialAd = null

                        callback.invoke()
                    }
                })
        } else {
            callback.invoke()
        }
    }

    fun initInterSplash(ac: Context, callback: Callback?) {
        if (isCanLoadAds()) {
            isLoading = true
            mInterstitialAd = null
            InterstitialAd.load(
                ac,
                if (BuildConfig.DEBUG) INTER_TEST_ID else INTER_SPLASH_DEFAULT,
                getAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        mInterstitialAd = interstitialAd
                        mInterstitialAd?.onPaidEventListener = OnPaidEventListener { adValue ->
                            try {
                                MyApplication.Companion.initROAS(
                                    adValue.valueMicros,
                                    adValue.currencyCode
                                )
//                                val adRevenue =
//                                    com.adjust.sdk.AdjustAdRevenue(com.adjust.sdk.AdjustConfig.AD_REVENUE_ADMOB)
//                                com.adjust.sdk.AdjustAdRevenue.setRevenue(
//                                    adValue.valueMicros / 1_000_000.0,
//                                    adValue.currencyCode
//                                )
//                                com.adjust.sdk.Adjust.trackAdRevenue(adRevenue)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        isLoading = false
                        loadTimeAd = Date().time
                        callback?.callback()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialAd = null
                        isLoading = false
                        callback?.callback()
                    }
                })
        } else {
            callback?.callback()
        }
    }

    private fun getAdRequest(): AdRequest = AdRequest.Builder().build()

    private fun isCanLoadAds(): Boolean =
        !isLoading && !isShowing && (mInterstitialAd == null || isAdsOverdue())

    fun isCanShowAds(): Boolean =
        !isLoading && !isShowing && !isInDelayTime() && mInterstitialAd == null && !isAdsOverdue()

    fun isInDelayTime(): Boolean = System.currentTimeMillis() - lastTimeShowAds < TIME_DELAY

    private fun isCanShowAdsIgnoreDelay(): Boolean =
        !isLoading && !isShowing && mInterstitialAd == null && !isAdsOverdue()

    private fun isAdsOverdue(): Boolean {
        val dateDifference = Date().time - loadTimeAd
        return dateDifference > 4 * 3600000L
    }

//    fun showAdsSplash(activity: Activity, callback: () -> Unit) {
//        try {
//            val prefs =
//                com.ezt.v2.ezt.admobdemo.ads.helper.Prefs(com.ezt.v2.ezt.admobdemo.App.Companion.getInstance())
//            if (com.ezt.v2.ezt.admobdemo.ads.helper.Prefs.premium || com.ezt.v2.ezt.admobdemo.ads.helper.Prefs.isRemoveAd) {
//                callback.invoke()
//                return
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        if (isCanShowAds()) {
//            try {
//                showDialog(activity)
//                Handler(Looper.getMainLooper()).postDelayed(
//                    { showAdsFull(activity, callback) },
//                    800
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//                dismissAdDialog()
//                callback.invoke()
//            }
//        } else {
//            callback.invoke()
//        }
//    }

    fun showAds(
        activity: FragmentActivity,
        callback: () -> Unit,
        needLoadAfterShow: Boolean = true
    ) {
//        try {
//            val prefs =
//                com.ezt.v2.ezt.admobdemo.ads.helper.Prefs(com.ezt.v2.ezt.admobdemo.App.Companion.getInstance())
//            if (com.ezt.v2.ezt.admobdemo.ads.helper.Prefs.premium || com.ezt.v2.ezt.admobdemo.ads.helper.Prefs.isRemoveAd) {
//                callback.invoke()
//                return
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        if (isCanShowAds()) {
            try {
                showAdsFull(activity, callback, needLoadAfterShow = needLoadAfterShow)
            } catch (e: Exception) {
                e.printStackTrace()
                callback.invoke()

            }
        } else {
            callback.invoke()

        }
    }

    private fun showDialog(activity: Activity) {
        try {
            if (loadingDialog == null) {
                loadingDialog = Dialog(activity).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(R.layout.ads_dialog_loading)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    window?.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            }
            if (!activity.isDestroyed && !activity.isFinishing && loadingDialog?.isShowing == false) {
                loadingDialog?.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

//    fun loadAndShowAds(activity: FragmentActivity, callback: () -> Unit) {
//        try {
//            val prefs =
//                com.ezt.v2.ezt.admobdemo.ads.helper.Prefs(com.ezt.v2.ezt.admobdemo.App.Companion.getInstance())
//            if (com.ezt.v2.ezt.admobdemo.ads.helper.Prefs.premium || com.ezt.v2.ezt.admobdemo.ads.helper.Prefs.isRemoveAd) {
//                callback.invoke()
//                return
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        dismissAdDialog()
//        val canLoadAndShowAds = !isShowing && !isInDelayTime()
//        if (canLoadAndShowAds) {
//            showDialog(activity)
//            if (isLoading) {
//                adObserver.observe(activity) { interAd ->
//                    if (interAd == null) {
//                        if (!isLoading) {
//                            dismissAdDialog()
//                            callback.invoke()
//                        }
//                    } else {
//                        dismissAdDialog()
//                        showAds(activity, callback)
//                    }
//                }
//            } else {
//                initInterAds(activity) {
//                    dismissAdDialog()
//                    showAds(activity, callback,false)
//                }
//            }
//        } else {
//            callback.invoke()
//        }
//
//    }

    fun dismissAdDialog() {
        loadingDialog?.takeIf { it.isShowing }?.dismiss()
    }

    private fun showAdsFull(
        context: Activity,
        callback: () -> Unit,
        needLoadAfterShow: Boolean = true
    ) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mInterstitialAd = null
                isShowing = false
                callback.invoke()
                dismissAdDialog()
            }

            override fun onAdShowedFullScreenContent() {
                dismissAdDialog()
                isShowing = true
            }

            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                mInterstitialAd = null
                startDelay()
                if (needLoadAfterShow) {
                    initInterAds(context) {

                    }
                }

                callback.invoke()
            }
        }
        mInterstitialAd?.show(context)
    }

    fun isShowing(): Boolean = isShowing

    private var lastTimeShowAds = 0L

    fun startDelay() {
        lastTimeShowAds = System.currentTimeMillis()
    }

    interface Callback {
        fun callback()
    }
}
