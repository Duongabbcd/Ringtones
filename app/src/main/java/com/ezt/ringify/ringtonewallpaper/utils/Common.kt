package com.ezt.ringify.ringtonewallpaper.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Application.MODE_MULTI_PROCESS
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.admob.max.dktlibrary.AppOpenManager
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.ratingdialog.RatingDialog
import com.ezt.ringify.ringtonewallpaper.BuildConfig
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity
import java.util.Locale

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
            mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getString("KEY_LANG", "en") ?: "English (UK)"
    }

    fun setLang(context: Context, open: String?) {
        val preferences =
            context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
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
            context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putString(key, value).apply()
    }

    fun getRemoteKey(context: Context, key: String, default: String): String {
        val preferences =
            context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        return preferences.getString(key, default).toString()
    }

    fun showRate(context: Activity) {
        val ratingDialog1 = RatingDialog.Builder(context).session(1).date(1).ignoreRated(false)
            .setNameApp(context.resources.getString(R.string.app_name))
            .setIcon(R.drawable.icon_app_round)
            .setEmail("linhnguyen.ezt@gmail.com")
            .isShowButtonLater(true).isClickLaterDismiss(true)
            .setTextButtonLater("Maybe Later").setOnlickMaybeLate {
                AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
            }.setOnlickRate {
                AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity::class.java)
            }.setDeviceInfo(
                BuildConfig.VERSION_NAME,
                Build.VERSION.SDK_INT.toString(),
                Build.MANUFACTURER + "_" + Build.MODEL
            ).ratingButtonColor(Color.parseColor("#004BBB"))
            .build()
        ratingDialog1.setCanceledOnTouchOutside(false)
        ratingDialog1.show()
    }

    fun Context.openUrl(url: String) {
        runCatching {
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).also {
                this.startActivity(it)
            }
        }
    }

    fun Context.openPrivacy() {
        openUrl("https://docs.google.com/document/d/1QXF3ntK3PG9uxOALSgkoChrpN80-hx6PZsXDNVRQzig/edit?pli=1&tab=t.0")
    }

    fun Context.composeEmail(recipient: String, subject: String) {

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

        try {
            this.startActivity(Intent.createChooser(emailIntent, "Send Email"))
        } catch (e: ActivityNotFoundException) {
            // Handle case where no email app is available
        }
    }

    fun Context.rateApp() {
        val applicationID = this.packageName
        val playStoreUri = Uri.parse("market://details?id=$applicationID")

        val rateIntent = Intent(Intent.ACTION_VIEW, playStoreUri)
        rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            this.startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            val webPlayStoreUri =
                Uri.parse("https://play.google.com/store/apps/details?id=$applicationID")
            val webRateIntent = Intent(Intent.ACTION_VIEW, webPlayStoreUri)
            this.startActivity(webRateIntent)
        }
    }


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
            MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_CountOpenApp", 0)
    }

    fun setCountOpenApp(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_CountOpenApp", flag).apply()
    }

    fun getCountRate(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_CountRate", 0)
    }

    fun setCountRate(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_CountRate", flag).apply()
    }

    fun getFirstUse2(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_FirstUse2", 0)
    }

    fun setAllFavouriteWallpaper(context: Context, list: List<Int> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Fav_Wall", list.joinToString(",")).apply()
    }


    fun setAllFavouriteGenres(context: Context, list: List<Int> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Fav_Gen", list.joinToString(",")).apply()
    }


    fun getAllFavouriteGenres(mContext: Context): List<Int> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Fav_Gen", null)
        return savedList?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    }

    fun getAllFavouriteWallpaper(mContext: Context): List<Int?> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Fav_Wall", null)
        return savedList?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
    }

    fun getAllFreeRingtones(mContext: Context): List<String> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Free_Ringtones", null)
        return savedList?.split(",")?.mapNotNull { it } ?: listOf()
    }

    fun setAllFreeRingtones(context: Context, list: List<String> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Free_Ringtones", list.joinToString(",")).apply()
    }

    fun getAllFreeWallpapers(mContext: Context): List<Int> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Free_Wallpapers", null)
        return savedList?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
    }

    fun setAllFreeWallpapers(context: Context, list: List<Int> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Free_Wallpapers", list.joinToString(",")).apply()
    }

    fun getAllNewRingtones(mContext: Context): List<Int> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Fixed_New", null)
        return savedList?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
    }

    fun setAllNewRingtones(context: Context, list: List<Int> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Fixed_New", list.joinToString(",")).apply()
    }

    fun getAllTrendingRingtones(mContext: Context): List<Int> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Fixed_Trend", null)
        return savedList?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
    }

    fun setAllWeeklyTrendingRingtones(context: Context, list: List<Int> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Fixed_Trend", list.joinToString(",")).apply()
    }

    fun getAllEditorChoices(mContext: Context): List<Int> {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            MODE_MULTI_PROCESS
        )
        val savedList = preferences.getString("KEY_Fixed_Choice", null)
        return savedList?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf()
    }

    fun setAllEditorChoices(context: Context, list: List<Int> = emptyList()) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
        )
        preferences.edit().putString("KEY_Fixed_Choice", list.joinToString(",")).apply()
    }

    fun setTheme(context: Context, selectedTheme: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            MODE_MULTI_PROCESS
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

    fun setSortWppOrder(context: Context, sortOrder: String) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putString("KEY_Wpp_SortOrder", sortOrder).apply()
    }


    fun getSortWppOrder(context: Context): String {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        return preferences.getString("KEY_Wpp_SortOrder", "Default").toString()
    }

    fun setNotificationEnable(context: Context, isEnable: Boolean) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putBoolean("KEY_NOTIF_ENABLE", isEnable).apply()
    }

    private val isTiramisuOrAbove by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun getNotificationEnable(context: Context): Boolean {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        val defaultValue = isTiramisuOrAbove
        return preferences.getBoolean("KEY_NOTIF_ENABLE", defaultValue)
    }

}