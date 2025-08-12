package com.ezt.ringify.ringtonewallpaper.ads.new

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlin.apply
import com.ezt.ringify.ringtonewallpaper.R
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.MyApplication
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.helper.Prefs
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError


@SuppressLint("StaticFieldLeak")
object BannerAds {

    private const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/9214589741"
    private val BANNER_ID_DEFAULT = AdsManager.BANNER_HOME
    private const val BANNER_ID_COLLAPSIBLE = "ad-id-here"
    private const val BANNER_HOME_COLLAPSIBLE = "ad-id-here"

    private var isInitBanner = false

    fun getAdSize(activity: Activity): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val adWidth = (outMetrics.widthPixels / outMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            activity,
            adWidth
        )
    }


    private fun getAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    fun initBannerAdsHome(ctx: Activity) {
        initBannerAds(ctx, BANNER_HOME_COLLAPSIBLE)
    }

    fun initBannerAds(ctx: Activity, adUnitId: String = BANNER_ID_DEFAULT) {
        try {
            val adBanner: ViewGroup? = ctx.findViewById(R.id.frBanner)
            val prefs = Prefs(MyApplication.getInstance())
            println("initBannerAds: ${prefs.premium} and ${prefs.isRemoveAd}")
            if (prefs.premium || prefs.isRemoveAd) {
                adBanner?.visibility = View.GONE
                return
            }

            val adViewContainer: LinearLayout = ctx.findViewById(R.id.adView_container) ?: return
            val mAdViewBanner = AdView(ctx)
            mAdViewBanner.setAdSize(getAdSize(ctx))
            mAdViewBanner.adUnitId = if (BuildConfig.DEBUG) BANNER_TEST_ID else adUnitId

            val adRequest = if (false) {
                val extras = Bundle().apply { putString("collapsible", "bottom") }
                AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
            } else {
                AdRequest.Builder().build()
            }


            adViewContainer.removeAllViews()
            adViewContainer.addView(mAdViewBanner)
            mAdViewBanner.loadAd(adRequest)
            mAdViewBanner.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    println("onAdLoaded: is here")
//                    mAdViewBanner.setOnPaidEventListener { adValue ->
//                        try {
//                            MyApplication.initROAS(adValue.valueMicros, adValue.currencyCode)
//                            val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
//                            adRevenue.setRevenue(
//                                adValue.valueMicros / 1_000_000.0,
//                                adValue.currencyCode
//                            )
//                            Adjust.trackAdRevenue(adRevenue)
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
                    adViewContainer.visibility = View.VISIBLE
                    hideBannerLoading(ctx, false)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adViewContainer.visibility = View.GONE
                    println("onAdFailedToLoad: $loadAdError")
                    hideBannerLoading(ctx, true)
                }

                override fun onAdClicked() {
                    adViewContainer.visibility = View.GONE
                    println("onAdClicked: is here")
                    hideBannerLoading(ctx, true)
                }
            }

            hideBannerLoading(ctx, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initBannerAds(ctx: Activity, adBanner: ViewGroup?, adUnitId: String = BANNER_ID_DEFAULT) {
        try {
            val prefs = Prefs(MyApplication.getInstance())
            if (prefs.premium || prefs.isRemoveAd) {
                adBanner?.visibility = View.GONE
                return
            }

            if (adBanner == null) {
                return
            }

            val adViewContainer: LinearLayout =
                adBanner.findViewById(R.id.adView_container) ?: return
            val mAdViewBanner = AdView(ctx)
            mAdViewBanner.setAdSize(getAdSize(ctx))
            mAdViewBanner.adUnitId = if (BuildConfig.DEBUG) BANNER_TEST_ID else adUnitId

            val adRequest = if (false) {
                val extras = Bundle().apply { putString("collapsible", "bottom") }
                AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
            } else {
                AdRequest.Builder().build()
            }


            adViewContainer.removeAllViews()
            adViewContainer.addView(mAdViewBanner)
            mAdViewBanner.loadAd(adRequest)
            mAdViewBanner.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    mAdViewBanner.setOnPaidEventListener { adValue ->
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
                    adViewContainer.visibility = View.VISIBLE
                    hideBannerLoading(adBanner, false)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adViewContainer.visibility = View.GONE
                    hideBannerLoading(adBanner, true)
                }

                override fun onAdClicked() {
                    adViewContainer.visibility = View.GONE
                    hideBannerLoading(adBanner, true)
                }
            }

            hideBannerLoading(adBanner, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideBannerLoading(bannerAd: ViewGroup, bl: Boolean) {
        try {
            val tvBannerLoading: com.facebook.shimmer.ShimmerFrameLayout =
                bannerAd.findViewById(R.id.shimmer_layout)
            val visibility = if (bl) View.GONE else View.VISIBLE
            tvBannerLoading.visibility = visibility
            bannerAd.findViewById<View>(R.id.view_d).visibility = visibility
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideBannerLoading(ctx: Activity, bl: Boolean) {
        try {
            val tvBannerLoading: com.facebook.shimmer.ShimmerFrameLayout =
                ctx.findViewById(R.id.shimmer_layout)
            val visibility = if (bl) View.GONE else View.VISIBLE
            tvBannerLoading.visibility = visibility
            ctx.findViewById<View>(R.id.view_d).visibility = visibility
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}