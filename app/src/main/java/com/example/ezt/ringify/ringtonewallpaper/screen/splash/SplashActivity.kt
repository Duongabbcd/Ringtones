package com.example.ringtone.screen.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.admob.max.dktlibrary.AOAManager
import com.admob.max.dktlibrary.AdmobUtils
import com.admob.max.dktlibrary.AdmobUtils.isNetworkConnected
import com.admob.max.dktlibrary.cmp.GoogleMobileAdsConsentManager
import com.admob.max.dktlibrary.firebase.FireBaseConfig
import com.admob.max.dktlibrary.utils.admod.callback.MobileAdsListener
import com.example.ringtone.R
import com.example.ringtone.base.BaseActivity2
import com.example.ringtone.databinding.ActivitySplashBinding
import com.example.ringtone.screen.language.LanguageActivity
import com.example.ringtone.utils.Common
import com.example.ringtone.utils.Common.inVisible
import com.google.android.ump.FormError
import com.google.firebase.FirebaseApp
import com.musicplayer.mp3.playeroffline.ads.AdsManager
import com.musicplayer.mp3.playeroffline.ads.AdsManager.isDebug
import com.musicplayer.mp3.playeroffline.ads.RemoteConfig
import com.musicplayer.mp3.playeroffline.ads.RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625
import com.musicplayer.mp3.playeroffline.ads.RemoteConfig.NATIVE_FULL_SPLASH_070625
import com.musicplayer.mp3.playeroffline.ads.RemoteConfig.NATIVE_INTRO_070625
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
    }

    override fun initView() {
        if ((!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) && intent.action != null && intent.action === Intent.ACTION_MAIN) {
            finish()
            return
        }

        Common.setPreLanguage(this, "en")
        handler.postDelayed(runnable, 20000)
        val x = false
        if (x) {
            FireBaseConfig.initRemoteConfig(
                R.xml.remote_config_default,
                object : FireBaseConfig.CompleteListener {
                    override fun onComplete() {
                        RemoteConfig.REMOTE_SPLASH_070625 =
                            FireBaseConfig.getValue("REMOTE_SPLASH_070625")
                        RemoteConfig.ADS_SPLASH_070625 =
                            FireBaseConfig.getValue("ADS_SPLASH_070625")
                        NATIVE_FULL_SPLASH_070625 =
                            FireBaseConfig.getValue("NATIVE_FULL_SPLASH_070625")
                        RemoteConfig.NATIVE_LANGUAGE_070625 =
                            FireBaseConfig.getValue("NATIVE_LANGUAGE_070625")
                        RemoteConfig.INTER_LANGUAGE_070625 =
                            FireBaseConfig.getValue("INTER_LANGUAGE_070625")
                        NATIVE_INTRO_070625 = FireBaseConfig.getValue("NATIVE_INTRO_070625")
                        RemoteConfig.ADS_INTRO_070625 = FireBaseConfig.getValue("ADS_INTRO_070625")
                        NATIVE_FULL_SCREEN_INTRO_070625 =
                            FireBaseConfig.getValue("NATIVE_FULL_SCREEN_INTRO_070625")
                        RemoteConfig.INTER_INTRO_070625 =
                            FireBaseConfig.getValue("INTER_INTRO_070625")
                        RemoteConfig.INTER_HOME_070625 =
                            FireBaseConfig.getValue("INTER_HOME_070625")
                        RemoteConfig.REMOTE_ADS_PLAY_SONG_070625 =
                            FireBaseConfig.getValue("REMOTE_ADS_PLAY_SONG_070625")
                        RemoteConfig.REMOTE_ADS_HOME_070625 =
                            FireBaseConfig.getValue("REMOTE_ADS_HOME_070625")
                        RemoteConfig.NATIVE_CUSTOM_HOME_070625 =
                            FireBaseConfig.getValue("NATIVE_CUSTOM_HOME_070625")
                        RemoteConfig.BANNER_COLLAP_ALL_070625 =
                            FireBaseConfig.getValue("BANNER_COLLAP_ALL_070625")
                        RemoteConfig.UPDATE_APP_VERSION =
                            FireBaseConfig.getValue("UPDATE_APP_VERSION")
                        RemoteConfig.ONRESUME_070625 = FireBaseConfig.getValue("ONRESUME_070625")

                        AdsManager.countClickHome = 0
                        AdsManager.countClickMessageBack = 0
                        AdsManager.countClickItem = 0

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
            isCheckTestDevice = true,
            mobileAdsListener = object : MobileAdsListener {
                override fun onSuccess() {
//                    showBanner()
//                    println("RemoteConfig.ONRESUME_070625: ${RemoteConfig.ONRESUME_070625}")
//                    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.P && RemoteConfig.ONRESUME_070625 == "1") {
//                        AppOpenManager.getInstance().init(application, AdsManager.ONRESUME)
//                        AppOpenManager.getInstance()
//                            .disableAppResumeWithActivity(com.musicplayer.mp3.playeroffline.screen.splash.SplashActivity::class.java)
//                        AppOpenManager.getInstance()
//                            .disableAppResumeWithActivity(NativeFullActivity::class.java)
//
//                    }
//                    if (RemoteConfig.NATIVE_LANGUAGE_070625 == "1") {
//                        AdsManager.loadNative(thiscom.musicplayer.mp3.playeroffline.screen.splash.SplashActivity, AdsManager.NATIVE_LANGUAGE)
//                        AdsManager.loadNative(thiscom.musicplayer.mp3.playeroffline.screen.splash.SplashActivity, AdsManager.NATIVE_LANGUAGE_ID2)
//                    }
//                    if (NATIVE_FULL_SPLASH_070625 == "1") {
//                        AdsManager.loadNativeFullScreen(
//                            thiscom.musicplayer.mp3.playeroffline.screen.splash.SplashActivity,
//                            AdsManager.NATIVE_FULL_SPLASH
//                        ) {
//
//                        }
//                    }
//                    println("NATIVE_FULL_SCREEN_INTRO_070625:$NATIVE_FULL_SCREEN_INTRO_070625")
//                    if (NATIVE_FULL_SCREEN_INTRO_070625 != "0") {
//                        AdsManager.loadNativeFullScreen(
//                            thiscom.musicplayer.mp3.playeroffline.screen.splash.SplashActivity,
//                            AdsManager.NATIVE_FULL_SCREEN_INTRO
//                        ) { result ->
//                            IntroActivityNew.isIntroFullFail1 = !result
//                        }
//                    }
                }
            })

    }


    override fun onBackPressed() {
        super.onBackPressed()
        exitProcess(0)
    }
}