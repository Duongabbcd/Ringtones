package com.ezt.ringify.ringtonewallpaper.screen.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.admob.max.dktlibrary.AOAManager
import com.admob.max.dktlibrary.AdmobUtils
import com.admob.max.dktlibrary.AdmobUtils.isNetworkConnected
import com.admob.max.dktlibrary.AppOpenManager
import com.admob.max.dktlibrary.cmp.GoogleMobileAdsConsentManager
import com.admob.max.dktlibrary.firebase.FireBaseConfig
import com.admob.max.dktlibrary.utils.admod.callback.MobileAdsListener
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity2
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySplashBinding
import com.ezt.ringify.ringtonewallpaper.screen.language.LanguageActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.inVisible
import com.google.android.ump.FormError
import com.google.firebase.FirebaseApp
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.isDebug
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.isTestDevice
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig.NATIVE_FULL_SPLASH_070625
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig.REMOTE_SPLASH_070625
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroActivityNew
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.google.android.gms.ads.AdValue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

class SplashActivity : BaseActivity2<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    private var appOpenManager: AOAManager? = null
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var isInitAds = AtomicBoolean(false)

    private var handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {
        if (showAds) {
//            initAdmob()
        }
    }
    private var showAds = true
    private var isFinish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
//        Common.setCountOpenApp(this,0)
    }

    override fun initView() {
        if ((!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) && intent.action != null && intent.action === Intent.ACTION_MAIN) {
            finish()
            return
        }

        Common.setPreLanguage(this, "en")
        handler.postDelayed(runnable, 20000)
        if (isNetworkConnected(this)) {
            FireBaseConfig.initRemoteConfig(
                R.xml.remote_config_default,
                object : FireBaseConfig.CompleteListener {
                    override fun onComplete() {
//                        REMOTE_SPLASH_070625 =
//                            FireBaseConfig.getValue("REMOTE_SPLASH_070625")
//                        RemoteConfig.ADS_SPLASH_070625 =
//                            FireBaseConfig.getValue("ADS_SPLASH_070625")
//                        NATIVE_FULL_SPLASH_070625 =
//                            FireBaseConfig.getValue("NATIVE_FULL_SPLASH_070625")
//                        RemoteConfig.NATIVE_LANGUAGE_070625 =
//                            FireBaseConfig.getValue("NATIVE_LANGUAGE_070625")
//                        RemoteConfig.INTER_LANGUAGE_070625 =
//                            FireBaseConfig.getValue("INTER_LANGUAGE_070625")
//                        NATIVE_INTRO_070625 = FireBaseConfig.getValue("NATIVE_INTRO_070625")
//                        RemoteConfig.ADS_INTRO_070625 = FireBaseConfig.getValue("ADS_INTRO_070625")
//                        NATIVE_FULL_SCREEN_INTRO_070625 =
//                            FireBaseConfig.getValue("NATIVE_FULL_SCREEN_INTRO_070625")
//                        RemoteConfig.INTER_INTRO_070625 =
//                            FireBaseConfig.getValue("INTER_INTRO_070625")
//                        RemoteConfig.INTER_HOME_070625 =
//                            FireBaseConfig.getValue("INTER_HOME_070625")
//                        RemoteConfig.REMOTE_ADS_PLAY_SONG_070625 =
//                            FireBaseConfig.getValue("REMOTE_ADS_PLAY_SONG_070625")
//                        RemoteConfig.REMOTE_ADS_HOME_070625 =
//                            FireBaseConfig.getValue("REMOTE_ADS_HOME_070625")
//                        RemoteConfig.NATIVE_CUSTOM_HOME_070625 =
//                            FireBaseConfig.getValue("NATIVE_CUSTOM_HOME_070625")
//                        RemoteConfig.BANNER_COLLAP_ALL_070625 =
//                            FireBaseConfig.getValue("BANNER_COLLAP_ALL_070625")
//                        RemoteConfig.UPDATE_APP_VERSION =
//                            FireBaseConfig.getValue("UPDATE_APP_VERSION")
//                        RemoteConfig.ONRESUME_070625 = FireBaseConfig.getValue("ONRESUME_070625")

                        AdsManager.countClickRingtone = 0
                        AdsManager.countClickWallpaper = 0
                        AdsManager.countClickCallscreen = 0

                        if (isInitAds.get()) {
                            return
                        }
                        isInitAds.set(true)
                        setupCMP()
                    }
                })
        } else {
            binding.tvStart.inVisible()
            Handler(Looper.getMainLooper()).postDelayed({
                nextScreen()
            }, 3000)
        }
    }

    private fun nextScreen() {
        val intent = Intent(this@SplashActivity, LanguageActivity::class.java)
        intent.putExtra("fromSplash", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun setupCMP() {
        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager(this)
        googleMobileAdsConsentManager.gatherConsent { error: FormError? ->
            if (error != null) {
                initializeMobileAdsSdk()
            }
            if (googleMobileAdsConsentManager.canRequestAds) {
                initializeMobileAdsSdk()
            }
        }
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.get()) {
            return
        }
        isMobileAdsInitializeCalled.set(true)
        initAdmob()
    }

    private fun initAdmob() {
        showAds = false
        handler.removeCallbacks(runnable)
        AdmobUtils.initAdmob(this@SplashActivity,
            isDebug,
            true,
            isCheckTestDevice = false,
            mobileAdsListener = object : MobileAdsListener {
                override fun onSuccess() {
                    showBanner()
                    println("RemoteConfig.ONRESUME_070625: ${RemoteConfig.ONRESUME_070625}")
                    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.P && RemoteConfig.ONRESUME_070625 == "1") {
                        AppOpenManager.getInstance().init(application, AdsManager.ONRESUME)
                        AppOpenManager.getInstance()
                            .disableAppResumeWithActivity(SplashActivity::class.java)
//                        AppOpenManager.getInstance()
//                            .disableAppResumeWithActivity(NativeFullActivity::class.java)

                    }
                    if (RemoteConfig.NATIVE_LANGUAGE_070625 == "1") {
                        AdsManager.loadNative(this@SplashActivity, AdsManager.NATIVE_LANGUAGE)
                        AdsManager.loadNative(this@SplashActivity, AdsManager.NATIVE_LANGUAGE_ID2)
                    }
                    if (NATIVE_FULL_SPLASH_070625 == "1") {
                        AdsManager.loadNativeFullScreen(
                            this@SplashActivity,
                            AdsManager.NATIVE_FULL_SPLASH
                        ) {

                        }
                    }
                    println("NATIVE_FULL_SCREEN_INTRO_070625:$NATIVE_FULL_SCREEN_INTRO_070625")
                    if (NATIVE_FULL_SCREEN_INTRO_070625 != "0") {
                        AdsManager.loadNativeFullScreen(
                            this@SplashActivity,
                            AdsManager.NATIVE_FULL_SCREEN_INTRO
                        ) { result ->
                            IntroActivityNew.isIntroFullFail1 = !result
                        }
                    }
                }
            })

    }

    private fun showBanner() {
        Log.d("ADS_SPLASH_070625", "showBanner: ${RemoteConfig.ADS_SPLASH_070625}")
        when (RemoteConfig.ADS_SPLASH_070625) {
            "1" -> {
                binding.tvStart.visibility = View.GONE
                AdsManager.showAdBanner(
                    this,
                    AdsManager.BANNER_SPLASH,
                    binding.frBanner,
                    binding.view,
                    false
                ) {
                    showAds()
                }
            }

            "2" -> {
                binding.tvStart.visibility = View.GONE
                AdsManager.loadAndShowNative(
                    this,
                    binding.frBanner,
                    false,
                    AdsManager.NATIVE_SPLASH
                ) {
                    showAds()
                }
            }

            "3" -> {
                binding.tvStart.visibility = View.GONE
                AdsManager.loadAndShowNativeCollapsible(
                    this,
                    AdsManager.NATIVE_SPLASH,
                    binding.frBanner,
                    false
                ) {
                    showAds()
                }
            }

            else -> {
                binding.tvStart.visibility = View.GONE
                showAds()
            }
        }
    }

    private fun showAds() {
        println("showAds: $REMOTE_SPLASH_070625")
        when (REMOTE_SPLASH_070625) {
            "0" -> {
                binding.tvStart.inVisible()
                Handler(Looper.getMainLooper()).postDelayed({
                    nextScreen()
                }, 1500)
            }

            "1" -> {
                binding.tvStart.visible()
                Handler(Looper.getMainLooper()).postDelayed({
                    loadAOA()
                }, 1500)
            }

            "2" -> {
                binding.tvStart.visible()
                Handler(Looper.getMainLooper()).postDelayed({
                    showInter()
                }, 1500)
            }
        }

    }

    private var isCheckFail = false

    private fun loadAOA() {
        val initialValue = AdsManager.AOA_SPLASH
        appOpenManager = AOAManager(
            this@SplashActivity,
            initialValue,
            10000,
            object : AOAManager.AppOpenAdsListener {
                override fun onAdPaid(adValue: AdValue, adUnitAds: String) {
                }

                override fun onAdsClose() {
                    if (NATIVE_FULL_SPLASH_070625 == "1" && !isTestDevice) {
                        nextScreen()
                    } else {
                        nextScreen()
                    }
                }

                override fun onAdsFailed(message: String) {
                    isCheckFail = true
                    nextScreen()
                }

                override fun onAdsLoaded() {

                }
            })
        appOpenManager?.loadAoA()

    }

    private fun showInter() {
        val initialValue = AdsManager.INTER_SPLASH
        AdsManager.loadAndShowInterSplash(
            this,
            initialValue,
            false,
            object : AdsManager.AdListenerWithNative {
                override fun onAdClosedOrFailed() {
                    if (NATIVE_FULL_SPLASH_070625 != "0" && !isTestDevice) {
                        nextScreen()
                    } else {
                        nextScreen()
                    }
                }

                override fun onAdClosedOrFailedWithNative() {
                    if (NATIVE_FULL_SPLASH_070625 != "0" && !isTestDevice) {
                        nextScreen()
                    } else {
                        nextScreen()
                    }
                }
            }, true
        )
    }


    override fun onBackPressed() {
        super.onBackPressed()
        exitProcess(0)
    }
}