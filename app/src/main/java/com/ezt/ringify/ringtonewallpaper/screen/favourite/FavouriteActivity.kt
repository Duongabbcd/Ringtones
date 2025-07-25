package com.ezt.ringify.ringtonewallpaper.screen.favourite

import android.content.Intent
import android.os.Bundle
import com.admob.max.dktlibrary.AdmobUtils
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFavouriteBinding
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FavouriteActivity : BaseActivity<ActivityFavouriteBinding>(ActivityFavouriteBinding::inflate),
    com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew.CallbackIntro {

    private lateinit var introViewPagerAdapter: FavouriteAdapter
    var position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        viewPager()
    }
    private fun viewPager() {
        if (!AdmobUtils.isNetworkConnected(this)) {
            if (!isIntroFullFail1) {
                isIntroFullFail1 = true
            }
        }

        println("isIntroFullFail:$isIntroFullFail1")
        println("isIntroFullFail:${RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625}")
        numberPage = 2

        introViewPagerAdapter = FavouriteAdapter(this)
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

    override fun onNext(position: Int, introPos: Int) {
        if (position <= numberPage - 1) {
            when(introPos){
               0 -> {
                    showAfterIntro1 {
                        binding.viewpager.currentItem++
                    }
                }
                else -> {
                    showAfterIntro1 {
                        binding.viewpager.currentItem++
                    }
                }
            }
        } else {
            showAfterIntro2 {
                startAc()
            }
        }
    }

    private fun showAfterIntro1(callback : () -> Unit){
        callback.invoke()
    }

    private fun showAfterIntro2(callback : () -> Unit){
        callback.invoke()
    }

    override fun closeAds() {
        //DO NOTHING
    }

    override fun disableSwip() {
       //DO NOTHING
    }

    companion object {
        var isIntroFullFail1: Boolean = true
        var numberPage = 2
    }
}