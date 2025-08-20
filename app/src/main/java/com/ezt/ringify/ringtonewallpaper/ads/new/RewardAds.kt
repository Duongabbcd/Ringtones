package com.ezt.ringify.ringtonewallpaper.ads.new

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.helper.Prefs
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object RewardAds {

    private const val REWARDED_INTER_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val REWARDED_INTER_ID_DEFAULT = "ca-app-pub-8048589936179473/2130095382"
    private val TAG = RewardAds::class.java.canonicalName

    private var mRewardAds: RewardedAd? = null
    private var isLoading = false
    private var isShowing = false

    var mLoadingDialog: Dialog? = null


    private fun getAdRequest(): AdRequest = AdRequest.Builder().build()


    private fun isCanShowAds(): Boolean {
        Log.d(TAG, "isLoading $isLoading and isShowing $isShowing")
//        if (isLoading || isShowing) return false
        Log.e(TAG, "mInterstitialAd == null")
        return mRewardAds != null
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
                                initRewardAds(context)
                                callback.onAdShowed()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                isShowing = false
                                mRewardAds = null
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

    fun showAds(activity: Activity, callback: RewardCallback) {
        val ad = mRewardAds
        if (ad != null) {
            // Ad is ready — show immediately
            ad.show(activity) { rewardItem ->
                Log.d("RewardAds", "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                callback.onEarnedReward()
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    callback.onAdShowed()
                }

                override fun onAdDismissedFullScreenContent() {
                    mRewardAds = null // Release reference
                    callback.onAdDismiss()
                    initRewardAds(activity.applicationContext) // Preload next ad
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mRewardAds = null
                    callback.onAdFailedToShow()
                }
            }
        } else {
            // Ad not ready — try loading, then show
            Log.d("RewardAds", "Ad not ready, loading first...")
            initRewardAds(activity.applicationContext)

            // Small delay to give loading a chance; ideally you’d chain the load callback
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (mRewardAds != null) {
                    showAds(activity, callback) // Try again
                } else {
                    callback.onAdFailedToShow()
                }
            }, 1500)
        }
    }

    fun initRewardAds(context: Context) {
        if (RemoteConfig.ADS_DISABLE == "0" || RemoteConfig.REWARD_ADS == "0") return
        if (mRewardAds != null || isLoading || !isCanLoadAds()) return

        isLoading = true
        RewardedAd.load(
            context,
            if (BuildConfig.DEBUG) REWARDED_INTER_TEST_ID else REWARDED_INTER_ID_DEFAULT,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardAds", "Load OK")
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
                    Log.d("RewardAds", "onAdFailedToLoad: $error")
                    mRewardAds = null
                    isLoading = false
                }
            }
        )
    }

    private fun isCanLoadAds(): Boolean {
        // your logic here
        return RemoteConfig.REWARD_ADS != "0"
    }

    interface RewardCallback {
        fun onAdShowed()
        fun onAdDismiss()
        fun onAdFailedToShow()
        fun onEarnedReward()
        fun onPremium()
    }
}