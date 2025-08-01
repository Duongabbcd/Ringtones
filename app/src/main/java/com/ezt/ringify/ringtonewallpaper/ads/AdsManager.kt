package com.ezt.ringify.ringtonewallpaper.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.admob.max.dktlibrary.AdmobUtils
import com.admob.max.dktlibrary.AdmobUtils.NativeAdCallbackNew
import com.admob.max.dktlibrary.AdmobUtils.checkAdsTest
import com.admob.max.dktlibrary.AppOpenManager
import com.admob.max.dktlibrary.CollapsibleBanner
import com.admob.max.dktlibrary.GoogleENative
import com.admob.max.dktlibrary.utils.admod.BannerHolderAdmob
import com.admob.max.dktlibrary.utils.admod.InterHolderAdmob
import com.admob.max.dktlibrary.utils.admod.NativeHolderAdmob
import com.admob.max.dktlibrary.utils.admod.callback.AdsInterCallBack
import com.admob.max.dktlibrary.utils.admod.callback.NativeAdmobCallback
import com.applovin.sdk.AppLovinSdkUtils
import com.ezt.ringify.ringtonewallpaper.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.nativead.NativeAd

object AdsManager {
    var isDebug = true
    var isTestDevice = true

    var AOA_SPLASH = ""
    var INTER_SPLASH = InterHolderAdmob("")
    var BANNER_SPLASH = ""
    var NATIVE_SPLASH = NativeHolderAdmob("")
    var NATIVE_FULL_SPLASH = NativeHolderAdmob("")

    var NATIVE_LANGUAGE = NativeHolderAdmob("")
    var NATIVE_LANGUAGE_ID2 = NativeHolderAdmob("")
    var NATIVE_INTRO = NativeHolderAdmob("")
    var NATIVE_FULL_SCREEN_INTRO = NativeHolderAdmob("")

    var INTER_INTRO = InterHolderAdmob("")
    var BANNER_HOME = ""
    var BANNER_COLLAP_HOME = BannerHolderAdmob("")
    var NATIVE_COLLAP_HOME = NativeHolderAdmob("")
    var NATIVE_CUSTOM_HOME = NativeHolderAdmob("")

    var INTER_HOME = InterHolderAdmob("")
    var INTER_LANGUAGE = InterHolderAdmob("")

    var BANNER_PLAY_SONG = ""
    var BANNER_COLLAP_PLAY_SONG =BannerHolderAdmob("")
    var NATIVE_COLLAP_PLAY_SONG = NativeHolderAdmob("")

    var ONRESUME = ""

    var countClickHome = 0
    var countClickMessageBack = 0
    var countClickItem = 0

//    fun checkAdsTest(ad: NativeAd?) {
//        try {
//            val testAdResponse = ad?.headline.toString().replace(" ", "").split(":")[0]
//            Log.d("===Native", ad?.headline.toString().replace(" ", "").split(":")[0])
//            val testAdResponses = arrayOf(
//                "TestAd",
//                "Anunciodeprueba",
//                "Annuncioditesto",
//                "Testanzeige",
//                "TesIklan",
//                "Anúnciodeteste",
//                "Тестовоеобъявление",
//                "পরীক্ষামূলকবিজ্ঞাপন",
//                "जाँचविज्ञापन",
//                "إعلانتجريبي",
//                "Quảngcáothửnghiệm"
//            )
//            isTestDevice = testAdResponses.contains(testAdResponse)
//        } catch (_: Exception) {
//            isTestDevice = true
//            Log.d("===Native", "Error")
//        }
//        AppOpenManager.getInstance().setTestAds(isTestDevice)
//    }

    fun showAdBanner(activity: Activity, adsEnum: String, view: ViewGroup, line: View, isCheckTestDevice:Boolean= false, isMoveNextScreen : () -> Unit) {
        if(isTestDevice  && isCheckTestDevice) {
            isMoveNextScreen()
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            isMoveNextScreen()
            view.visibility = View.GONE
            line.visibility = View.GONE
            return
        }
        AdmobUtils.loadAdBanner(activity, adsEnum, view, isCheckTestDevice, object :
            AdmobUtils.BannerCallBack {
            override fun onClickAds() {

            }

            override fun onFailed(message: String) {
                Log.d("Fail===", "onFailed: $message")
                isMoveNextScreen()
                view.visibility = View.GONE
                line.visibility = View.GONE
            }

            override fun onLoad() {
                isMoveNextScreen()
            }


            override fun onPaid(adValue: AdValue?, mAdView: AdView?) {
            }

        })
    }

    fun showAdBannerCollapsible(
        activity: Activity,
        adsEnum: BannerHolderAdmob,
        view: ViewGroup,
        line: View,
        isCheckTestDevice:Boolean= false,
    ) {

        if (isTestDevice && isCheckTestDevice) {
            view.visibility = View.GONE
            line.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            view.visibility = View.GONE
            line.visibility = View.GONE
            return
        }
        AdmobUtils.loadAdBannerCollapsibleReload(
            activity,
            adsEnum,
            CollapsibleBanner.BOTTOM,
            view,
            isCheckTestDevice,
            object : AdmobUtils.BannerCollapsibleAdCallback {
                override fun onBannerAdLoaded(
                    adSize: AdSize
                ) {
                    val params: ViewGroup.LayoutParams =
                        view.layoutParams
                    params.height = adSize.getHeightInPixels(activity)
                    view.layoutParams = params
                }

                override fun onClickAds() {

                }

                override fun onAdFail(message: String) {
                    view.visibility = View.GONE
                    line.visibility = View.GONE

                }

                override fun onAdPaid(adValue: AdValue, mAdView: AdView) {
                }
            })
    }


    fun loadAndShowNative(
        activity: Activity,
        nativeAdContainer: ViewGroup,
        isCheckTestDevice:Boolean= false,
        NativeHolderAdmob: NativeHolderAdmob,
        isMoveNextScreen : () -> Unit
    ) {
        if(isTestDevice) {
            isMoveNextScreen()
            return
        }

        if (!AdmobUtils.isNetworkConnected(activity)) {
            isMoveNextScreen()
            nativeAdContainer.visibility = View.GONE
            return
        }
        AdmobUtils.loadAndShowNativeAdsWithLayoutAds(
            activity,
            NativeHolderAdmob,
            nativeAdContainer,
            R.layout.ad_template_medium,
            GoogleENative.UNIFIED_MEDIUM,
            isCheckTestDevice,
            object : NativeAdCallbackNew {
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    println("onLoadedAndGetNativeAd")
                    checkAdsTest(ad = ad)
                }

                override fun onNativeAdLoaded() {
                    println("onNativeAdLoaded")
                    isMoveNextScreen()
                }

                override fun onAdFail(error: String) {
                    isMoveNextScreen()
                    println("onAdFail")
                    nativeAdContainer.visibility = View.GONE
                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                  //do nothing
                }


                override fun onClickAds() {

                }
            })
    }

    fun loadAndShowNative2(
        activity: Activity,
        nativeAdContainer: ViewGroup,
        isCheckTestDevice:Boolean= false,
        NativeHolderAdmob: NativeHolderAdmob,
        isMoveNextScreen : () -> Unit
    ) {
        println("loadAndShowNative2: $isTestDevice and $isCheckTestDevice")
        if(isTestDevice && isCheckTestDevice) {
            return
        }

        if (!AdmobUtils.isNetworkConnected(activity)) {
            nativeAdContainer.visibility = View.GONE
            return
        }
        AdmobUtils.loadAndShowNativeAdsWithLayoutAds(
            activity,
            NativeHolderAdmob,
            nativeAdContainer,
            R.layout.ad_template_medium_custom_home,
            GoogleENative.UNIFIED_MEDIUM,
            isCheckTestDevice,
            object : NativeAdCallbackNew {
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    println("onLoadedAndGetNativeAd")
                    checkAdsTest(ad = ad)
                }

                override fun onNativeAdLoaded() {
                    println("onNativeAdLoaded")
                    isMoveNextScreen()
                }

                override fun onAdFail(error: String) {
                    println("onAdFail $error" )
                    nativeAdContainer.visibility = View.GONE
                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                  //do nothing
                }


                override fun onClickAds() {

                }
            })
    }

    fun loadAndShowNativeCollapsible(
        activity: Activity,
        nativeHolderAdmob: NativeHolderAdmob,
        nativeAdContainer: ViewGroup,
        isCheckTestDevice: Boolean = false,
        isMoveNextScreen : () -> Unit
    ) {
        try {
            if(isTestDevice && isCheckTestDevice) {
                isMoveNextScreen()
                return
            }
            if (!AdmobUtils.isNetworkConnected(activity)) {
                isMoveNextScreen()
                nativeAdContainer.visibility = View.GONE
                return
            }
            val params: ViewGroup.LayoutParams = nativeAdContainer.layoutParams
            params.height = AppLovinSdkUtils.dpToPx(activity,230)
            nativeAdContainer.layoutParams = params

            AdmobUtils.loadAndShowNativeAdsWithLayoutAdsCollapsible(activity, nativeHolderAdmob, nativeAdContainer,
                R.layout.ad_template_mediumcollapsible, GoogleENative.UNIFIED_MEDIUM, isCheckTestDevice,object : NativeAdCallbackNew {
                    override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    }

                    override fun onNativeAdLoaded() {
                        val params: ViewGroup.LayoutParams = nativeAdContainer.layoutParams
                        params.height = AppLovinSdkUtils.dpToPx(activity,330)
                        nativeAdContainer.layoutParams = params

                        isMoveNextScreen()
                    }

                    override fun onAdFail(error: String) {
                        isMoveNextScreen()
                        nativeAdContainer.visibility = View.GONE
                    }

                    override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {

                    }

                    override fun onClickAds() {
                        val params: ViewGroup.LayoutParams = nativeAdContainer.layoutParams
                        params.height = AppLovinSdkUtils.dpToPx(activity,80)
                        nativeAdContainer.layoutParams = params
                    }
                })
        }catch (_:Exception){
            nativeAdContainer.visibility = View.GONE
        }catch (_:OutOfMemoryError){
            nativeAdContainer.visibility = View.GONE
        }
    }

    fun loadAndShowInterSplash(
        context: AppCompatActivity,
        interHolder: InterHolderAdmob,
        isdialog : Boolean,
        callback: AdListenerWithNative,
        isCheckTestDevice: Boolean =false
    ) {
        if (isTestDevice || isCheckTestDevice) {
            callback.onAdClosedOrFailed()
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            callback.onAdClosedOrFailed()
            return
        }
        AppOpenManager.getInstance().isAppResumeEnabled = true
        AdmobUtils.loadAndShowAdInterstitial(context, interHolder, isCheckTestDevice, object : AdsInterCallBack {
            override fun onStartAction() {
            }

            override fun onEventClickAdClosed() {
                callback.onAdClosedOrFailedWithNative()
            }

            override fun onAdShowed() {
                AppOpenManager.getInstance().isAppResumeEnabled = false
                Handler().postDelayed({
                    try {
                        AdmobUtils.dismissAdDialog()
                    } catch (_: Exception) {

                    }
                }, 800)
            }

            override fun onAdLoaded() {

            }

            override fun onAdFail(p0: String?) {
                println("onAdFail 123: $p0")
                callback.onAdClosedOrFailedWithNative()
            }

            override fun onClickAds() {

            }

            override fun onPaid(p0: AdValue?, p1: String?) {

            }
        }, enableLoadingDialog = isdialog)
    }

    fun loadNative(context: Context, nativeHolder: NativeHolderAdmob,  isCheckTestDevice: Boolean =false) {
        if (isTestDevice || isCheckTestDevice) {
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            return
        }
        AdmobUtils.loadAndGetNativeAds(context, nativeHolder, isCheckTestDevice, object : NativeAdmobCallback {
            override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                checkAdsTest(ad)
            }

            override fun onNativeAdLoaded() {

            }

            override fun onAdFail(error: String?) {
                Log.e("Admob", "onAdFail: ${nativeHolder.ads}" + error)
            }

            override fun onPaid(p0: AdValue?, p1: String?) {

            }
        })
    }

    fun showNativeLanguage(activity: Activity, viewGroup: ViewGroup, holder: NativeHolderAdmob,isCheckTestDevice: Boolean =false) {
        if (isTestDevice && isCheckTestDevice) {
            viewGroup.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }

        AdmobUtils.showNativeAdsWithLayout(activity,
            holder,
            viewGroup,
            R.layout.ad_template_medium,
            GoogleENative.UNIFIED_MEDIUM,
            isCheckTestDevice,
            object : AdmobUtils.AdsNativeCallBackAdmod {
                override fun NativeLoaded() {
                    viewGroup.visibility = View.VISIBLE
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun NativeFailed(massage: String) {
                    println("NativeFailed: $massage")
                    AdmobUtils.showNativeAdsWithLayout(activity,
                        holder,
                        viewGroup,
                        R.layout.ad_template_medium,
                        GoogleENative.UNIFIED_MEDIUM,
                        isCheckTestDevice,
                        object : AdmobUtils.AdsNativeCallBackAdmod {
                            override fun NativeLoaded() {
                                viewGroup.visibility = View.VISIBLE
                            }

                            override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                            }

                            override fun NativeFailed(massage: String) {
                                viewGroup.visibility = View.GONE
                            }

                        })
                }

            })
    }
    fun showNativeLanguageCustom(activity: Activity, viewGroup: ViewGroup, holder: NativeHolderAdmob,isCheckTestDevice: Boolean =false) {
        if (isTestDevice || isCheckTestDevice) {
            viewGroup.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }

        AdmobUtils.showNativeAdsWithLayout(activity,
            holder,
            viewGroup,
            R.layout.ad_template_small,
            GoogleENative.UNIFIED_MEDIUM,
            isCheckTestDevice,
            object : AdmobUtils.AdsNativeCallBackAdmod {
                override fun NativeLoaded() {
                    viewGroup.visibility = View.VISIBLE
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun NativeFailed(massage: String) {
                    AdmobUtils.showNativeAdsWithLayout(activity,
                        holder,
                        viewGroup,
                        R.layout.ad_template_medium,
                        GoogleENative.UNIFIED_MEDIUM,
                        isCheckTestDevice,
                        object : AdmobUtils.AdsNativeCallBackAdmod {
                            override fun NativeLoaded() {
                                viewGroup.visibility = View.VISIBLE
                            }

                            override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                            }

                            override fun NativeFailed(massage: String) {
                                viewGroup.visibility = View.GONE
                            }

                        })
                }

            })
    }

    fun showNativeLanguageSmall(activity: Activity, viewGroup: ViewGroup, holder: NativeHolderAdmob,isCheckTestDevice: Boolean =false) {
        if (isTestDevice || isCheckTestDevice) {
            viewGroup.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }

        AdmobUtils.showNativeAdsWithLayout(activity,
            holder,
            viewGroup,
            R.layout.ad_template_small_custom,
            GoogleENative.UNIFIED_SMALL,
            isCheckTestDevice,
            object : AdmobUtils.AdsNativeCallBackAdmod {
                override fun NativeLoaded() {
                    viewGroup.visibility = View.VISIBLE
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun NativeFailed(massage: String) {
                    AdmobUtils.showNativeAdsWithLayout(activity,
                        holder,
                        viewGroup,
                        R.layout.ad_template_small_custom,
                        GoogleENative.UNIFIED_SMALL,
                        isCheckTestDevice,
                        object : AdmobUtils.AdsNativeCallBackAdmod {
                            override fun NativeLoaded() {
                                viewGroup.visibility = View.VISIBLE
                            }

                            override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                            }

                            override fun NativeFailed(massage: String) {
                                viewGroup.visibility = View.GONE
                            }

                        })
                }

            })
    }

    fun showNativeLanguageSmall2(activity: Activity, viewGroup: ViewGroup, holder: NativeHolderAdmob,isCheckTestDevice: Boolean =false) {
        if (isTestDevice || isCheckTestDevice) {
            viewGroup.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }

        AdmobUtils.showNativeAdsWithLayout(activity,
            holder,
            viewGroup,
            R.layout.ad_template_small_custom,
            GoogleENative.UNIFIED_SMALL,
            isCheckTestDevice,
            object : AdmobUtils.AdsNativeCallBackAdmod {
                override fun NativeLoaded() {
                    viewGroup.visibility = View.VISIBLE
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun NativeFailed(massage: String) {
                    AdmobUtils.showNativeAdsWithLayout(activity,
                        holder,
                        viewGroup,
                        R.layout.ad_template_small_custom,
                        GoogleENative.UNIFIED_SMALL,
                        isCheckTestDevice,
                        object : AdmobUtils.AdsNativeCallBackAdmod {
                            override fun NativeLoaded() {
                                viewGroup.visibility = View.VISIBLE
                            }

                            override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                            }

                            override fun NativeFailed(massage: String) {
                                viewGroup.visibility = View.GONE
                            }

                        })
                }

            })
    }

    fun showAdNativeLanguage(activity: Activity, nativeAdContainer: ViewGroup, native: NativeHolderAdmob,
                             checkTestAd: Boolean = false,        isCheckTestDevice: Boolean =false, callback: () -> Unit,
                     ) {
        if (!AdmobUtils.isNetworkConnected(activity)) {
            callback.invoke()
            nativeAdContainer.visibility = View.GONE
            return
        }
        if(checkTestAd){
            if(isTestDevice){
                callback.invoke()
                nativeAdContainer.visibility = View.GONE
                return
            }
        }
        try {
            AdmobUtils.showNativeAdsWithLayout(
                activity,
                native,
                nativeAdContainer,
                R.layout.ad_template_small_custom,
                GoogleENative.UNIFIED_MEDIUM, isCheckTestDevice, object : AdmobUtils.AdsNativeCallBackAdmod {
                    override fun NativeLoaded() {
                        callback.invoke()
                    }

                    override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                    }

                    override fun NativeFailed(massage: String) {
                        callback.invoke()
                        nativeAdContainer.visibility = View.GONE
                        Log.d("NativeFailed", "NativeFailed: $massage")
                    }

                }
            )
        }catch (e: Exception){
            nativeAdContainer.visibility = View.GONE
        }
    }

    fun loadAndShowInterSP2(
        context: Context,
        interHolder: InterHolderAdmob,
        type: String,
        callback: AdListenerWithNative,
        isCheckTestDevice: Boolean = false
    ) {
        var isNowTestDeviceOrNot = isCheckTestDevice
        if (isTestDevice && isCheckTestDevice) {
            callback.onAdClosedOrFailed()
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            callback.onAdClosedOrFailed()
            return
        }
        when (type) {
            "INTER_INTRO" -> {
                if (RemoteConfig.INTER_INTRO_070625 == "0" || isTestDevice) {
                    callback.onAdClosedOrFailed()
                    return
                }
            }
            "INTER_LANGUAGE" -> {
                if (RemoteConfig.INTER_LANGUAGE_070625 == "0") {
                    callback.onAdClosedOrFailed()
                    return
                }
            }

            "INTER_HOME" -> {
                isNowTestDeviceOrNot = true
                if (RemoteConfig.INTER_HOME_070625 == "0" || isTestDevice) {
                    callback.onAdClosedOrFailed()
                    return
                }
                countClickHome ++

                if (countClickHome % RemoteConfig.INTER_HOME_070625.toInt()!=0){
                    callback.onAdClosedOrFailed()
                    return
                }
            }
        }

        AppOpenManager.getInstance().isAppResumeEnabled = true
        AdmobUtils.loadAndShowAdInterstitial(
            context as AppCompatActivity,
            interHolder,
            isNowTestDeviceOrNot,
            object : AdsInterCallBack {
                override fun onStartAction() {
                    println("onStartAction")
                }

                override fun onEventClickAdClosed() {
                    println("onEventClickAdClosed")
                    callback.onAdClosedOrFailedWithNative()
                }

                override fun onAdShowed() {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            AdmobUtils.dismissAdDialog()
                        } catch (_: Exception) {

                        }
                    }, 800)
                }

                override fun onAdLoaded() {

                }

                override fun onAdFail(p0: String?) {
                    println("onAdFail")
                    callback.onAdClosedOrFailed()
                }

                override fun onClickAds() {

                }

                override fun onPaid(p0: AdValue?, p1: String?) {
                }
            },
            true
        )
    }


    fun showAdBannerWithCallBack(
        activity: Context,
        adsEnum: String,
        view: ViewGroup,
        line: View,
        isCheckTestDevice: Boolean,
        callback: () -> Unit
    ) {
        if(isTestDevice) {
            return
        }
        if (AdmobUtils.isNetworkConnected(activity)) {
            AdmobUtils.loadAdBanner(
                activity as Activity,
                adsEnum,
                view,
                isCheckTestDevice = isCheckTestDevice,
                object : AdmobUtils.BannerCallBack {
                    override fun onLoad() {
                        view.visibility =
                            View.VISIBLE
                        line.visibility = View.VISIBLE
                        callback.invoke()
                    }

                    override fun onClickAds() {}
                    override fun onFailed(message: String) {
                        view.visibility =
                            View.GONE
                        line.visibility = View.GONE
                        callback.invoke()
                    }

                    override fun onPaid(adValue: AdValue?, mAdView: AdView?) {}
                })
        } else {
            view.visibility = View.GONE
            line.visibility = View.GONE
        }
    }

    fun showNativeFullScreen(
        context: Context,
        nativeHolder: NativeHolderAdmob,
        view: ViewGroup,
        isCheckTestDevice: Boolean = false
    ) {
        println("showNativeFullScreen: $isCheckTestDevice and $isTestDevice")
        if (isTestDevice || isCheckTestDevice) {
            view.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            view.visibility = View.GONE
            return
        }
        if(isCheckTestDevice) {
            view.visibility = View.GONE
            return
        }

        AdmobUtils.showNativeFullScreenAdsWithLayout(
            context as Activity,
            nativeHolder,
            view,
            R.layout.ad_template_native_fullscreen,
            object :
                AdmobUtils.AdsNativeCallBackAdmod {
                override fun NativeFailed(massage: String) {
                    println("NativeFailed: $massage")
                }

                override fun NativeLoaded() {
                    view.visibility = View.VISIBLE
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {
                 //do nothing
                }
            })
    }

//    fun loadAndShowNativeFullScreen(
//        context: Context,
//        nativeHolder: NativeHolderAdmob,
//        view: ViewGroup,
//        layout: Int,
//        onLoading: onLoading
//    ) {
//        if(isTestDevice) {
//            return
//        }
//        if (!AdmobUtils.isNetworkConnected(context) || AdsManager.isTestDevice) {
//            IntroActivityNew.isIntroFullFail1 = true
//            view.visibility = View.GONE
//            return
//        }
//        AdmobUtils.loadAndShowNativeFullScreen(
//            context as Activity,
//            nativeHolder.ads,
//            view,
//            layout,
//            MediaAspectRatio.SQUARE,
//            object : NativeFullScreenCallBack {
//                override fun onLoadFailed() {
//                    onLoading.onLoading()
//                }
//
//                override fun onLoaded(nativeAd: NativeAd) {
//
//                }
//            })
//    }


    interface onLoading {
        fun onLoading()
    }


    fun loadNativeFullScreen(
        context: Context,
        nativeHolder: NativeHolderAdmob,
        onLoaded: (Boolean) -> Unit
    ) {

        if (!AdmobUtils.isNetworkConnected(context) || isTestDevice) {
            return
        }

        AdmobUtils.loadAndGetNativeFullScreenAds(
            context as Activity,
            nativeHolder,
            MediaAspectRatio.SQUARE,
            object :
                AdmobUtils.NativeAdCallbackNew {

                override fun onAdFail(error: String) {
                    println("onAdFail: $error")
                    onLoaded(false)

                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun onClickAds() {
                }

                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    checkAdsTest(ad)
                    onLoaded(true)
                }

                override fun onNativeAdLoaded() {
                    onLoaded(true)
                }

            })

    }


    interface AdListenerWithNative {
        fun onAdClosedOrFailed()
        fun onAdClosedOrFailedWithNative()
    }
}