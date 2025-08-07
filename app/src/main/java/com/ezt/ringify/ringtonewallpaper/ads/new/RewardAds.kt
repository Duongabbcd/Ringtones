package com.ezt.ringify.ringtonewallpaper.ads.new

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.ezt.ringify.ringtonewallpaper.ads.helper.Prefs
import com.ezt.ringify.ringtonewallpaper.R

object RewardAds {

    private const val REWARDED_INTER_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val REWARDED_INTER_ID_DEFAULT = "your_id_here"
    private val TAG = RewardAds::class.java.canonicalName

    private var mRewardAds: RewardedAd? = null
    private var isLoading = false
    private var isShowing = false

    var mLoadingDialog: Dialog? = null

    fun initRewardAds(context: Context) {
        if (!isCanLoadAds()) return

        mRewardAds = null
        isLoading = true

        RewardedAd.load(
            context,
            if (BuildConfig.DEBUG) REWARDED_INTER_TEST_ID else REWARDED_INTER_ID_DEFAULT,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Load OK")
                    mRewardAds = ad
                    ad.setOnPaidEventListener { adValue ->
                        try {
                            MyApplication.initROAS(adValue.valueMicros, adValue.currencyCode)
                            val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB).apply {
                                setRevenue(adValue.valueMicros / 1_000_000.0, adValue.currencyCode)
                            }
                            Adjust.trackAdRevenue(adRevenue)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.d(TAG, error.toString())
                    mRewardAds = null
                    isLoading = false
                }
            }
        )
    }

    private fun getAdRequest(): AdRequest = AdRequest.Builder().build()

    private fun isCanLoadAds(): Boolean = !isLoading && !isShowing

    private fun isCanShowAds(): Boolean {
        if (isLoading || isShowing) return false
        Log.e(TAG, "mInterstitialAd == null")
        return mRewardAds != null
    }

    fun showAds(activity: Activity, callback: RewardCallback) {
        MyApplication.trackingEvent("user_get_reward")
        try {
            val isPro = Prefs(activity).premium
            val isSub = Prefs(activity).isRemoveAd
            if (isPro || isSub) {
                callback.onPremium()
                Log.e(TAG, "pro/subbed")
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isCanShowAds()) {
            try {
                showAdsFull(activity, callback)
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onAdFailedToShow()
            }
        } else {
            callback.onAdFailedToShow()
        }
    }

    private fun showAdsFull(context: Activity, callback: RewardCallback) {
        mRewardAds?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, adError.message ?: "Ad failed to show")
                mRewardAds = null
                isShowing = false
                callback.onAdFailedToShow()
            }

            override fun onAdShowedFullScreenContent() {
                isShowing = true
                callback.onAdShowed()
            }

            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                mRewardAds = null
                initRewardAds(context)
                callback.onAdDismiss()
            }
        }

        mRewardAds?.show(context) {
            callback.onEarnedReward()
        }
    }

    fun isShowing(): Boolean = isShowing

    fun loadAndShowAds(context: Activity, callback: RewardCallback) {
        try {
            val isPro = Prefs(context).premium
            val isSub = Prefs(context).isRemoveAd
            if (isPro || isSub) {
                callback.onPremium()
                Log.e(TAG, "pro/subbed")
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (isCanLoadAds() && mRewardAds == null) {
            mRewardAds = null
            isLoading = true
            mLoadingDialog = createLoadingDialog(context)
            mLoadingDialog?.show()

            RewardedAd.load(
                context,
                if (BuildConfig.DEBUG) REWARDED_INTER_TEST_ID else REWARDED_INTER_ID_DEFAULT,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Load OK")
                        mRewardAds = ad
                        ad.setOnPaidEventListener { adValue ->
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
                        isLoading = false

                        mRewardAds?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                dismissAdsDialog()
                                Log.e(TAG, adError.message ?: "Ad failed to show")
                                mRewardAds = null
                                isShowing = false
                                callback.onAdFailedToShow()
                            }

                            override fun onAdShowedFullScreenContent() {
                                dismissAdsDialog()
                                isShowing = true
                                callback.onAdShowed()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                isShowing = false
                                mRewardAds = null
                                initRewardAds(context)
                                callback.onAdDismiss()
                            }
                        }

                        mRewardAds?.show(context) {
                            callback.onEarnedReward()
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        dismissAdsDialog()
                        Log.d(TAG, error.toString())
                        mRewardAds = null
                        isLoading = false
                    }
                }
            )
        } else if (mRewardAds != null) {
            showAds(context, callback)
        } else {
            callback.onAdFailedToShow()
        }
    }

    private fun createLoadingDialog(context: Activity): Dialog {
        return Dialog(context).apply {
            setContentView(R.layout.ads_dialog_loading)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
        }
    }

    fun dismissAdsDialog() {
        mLoadingDialog?.dismiss()
    }

    interface RewardCallback {
        fun onAdShowed()
        fun onAdDismiss()
        fun onAdFailedToShow()
        fun onEarnedReward()
        fun onPremium()
    }
}