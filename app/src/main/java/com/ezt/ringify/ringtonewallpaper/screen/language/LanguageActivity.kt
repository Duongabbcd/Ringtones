package com.ezt.ringify.ringtonewallpaper.screen.language

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.admob.max.dktlibrary.AppOpenManager
import com.admob.max.dktlibrary.utils.admod.NativeHolderAdmob
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityLanguageBinding
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroActivityNew
import com.ezt.ringify.ringtonewallpaper.screen.language.adapter.Language
import com.ezt.ringify.ringtonewallpaper.screen.language.adapter.LanguageAdapter
import com.ezt.ringify.ringtonewallpaper.screen.setting.SettingActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.GlobalConstant
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate){
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private var adapter2: LanguageAdapter? = null
    private var start = false
    private var now = 0L

    private lateinit var allLanguages : ArrayList<Language>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allLanguages = GlobalConstant.getListLocation(this@LanguageActivity)
        now = System.currentTimeMillis()
        if (RemoteConfig.INTER_WALLPAPER != "0") {
            InterAds.preloadInterAds(
                this@LanguageActivity,
                alias = InterAds.ALIAS_INTER_RINGTONE,
                adUnit = InterAds.INTER_RINGTONE
            )
        }


        start = intent.getBooleanExtra("fromSplash", false)
        binding.apply {
            if(RemoteConfig.NATIVE_INTRO_070625 != "0") {
                AdsManager.loadNative(this@LanguageActivity, AdsManager.NATIVE_INTRO)
            } else {
                view.gone()
            }
            if (start) {
                backBtn.gone()
                selectedLanguage = ""
            } else {
                backBtn.visible()
                selectedLanguage = Common.getPreLanguage(this@LanguageActivity)
                selectedLanguageName = Common.getLang(this@LanguageActivity)
                binding.backBtn.setOnClickListener {
                    finish()
                }

            }
        }
        getLanguage()
        changeLanguageDone()
    }

    override fun onResume() {
        super.onResume()
        if(!AppOpenManager.getInstance().isDismiss) {

        }
    }

    private fun changeLanguageDone() {
        binding.applyBtn.setOnClickListener {
            if (selectedLanguage != "") {
                Common.setPreLanguage(this@LanguageActivity, selectedLanguage)
                Common.setPreLanguageflag(this@LanguageActivity, selectedLanguageFlag)
                Common.setLang(this@LanguageActivity, selectedLanguageName)
                Log.d(
                    TAG,
                    "changeLanguageDone: $selectedLanguage and $selectedLanguageName"
                )
                if(!start) {
                    startActivity(Intent(this@LanguageActivity, SettingActivity::class.java))
                    val duration = System.currentTimeMillis() - now
                    analyticsLogger.logScreenGo("setting_screen", "language_screen", duration)
                } else {
                    if (RemoteConfig.INTER_LANGUAGE != "0") {
                        InterAds.showPreloadInter(
                            activity = this@LanguageActivity,
                            InterAds.ALIAS_INTER_LANGUAGE,
                            onLoadDone = {
                                Log.d(TAG, "onLoadDone")
                                nextScreenByCondition()
                            },
                            onLoadFailed = {
                                Log.d(TAG, "onLoadFailed")
                                nextScreenByCondition()
                            })
                    } else {
                        nextScreenByCondition()

                    }

                }
            } else {
                Toast.makeText(this, "Please select language", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun nextScreenByCondition() {
        val duration = System.currentTimeMillis() - now
        analyticsLogger.logScreenGo("intro_screen", "language_screen", duration)
        val intent = Intent(this@LanguageActivity, IntroActivityNew::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    private var isFirstTime = true
    private fun getLanguage() {
        adapter2 = LanguageAdapter(object: LanguageAdapter.OnClickListener {
            override fun onClickListener(position: Int, language: Language) {
                if(language.key != selectedLanguage || language.name != selectedLanguageName) {
                    binding.applyBtn.setTextColor(resources.getColor(R.color.white))
                    binding.applyBtn.setBackgroundResource(R.drawable.background_radius_16_purple)
                }
                selectedLanguage = language.key
                selectedLanguageFlag = language.img
                selectedLanguageName = language.name

                adapter2?.updatePosition(selectedLanguage)
                if(isFirstTime && start) {
                    isFirstTime = false
                }
            }

        })

        val selected = if(!start) Common.getPreLanguage(this@LanguageActivity) else ""
        adapter2?.updateData(allLanguages, selected )
        binding.allLanguages.layoutManager = LinearLayoutManager(this)
        binding.allLanguages.adapter = adapter2
    }


    private fun showAds(nativeHolderAdmob: NativeHolderAdmob = AdsManager.NATIVE_LANGUAGE) {
        Log.d(TAG, "LanguageActivity: ${RemoteConfig.NATIVE_LANGUAGE_070625}")
        try {
            when(RemoteConfig.NATIVE_LANGUAGE_070625) {
                "1" -> {
                    AdsManager.showNativeLanguage(this, binding.frNative , nativeHolderAdmob)
                }
                else -> {
                    binding.frNative.gone()
                }
            }
        }catch (_: Exception){
            binding.frNative.gone()
        }
    }

    override fun onBackPressed() {
        if(!start) {
            finish()
        } else {
            moveTaskToBack(true)
        }
    }

    companion object {
        var selectedLanguage = ""
        var selectedLanguageName = ""
        var selectedLanguageFlag = -1

        val TAG = LanguageActivity::class.java.simpleName
    }

}