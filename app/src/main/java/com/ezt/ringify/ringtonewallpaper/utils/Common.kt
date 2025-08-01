package com.ezt.ringify.ringtonewallpaper.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Application.MODE_MULTI_PROCESS
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.ezt.ringify.ringtonewallpaper.R
import java.util.Locale
import android.provider.Settings
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object Common {
    var countDone = 0
    var countShowRate = 0

    var isShowRate = false
    var showTime = 0

    var isEnableClick = true

    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.inVisible() {
        visibility = View.INVISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun Context.toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
//
//    fun scheduleNotification(context: Context, time: Long) {
//        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//            .setInitialDelay(time, TimeUnit.SECONDS)
//            .build()
//
//        WorkManager.getInstance(context).enqueue(notificationRequest)
//    }

    fun getLang(mContext: Context): String {
        val preferences =
            mContext.getSharedPreferences(mContext.packageName, Context.MODE_MULTI_PROCESS)
        return preferences.getString("KEY_LANG", "en") ?: "English (UK)"
    }

    fun setLang(context: Context, open: String?) {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_MULTI_PROCESS)
        preferences.edit().putString("KEY_LANG", open).apply()
    }

    fun getPreLanguageflag(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getInt("KEY_FLAG", R.drawable.english)
    }

    fun setPreLanguageflag(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putInt("KEY_FLAG", flag).apply()
    }

    fun getPreLanguage(mContext: Context): String {
        val preferences = mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getString("KEY_LANGUAGE", "en").toString()
    }

    fun setPreLanguage(context: Context, language: String?) {
        if (TextUtils.isEmpty(language)) return
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putString("KEY_LANGUAGE", language).apply()
    }

    fun setPosition(context: Context, open: Int) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putInt("KEY_POSITION", open).apply()
    }

    fun getPosition(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getInt("KEY_POSITION", -1)
    }

    fun setLocale(context: Context, lang: String?) {
        val myLocale = lang?.let { Locale(it) }
        val res = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(myLocale)
        res.updateConfiguration(conf, dm)
    }

    fun setRemoteKey(context: Context, key: String, value: String) {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_MULTI_PROCESS)
        preferences.edit().putString(key, value).apply()
    }

    fun getRemoteKey(context: Context, key: String, default: String): String {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_MULTI_PROCESS)
        return preferences.getString(key, default).toString()
    }

//     fun showRate(context: Activity) {
//        val ratingDialog1 = RatingDialog.Builder(context).session(1).date(1).ignoreRated(false)
//            .setNameApp(context.resources.getString(R.string.app_name)).setIcon(R.drawable.app_logo_square)
//            .setEmail("namkutethanhhoa@gmail.com").isShowButtonLater(true).isClickLaterDismiss(true)
//            .setTextButtonLater("Maybe Later").setOnlickMaybeLate {
//                AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
//            }.setOnlickRate {
//                AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity::class.java)
//            }.setDeviceInfo(
//                BuildConfig.VERSION_NAME,
//                Build.VERSION.SDK_INT.toString(),
//                Build.MANUFACTURER + "_" + Build.MODEL
//            ) .ratingButtonColor(Color.parseColor("#004BBB"))
//            .build()
//        ratingDialog1 . setCanceledOnTouchOutside (false)
//        ratingDialog1 . show ()
//    }

    fun logEventFirebase(context: Context, eventName: String) {
//        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
//        val bundle = Bundle()
//        bundle.putString("onEvent", context.javaClass.simpleName)
//        firebaseAnalytics.logEvent(eventName + "_" + BuildConfig.VERSION_CODE, bundle)
//        Log.d("===Event", eventName + "_" + BuildConfig.VERSION_CODE)
    }

    fun getCountOpenApp(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_CountOpenApp", 0)
    }

    fun setCountOpenApp(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_CountOpenApp", flag).apply()
    }

    fun getCountRate(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_CountRate", 0)
    }

    fun setCountRate(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_CountRate", flag).apply()
    }

    fun getFirstUse2(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_FirstUse2", 0)
    }

    fun setFirstUse2(context: Context, isFirstUse: Int = 0 ) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_FirstUse2", isFirstUse).apply()
    }


    fun getTheme(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_UsedTheme", 0)
    }

    fun setTheme(context: Context, selectedTheme: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_UsedTheme", selectedTheme).apply()
    }


    fun showDialogGoToSetting(context: Context, onClickListener: (Boolean) -> Unit) {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(R.string.title_grant_Permission)
        alertDialog.setMessage(context.getString(R.string.message_grant_Permission))
        alertDialog.setCancelable(true)
        alertDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, context.getString(R.string.goto_setting)
        ) { _: DialogInterface?, _: Int ->
            onClickListener(true)
            alertDialog.dismiss()
        }

        // Negative button: Cancel
        alertDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel)
        ) { _: DialogInterface?, _: Int ->
            onClickListener(false)
            alertDialog.dismiss()
        }

        // Handle cancel (e.g., tapped outside or pressed back)
        alertDialog.setOnCancelListener {
            onClickListener(false)
        }

        alertDialog.show ()

        // Set background to white
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        // Set positive button text color to black
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
        // Set title and message text color to black
        val titleId = context.resources.getIdentifier("alertTitle", "id", "android")
        val messageId = android.R.id.message

        alertDialog.findViewById<TextView>(titleId)?.setTextColor(Color.BLACK)
        alertDialog.findViewById<TextView>(messageId)?.setTextColor(Color.BLACK)
    }

    fun setCountDownTime(context: Context, totalTime: Long) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putLong("KEY_COUNTDOWN", totalTime).apply()
    }

  fun getCountDownTime(context: Context)  : Long{
      val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
      return preferences.getLong("KEY_COUNTDOWN", 0L)
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun generateJwt(deviceId: String, secret: String): String {
        val algorithm = Algorithm.HMAC256(secret)
        val data = mapOf("client_id" to deviceId, "type" to 1)
        return JWT.create()
            .withClaim("data", data)
            .sign(algorithm)
    }

    fun setSortOrder(context: Context, sortOrder: String)  {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
       preferences.edit().putString("KEY_SortOrder", sortOrder).apply()
    }

    fun getSortOrder(context: Context) : String {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        return preferences.getString("KEY_SortOrder", "name+asc").toString()
    }

}