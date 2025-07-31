package com.ezt.ringify.ringtonewallpaper.screen.callscreen.ext

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class AppPreferences(private val preferences: SharedPreferences) {
    var isFinishRate by BooleanPreference(KEY_IS_FINISH_RATE, false)
    var vibrateState by BooleanPreference(VIBRATE_STATE_KEY, false)
    var splashState by BooleanPreference(SPLASH_STATE_KEY, false)

    inner class BooleanPreference(
        private val key: String,
        private val defaultValue: Boolean = true
    ) : ReadWriteProperty<AppPreferences, Boolean> {
        override fun getValue(thisRef: AppPreferences, property: KProperty<*>): Boolean {
            return preferences.getBoolean(key, defaultValue)
        }

        override fun setValue(thisRef: AppPreferences, property: KProperty<*>, value: Boolean) {
            preferences.edit().putBoolean(key, value).apply()
        }
    }

    companion object {
        const val VIBRATE_STATE_KEY = "VIBRATE_STATE_KEY"
        const val KEY_IS_FINISH_RATE = "KEY_IS_FINISH_RATE"
        const val SPLASH_STATE_KEY = "SPLASH_STATE_KEY"
    }
}
