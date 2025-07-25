package com.ezt.ringify.ringtonewallpaper

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.admob.max.dktlibrary.application.AdsApplication
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil
import com.ezt.ringify.ringtonewallpaper.utils.Common
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : AdsApplication(), Application.ActivityLifecycleCallbacks{
    override fun onCreate() {
        super.onCreate()
        val deviceId = Common.getDeviceId(this)
        val secret = "abcadhjgashjd1231" // TODO: Replace with your actual secret
        val jwt = Common.generateJwt(deviceId, secret)
        println("MyApplication: $jwt")
        RingtoneRepository.TOKEN = jwt
    }

    override fun onCreateApplication() {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val lang = Common.getPreLanguage(this)
        println("onActivityCreated: $lang")
        Common.setLocale(this@MyApplication, lang)
    }

    override fun onActivityStarted(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
        TODO("Not yet implemented")
    }


}