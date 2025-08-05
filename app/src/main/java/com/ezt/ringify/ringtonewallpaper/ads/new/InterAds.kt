package com.ezt.ringify.ringtonewallpaper.ads.new

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
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.helper.Prefs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

object InterAds {

    val TIME_DELAY = if (BuildConfig.DEBUG) 10_000L else 60_000L
    private const val INTER_LANGUAGE = ""
    private const val INTER_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val INTER_ID_DEFAULT = "your-ad-id"

    private var adObserver = MutableLiveData<InterstitialAd?>(null)

    private var mInterstitialAd: InterstitialAd?
        get() = adObserver.value
        set(value) {
            adObserver.value = value
        }

    var interPreloadMap = mutableMapOf<String, MutableLiveData<InterAdWrapper>>()

    class InterAdWrapper(var intersAd: InterstitialAd?) {
        var state : Int = -1
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
//                                val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
//                                adRevenue.setRevenue(
//                                    adValue.valueMicros / 1_000_000.0,
//                                    adValue.currencyCode
//                                )
//                                Adjust.trackAdRevenue(adRevenue)
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

    fun initInterLanguage(ac: Context, callback: Callback?) {
        if (isCanLoadAds()) {
            isLoading = true
            mInterstitialAd = null
            InterstitialAd.load(
                ac,
                if (BuildConfig.DEBUG) INTER_TEST_ID else INTER_LANGUAGE,
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
//                                val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
//                                adRevenue.setRevenue(
//                                    adValue.valueMicros / 1_000_000.0,
//                                    adValue.currencyCode
//                                )
//                                Adjust.trackAdRevenue(adRevenue)
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
        !isLoading && !isShowing && !isInDelayTime() && mInterstitialAd != null && !isAdsOverdue()

    fun isInDelayTime(): Boolean = System.currentTimeMillis() - lastTimeShowAds < TIME_DELAY

    private fun isCanShowAdsIgnoreDelay(): Boolean =
        !isLoading && !isShowing && mInterstitialAd != null && !isAdsOverdue()

    private fun isAdsOverdue(): Boolean {
        val dateDifference = Date().time - loadTimeAd
        return dateDifference > 4 * 3600000L
    }

    fun showAdsLanguage(activity: Activity, callback: () -> Unit) {
        try {
            val prefs = Prefs(MyApplication.Companion.getInstance())
            if (prefs.premium || prefs.isRemoveAd) {
                callback.invoke()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isCanShowAds()) {
            try {
//                showDialog(activity)
                showAdsFull(activity, callback)
            } catch (e: Exception) {
                e.printStackTrace()
//                dismissAdDialog()
                callback.invoke()
            }
        } else {
            callback.invoke()
        }
    }

    fun showAds(activity: FragmentActivity, callback: () -> Unit, needLoadAfterShow : Boolean = true) {
        try {
            val prefs = Prefs(MyApplication.Companion.getInstance())
            if (prefs.premium || prefs.isRemoveAd) {
                callback.invoke()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        } catch (e : Exception) {
            e.printStackTrace()
        }

    }

    fun loadAndShowAds(activity: FragmentActivity, callback: () -> Unit) {
        try {
            val prefs = Prefs(MyApplication.Companion.getInstance())
            if (prefs.premium || prefs.isRemoveAd) {
                callback.invoke()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        dismissAdDialog()
        val canLoadAndShowAds = !isShowing && !isInDelayTime()
        if (canLoadAndShowAds) {
            showDialog(activity)
            if (isLoading) {
                adObserver.observe(activity) { interAd ->
                    if (interAd == null) {
                        if (!isLoading) {
                            dismissAdDialog()
                            callback.invoke()
                        }
                    } else {
                        dismissAdDialog()
                        showAds(activity, callback)
                    }
                }
            } else {
                initInterAds(activity) {
                    dismissAdDialog()
                    showAds(activity, callback,false)
                }
            }
        } else {
            callback.invoke()
        }

    }

    fun dismissAdDialog() {
        loadingDialog?.takeIf { it.isShowing }?.dismiss()
    }

    private fun showAdsFull(context: Activity, callback: () -> Unit, needLoadAfterShow : Boolean = true) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                mInterstitialAd = null
                isShowing = false
                callback.invoke()
                println("onAdFailedToShowFullScreenContent is here")
//                dismissAdDialog()
            }

            override fun onAdShowedFullScreenContent() {
//                dismissAdDialog()
                println("onAdShowedFullScreenContent is here")
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

    fun preloadInterAds(activity: Activity, alias : String, adId : String = INTER_ID_DEFAULT) {

        val mutableLiveData = MutableLiveData(InterAdWrapper(null))
        interPreloadMap[alias] = mutableLiveData

        val wrapper = interPreloadMap[alias]?.value
        if (Prefs(MyApplication.getInstance()).premium || Prefs(MyApplication.getInstance()).isRemoveAd) {
            wrapper?.state = -1
            interPreloadMap[alias]?.postValue(wrapper)
            return
        }

        val nativeId = INTER_ID_DEFAULT
        wrapper?.state = 0
        interPreloadMap[alias]?.postValue(wrapper)

        val adLoader = AdLoader.Builder(activity, if (BuildConfig.DEBUG) INTER_TEST_ID else nativeId)
            .forNativeAd { nativeAd ->
                nativeAd.setOnPaidEventListener { adValue ->
                    try {
                        MyApplication.initROAS(adValue.valueMicros, adValue.currencyCode)
//                        val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB).apply {
//                            setRevenue(adValue.valueMicros / 1_000_000.0, adValue.currencyCode)
//                        }
//                        Adjust.trackAdRevenue(adRevenue)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                wrapper?.apply {
                    this.intersAd = null
                    this.state = 1
                }
                interPreloadMap[alias]?.postValue(wrapper)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    wrapper?.state = -1
                    interPreloadMap[alias]?.postValue(wrapper)
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

//    fun showPreloadInter(
//        activity: FragmentActivity,
//        alias: String,
//        nativeView: ViewGroup,
//        onLoadDone: (() -> Unit)? = null,
//        onLoadFailed : (() -> Unit)? = null,
//        layoutResId : Int = R.layout.ads_native_large, ): Boolean {
//
//        if (Prefs(MyApplication.getInstance()).premium || Prefs(MyApplication.getInstance()).isRemoveAd) {
//            nativeView.visibility = View.GONE
//            return false
//        }
//
//        val ob = interPreloadMap[alias]?: return false
//
//        ob.observe(activity) { wrapper ->
//            try {
//                if (activity.isDestroyed || activity.isFinishing){
//                    ob.removeObservers(activity)
//                    return@observe
//                }
//                wrapper?.let {
//                    if (it.state == 1) {
//                        ob.removeObservers(activity)
//
//                        it.intersAd?.let { ads ->
//                            val materialCardView = activity.layoutInflater.inflate(layoutResId, null) as FrameLayout
//                            val adView = materialCardView.findViewById<NativeAdView>(R.id.uniform)
//                            populateNativeAdView(ads, adView)
//                            nativeView.removeAllViews()
//                            nativeView.addView(materialCardView)
//                            onLoadDone?.invoke()
//                        }
//
//                        if (it.intersAd == null) {
//                            onLoadFailed?.invoke()
//                        }
//
//                    } else if (it.state == -1) {
//                        ob.removeObservers(activity)
//                        onLoadFailed?.invoke()
//                    }
//                }
//            } catch (e : Exception) {
//                e.printStackTrace()
//            }
//
//        }
//
//
//        return true
//    }

    interface Callback {
        fun callback()
    }
}