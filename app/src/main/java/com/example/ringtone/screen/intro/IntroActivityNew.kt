package com.example.ringtone.screen.intro

import android.content.Intent
import android.os.Bundle
import com.admob.max.dktlibrary.AdmobUtils
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityIntroBinding
import com.example.ringtone.screen.MainActivity
import com.musicplayer.mp3.playeroffline.ads.AdsManager
import com.musicplayer.mp3.playeroffline.ads.AdsManager.isTestDevice
import com.musicplayer.mp3.playeroffline.ads.RemoteConfig
import kotlin.jvm.java

class IntroActivityNew : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate), IntroFragmentNew.CallbackIntro {
    private lateinit var introViewPagerAdapter: SlideAdapter
    var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewPager()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    private fun viewPager() {
        if (!AdmobUtils.isNetworkConnected(this)) {
            if (!isIntroFullFail1) {
                isIntroFullFail1 = true
            }
        }

        println("isIntroFullFail:$isIntroFullFail1")
        println("isIntroFullFail:${RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625}")
        if(isTestDevice) {
            numberPage = 3
        } else {
            numberPage = if (RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625 == "12" && !isIntroFullFail1 ) {
                5
            } else if (((RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625 == "1" || RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625 == "2")) && !isIntroFullFail1) {
                4
            } else {
                3
            }
        }


        introViewPagerAdapter = SlideAdapter(this)
        binding.viewpager.adapter = introViewPagerAdapter
        binding.viewpager.setOffscreenPageLimit(numberPage)
        binding.viewpager.isUserInputEnabled = false
    }

    override fun onStop() {
        super.onStop()
//        binding.viewGone.gone()
    }

    private fun startAc() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
//        AdsManager.isEnableClick = true
    }

    companion object {
        var isIntroFullFail1: Boolean = true
        var numberPage = 3
    }

    override fun onNext(position: Int, introPos: Int) {
        if (position < numberPage - 1) {
            when(introPos){
                1 -> {
                    showAfterIntro1 {
                        binding.viewpager.currentItem++
                    }
                }
                2 -> {
                    showAfterIntro2 {
                        binding.viewpager.currentItem++
                    }
                }
                else -> {
                    binding.viewpager.currentItem++
                }
            }
        } else {
            showAfterIntro3 {
                startAc()
            }
        }
    }

    override fun closeAds() {
//        binding.screenViewpager.isUserInputEnabled = true
        binding.viewpager.currentItem++
    }

    override fun disableSwip() {
//        binding.screenViewpager.isUserInputEnabled = false
    }

    private fun showAfterIntro1(callback : () -> Unit){
        if(RemoteConfig.INTER_INTRO_070625.contains("1") && !isTestDevice){
            showInter(callback)
        }else {
            callback.invoke()
        }
    }

    private fun showAfterIntro2(callback : () -> Unit){
        if(RemoteConfig.INTER_INTRO_070625.contains("2") && !isTestDevice){
            showInter(callback)
        }else {
            callback.invoke()
        }
    }

    private fun showAfterIntro3(callback : () -> Unit){
        if(RemoteConfig.INTER_INTRO_070625.contains("3") && !isTestDevice){
            showInter(callback)
        }else {
            callback.invoke()
        }
    }

    private fun showInter(callback : () -> Unit){
        AdsManager.loadAndShowInterSP2(this, AdsManager.INTER_INTRO, "",
            object : AdsManager.AdListenerWithNative {
                override fun onAdClosedOrFailed() {
                    callback.invoke()
                }

                override fun onAdClosedOrFailedWithNative() {
                    callback.invoke()
                }
            }, isCheckTestDevice = true)
    }

}