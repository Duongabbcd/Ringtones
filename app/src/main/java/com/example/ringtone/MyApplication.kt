package com.example.ringtone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.ringtone.remote.repository.RingtoneRepository
import com.example.ringtone.utils.Common
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks{
    override fun onCreate() {
        super.onCreate()
        val deviceId = Common.getDeviceId(this)
        val secret = "abcadhjgashjd1231" // TODO: Replace with your actual secret
        val jwt = Common.generateJwt(deviceId, secret)
        RingtoneRepository.TOKEN = jwt
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