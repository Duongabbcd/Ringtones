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
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdmobUtils
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.countClickCallscreen
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.countClickRingtone
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.countClickWallpaper
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.isTestDevice
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.helper.Prefs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Date

object InterAds {

    val TIME_DELAY = if (BuildConfig.DEBUG) 10_000L else 60_000L
    const val INTER_LANGUAGE = "ca-app-pub-8048589936179473/1006114778"
    const val INTER_RINGTONE = "ca-app-pub-8048589936179473/1006114778"
    const val INTER_WALLPAPER = "ca-app-pub-8048589936179473/1006114778"
    const val INTER_CALLSCREEN = "ca-app-pub-8048589936179473/1006114778"
    const val INTER_DOWNLOAD = "ca-app-pub-8048589936179473/1006114778"

    private const val INTER_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val INTER_ID_DEFAULT = "your-ad-id"

    const val ALIAS_INTER_LANGUAGE = "alias_inter_language"
    const val ALIAS_INTER_DOWNLOAD = "alias_inter_download"
    const val ALIAS_INTER_RINGTONE = "alias_inter_ringtone"
    const val ALIAS_INTER_WALLPAPER = "alias_inter_wallpaper"
    const val ALIAS_INTER_CALLSCREEN = "alias_inter_callscreen"

    private var adObserver = MutableLiveData<InterstitialAd?>(null)

    private var mInterstitialAd: InterstitialAd?
        get() = adObserver.value
        set(value) {
            adObserver.value = value
        }

    var interPreloadMap = mutableMapOf<String, MutableLiveData<InterAdWrapper>>()

    class InterAdWrapper(var intersAd: InterstitialAd?) {
        var state: Int = -1
        var adId: String? = null
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
                                val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
                                adRevenue.setRevenue(
                                    adValue.valueMicros / 1_000_000.0,
                                    adValue.currencyCode
                                )
                                Adjust.trackAdRevenue(adRevenue)
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

    fun initInterSelectedAds(ac: Context, interAdWrapper: InterAdWrapper, callback: () -> Unit) {
        val inputValue = interAdWrapper.adId ?: INTER_ID_DEFAULT
        println("interAdWrapper 123: ${interAdWrapper.intersAd} ${interAdWrapper.adId} ${interAdWrapper.state} and $inputValue")
        if (isCanLoadAds()) {
            mInterstitialAd = null
            isLoading = true
            InterstitialAd.load(
                ac,
                if (BuildConfig.DEBUG) INTER_TEST_ID else inputValue,
                getAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {

                        isLoading = false

//                        mInterstitialAd = interstitialAd
//                        mInterstitialAd?.onPaidEventListener = OnPaidEventListener { adValue ->
//                            try {
//                                MyApplication.Companion.initROAS(
//                                    adValue.valueMicros,
//                                    adValue.currencyCode
//                                )
//                                val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
//                                adRevenue.setRevenue(
//                                    adValue.valueMicros / 1_000_000.0,
//                                    adValue.currencyCode
//                                )
//                                Adjust.trackAdRevenue(adRevenue)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
                        loadTimeAd = Date().time
                        println("initInterSelectedAds: onAdLoaded")
                        callback.invoke()
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoading = false
                        mInterstitialAd = null
                        println("initInterSelectedAds: onAdFailedToLoad 12345")
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

    fun isCanShowAds(): Boolean {
        println("isCanShowAds: isLoading $isLoading  isShowing $isShowing isInDelayTime() ${isInDelayTime()} and ${mInterstitialAd == null}")
        val result = !isLoading && !isShowing && !isInDelayTime() && mInterstitialAd != null
        return result
    }

    fun isCanShowAds2(interAdWrapper: InterAdWrapper): Boolean {
        println("isCanShowAds: isLoading $isLoading  isShowing $isShowing  and ${interAdWrapper.intersAd != null}")
        val result = !isLoading && !isShowing && interAdWrapper.intersAd != null
        return result
    }


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

    fun showAds(
        activity: FragmentActivity,
        callback: () -> Unit,
        needLoadAfterShow: Boolean = true
    ) {
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

    fun showSelectedAds(
        activity: FragmentActivity,
        interAdWrapper: InterAdWrapper,
        callback: () -> Unit,
        needLoadAfterShow: Boolean = true
    ) {
        println("showSelectedAds: ${activity.isDestroyed} and ${activity.isFinishing}")

        try {
            val prefs = Prefs(MyApplication.Companion.getInstance())
            if (prefs.premium || prefs.isRemoveAd) {
                callback.invoke()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("isCanShowAds: ${isCanShowAds2(interAdWrapper)}")
        if (isCanShowAds2(interAdWrapper)) {
            println("isCanShowAds: ${interAdWrapper.state} and ${interAdWrapper.adId}")
            try {
                showSelectedAdsFull(
                    activity,
                    interAdWrapper,
                    callback,
                    needLoadAfterShow = needLoadAfterShow
                )
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
                    showAds(activity, callback, false)
                }
            }
        } else {
            callback.invoke()
        }

    }

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

    private fun showSelectedAdsFull(
        context: Activity,
        interAdWrapper: InterAdWrapper,
        callback: () -> Unit,
        needLoadAfterShow: Boolean = true
    ) {
        interAdWrapper.intersAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interAdWrapper.intersAd = null
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
                println("onAdDismissedFullScreenContent is here")
                isShowing = false
                interAdWrapper.intersAd = null
                startDelay()

                callback.invoke()
                // Preload next ad asynchronously
                Handler(Looper.getMainLooper()).post {
                    if (needLoadAfterShow) {
                        println("Ad preloaded after screen transition: $currentAlias and $currentAdUnit")
                        preloadInterAds(context, currentAlias, currentAdUnit)
                    } else {
                        println("Ad preloaded after screen transition: $needLoadAfterShow")
                    }
                }


            }
        }
        interAdWrapper.intersAd?.show(context)
    }

    fun isShowing(): Boolean = isShowing

    private var lastTimeShowAds = 0L

    fun startDelay() {
        lastTimeShowAds = System.currentTimeMillis()
    }

    private var currentAlias = ""
    private var currentAdUnit = ""

    fun preloadInterAds(context: Activity, alias: String, adUnit: String) {
        currentAlias = alias
        currentAdUnit = adUnit
        val mutableLiveData = MutableLiveData(InterAdWrapper(null))
        interPreloadMap[alias] = mutableLiveData

        val wrapper = interPreloadMap[alias]?.value
        if (Prefs(MyApplication.getInstance()).premium || Prefs(MyApplication.getInstance()).isRemoveAd) {
            wrapper?.state = -1
            interPreloadMap[alias]?.postValue(wrapper)
            return
        }

        val interAdId = adUnit
        wrapper?.apply {
            state = 0
            adId = interAdId
        }
        interPreloadMap[alias]?.postValue(wrapper)

        InterstitialAd.load(
            context,
            if (BuildConfig.DEBUG) INTER_TEST_ID else interAdId,
            getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interAd: InterstitialAd) {
                    interAd.onPaidEventListener = OnPaidEventListener { adValue ->
                        try {
                            MyApplication.initROAS(adValue.valueMicros, adValue.currencyCode)
                            val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
                            adRevenue.setRevenue(
                                adValue.valueMicros / 1_000_000.0,
                                adValue.currencyCode
                            )
                            Adjust.trackAdRevenue(adRevenue)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    wrapper?.apply {
                        this.intersAd = interAd
                        this.state = 1
                    }
                    interPreloadMap[alias]?.postValue(wrapper)
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    wrapper?.state = -1
                    interPreloadMap[alias]?.postValue(wrapper)
                }
            }
        )
    }

    fun showPreloadInter(
        activity: FragmentActivity,
        alias: String,
        onLoadDone: (() -> Unit)? = null,
        onLoadFailed: (() -> Unit)? = null,
    ): Boolean {
        when (alias) {
            ALIAS_INTER_DOWNLOAD -> {
                if (RemoteConfig.INTER_DOWNLOAD == "0" || isTestDevice) {
                    onLoadFailed?.invoke()
                    return false
                }
            }

            ALIAS_INTER_LANGUAGE -> {
                if (RemoteConfig.INTER_LANGUAGE == "0" || isTestDevice) {
                    onLoadFailed?.invoke()
                    return false
                }
            }

            ALIAS_INTER_RINGTONE -> {
                if (RemoteConfig.INTER_RINGTONE == "0" || isTestDevice) {
                    onLoadFailed?.invoke()
                    return false
                }
                countClickRingtone++
                if (countClickRingtone % RemoteConfig.INTER_RINGTONE.toInt() != 0) {
                    onLoadFailed?.invoke()
                    return false
                }
            }

            ALIAS_INTER_WALLPAPER -> {
                if (RemoteConfig.INTER_WALLPAPER == "0" || isTestDevice) {
                    onLoadFailed?.invoke()
                    return false
                }
                countClickWallpaper++
                if (countClickWallpaper % RemoteConfig.INTER_WALLPAPER.toInt() != 0) {
                    onLoadFailed?.invoke()
                    return false
                }
            }

            ALIAS_INTER_CALLSCREEN -> {
                if (RemoteConfig.INTER_CALLSCREEN == "0" || isTestDevice) {
                    onLoadFailed?.invoke()
                    return false
                }
                countClickCallscreen++
                if (countClickCallscreen % RemoteConfig.INTER_CALLSCREEN.toInt() != 0) {
                    onLoadFailed?.invoke()
                    return false

                }
            }
        }

        if (!AdmobUtils.isNetworkConnected(activity)) {
            onLoadFailed?.invoke()
            return false
        }

        if (AdsManager.isTestDevice) {
            onLoadFailed?.invoke()
            return false
        }

        if (Prefs(MyApplication.getInstance()).premium || Prefs(MyApplication.getInstance()).isRemoveAd) {
            return false
        }

        println("showPreloadInter $alias ----- ${interPreloadMap[alias]}")
        val ob = interPreloadMap[alias] ?: return false

        ob.observe(activity) { wrapper ->
            println("interPreloadMap[alias]: $alias AND $${wrapper.intersAd} ${wrapper.adId} and ${wrapper.state}")
            try {
                if (activity.isDestroyed || activity.isFinishing) {
                    ob.removeObservers(activity)
                    return@observe
                }
                wrapper?.let {
                    if (it.state == 1) {
                        ob.removeObservers(activity)

                        it.intersAd?.let { ads ->
                            println("interPreloadMap[alias]: ${activity.isDestroyed} and ${activity.isFinishing}")
                            showSelectedAds(
                                activity,
                                wrapper,
                                { onLoadDone?.invoke() },
                                needLoadAfterShow = true
                            )
                        }

                        if (it.intersAd == null) {
                            onLoadFailed?.invoke()
                        }

                    } else if (it.state == -1) {
                        ob.removeObservers(activity)
                        onLoadFailed?.invoke()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        return true
    }

    interface Callback {
        fun callback()
    }
}