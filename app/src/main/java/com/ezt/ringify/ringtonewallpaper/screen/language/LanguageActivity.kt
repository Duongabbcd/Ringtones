package com.ezt.ringify.ringtonewallpaper.screen.language

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.admob.max.dktlibrary.AppOpenManager
import com.admob.max.dktlibrary.utils.admod.NativeHolderAdmob
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityLanguageBinding
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroActivityNew
import com.ezt.ringify.ringtonewallpaper.screen.language.adapter.Language
import com.ezt.ringify.ringtonewallpaper.screen.language.adapter.LanguageAdapter
import com.ezt.ringify.ringtonewallpaper.screen.setting.SettingActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.GlobalConstant
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.NATIVE_LANGUAGE_ID2
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity


class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate){
    private var adapter2: LanguageAdapter? = null
    private var start = false

    private lateinit var allLanguages : ArrayList<Language>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        allLanguages = GlobalConstant.getListLocation(this@LanguageActivity)


        start = intent.getBooleanExtra("fromSplash", false)
        binding.apply {
            showAds()
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
            println("changeLanguageDone: $selectedLanguage and $selectedLanguageName")
            if (selectedLanguage != "") {
                Common.setPreLanguage(this@LanguageActivity, selectedLanguage)
                if(!start) {
                    startActivity(Intent(this@LanguageActivity, SettingActivity::class.java))
                } else {
                    if (RemoteConfig.INTER_WALLPAPER != "0") {
                        AdsManager.loadAndShowInterSP2(this, AdsManager.INTER_LANGUAGE, "INTER_LANGUAGE", object: AdsManager.AdListenerWithNative {
                            override fun onAdClosedOrFailed() {
                                nextScreenByCondition()
                            }

                            override fun onAdClosedOrFailedWithNative() {
                                nextScreenByCondition()
                            }

                        },
                            isCheckTestDevice = false
                        )
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
        val count = Common.getCountOpenApp(this)
        if (count < 1) {
            startActivity(Intent(this@LanguageActivity, IntroActivityNew::class.java))
            finish()
        } else {
            startActivity(Intent(this@LanguageActivity, MainActivity::class.java))
            finish()
        }
    }


    private var isFirstTime = true
    private fun getLanguage() {
        adapter2 = LanguageAdapter(object: LanguageAdapter.OnClickListener {
            override fun onClickListener(position: Int, language: Language) {
                if(language.key != selectedLanguage || language.name != selectedLanguageName) {
                    binding.applyBtn.setBackgroundResource(R.drawable.background_radius_12)
                } else {
                    binding.applyBtn.setBackgroundResource(R.drawable.background_radius_12_gray)
                }
                selectedLanguage = language.key
                selectedLanguageName = language.name

                adapter2?.updatePosition(selectedLanguage)
                if(isFirstTime && start) {
                    isFirstTime = false
                    showAds(NATIVE_LANGUAGE_ID2)
                }
            }

        })

        val selected = if(!start) Common.getPreLanguage(this@LanguageActivity) else ""
        adapter2?.updateData(allLanguages, selected )
        binding.allLanguages.layoutManager = LinearLayoutManager(this)
        binding.allLanguages.adapter = adapter2
//        binding.rcvLanguageList.setHasFixedSize(true)
    }


    private fun showAds(nativeHolderAdmob: NativeHolderAdmob = AdsManager.NATIVE_LANGUAGE) {
        println("LanguageActivity: ${RemoteConfig.NATIVE_LANGUAGE_070625}")
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
        super.onBackPressed()
        if(!start) {
            finish()
        } else {
            moveTaskToBack(true)
        }
    }

    companion object {
        var selectedLanguage = ""
        var selectedLanguageName = ""
    }

}