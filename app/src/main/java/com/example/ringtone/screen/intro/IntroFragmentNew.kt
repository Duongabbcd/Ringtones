package com.example.ringtone.screen.intro

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.admob.max.dktlibrary.AdmobUtils
import kotlin.apply
import androidx.core.view.isVisible
import com.example.ringtone.R
import com.example.ringtone.databinding.ViewpagerIntroItempageBinding
import com.example.ringtone.screen.intro.IntroActivityNew.Companion.numberPage
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import com.musicplayer.mp3.playeroffline.ads.AdsManager
import com.musicplayer.mp3.playeroffline.ads.AdsManager.isTestDevice
import com.musicplayer.mp3.playeroffline.ads.RemoteConfig

class IntroFragmentNew : Fragment() {
    private val binding by lazy { ViewpagerIntroItempageBinding.inflate(layoutInflater) }
    private lateinit var callbackIntro: CallbackIntro
    private var position = 0
    private var reload = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity is CallbackIntro) callbackIntro = activity as CallbackIntro
        position = arguments?.getInt(ARG_POSITION) ?: 0
        if (arguments != null) {
            println("IntroActivityNew.numberPage: ${IntroActivityNew.numberPage}")
            when (IntroActivityNew.numberPage) {
                3 -> {
                    fragmentPosition3()
                }
                4 -> {
                    if (RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625.contains("1")  && !IntroActivityNew.isIntroFullFail1) {
                        fragmentPosition41()
                    }else if(RemoteConfig.NATIVE_FULL_SCREEN_INTRO_070625.contains("2") && !IntroActivityNew.isIntroFullFail1){
                        fragmentPosition42()
                    }else {
                        fragmentPosition3()
                    }
                }
                5 -> {
                    fragmentPosition5()
                }
            }
        }

        binding.nextBtn.setOnClickListener {
            var positionIntro = 0
            when (binding.title.text) {
                getString(R.string.intro_1) -> {
                    positionIntro = 1
                }
                getString(R.string.intro_2) -> {
                    positionIntro = 2
                }
                getString(R.string.intro_3) -> {
                    positionIntro = 3
                }
            }
            callbackIntro.onNext(position,positionIntro)

        }

        binding.skipBtn.setOnClickListener {
            callbackIntro.onNext(numberPage - 1,3)
        }



    }


    private fun fragmentPosition5() {
        showView(true)
        when (position) {
            0 -> {
                setUiIntro1()
            }

            1 -> {
                println("fragmentPosition51: ${AdsManager.NATIVE_FULL_SCREEN_INTRO} and $isTestDevice")
                AdsManager.showNativeFullScreen(requireActivity(), AdsManager.NATIVE_FULL_SCREEN_INTRO, binding.frNativeFull, true)
                showNativeFull()
            }

            2 -> {
                setUiIntro2()
//                binding.lottieSlide.visible()
            }

            3 ->{
                println("fragmentPosition53: ${AdsManager.NATIVE_FULL_SCREEN_INTRO} and $isTestDevice")
                AdsManager.showNativeFullScreen(requireActivity(), AdsManager.NATIVE_FULL_SCREEN_INTRO, binding.frNativeFull, true)
                showNativeFull()
            }

            4 -> {
                setUiIntro3()
            }
        }
    }

    private fun fragmentPosition41() {
        showView(true)
//        binding.lottieSlide.gone()
        when (position) {
            0 -> {
                setUiIntro1()
//                binding.lottieSlide.visible()
            }

            1 -> {
                println("fragmentPosition41: ${AdsManager.NATIVE_FULL_SCREEN_INTRO} and $isTestDevice")
                AdsManager.showNativeFullScreen(requireActivity(), AdsManager.NATIVE_FULL_SCREEN_INTRO, binding.frNativeFull, true)
                showNativeFull()
            }

            2 -> {
                setUiIntro2()
            }

            3 ->{
                setUiIntro3()
            }
        }
    }

    private fun fragmentPosition42() {
        showView(true)
        when (position) {
            0 -> {
                setUiIntro1()
            }

            1 -> {
                setUiIntro2()
            }

            2 -> {
                println("fragmentPosition42: ${AdsManager.NATIVE_FULL_SCREEN_INTRO} and $isTestDevice")
                AdsManager.showNativeFullScreen(requireActivity(), AdsManager.NATIVE_FULL_SCREEN_INTRO, binding.frNativeFull, true)
                showNativeFull()
            }

            3 ->{
                setUiIntro3()
            }
        }
    }

    private fun fragmentPosition3() {
        showView(true)
        when (position) {
            0 -> {
                setUiIntro1()
            }

            1 -> {
                setUiIntro2()
            }

            2 -> {
                setUiIntro3()
            }
        }
    }

    private fun setUiIntro3() {
        showNativeIntro(2)
        binding.title.text = getString(R.string.intro_3)
        val third = getString(R.string.desc_3)
        val highlight3 = getString(R.string.high_light_3)
        setSpannableString(third,highlight3,  binding.description)
        binding.image2.setImageResource(R.drawable.bg_intro3)
        binding.slideDot.setImageResource(R.drawable.third_intro)
        binding.introImage.setImageResource(R.drawable.icon_call)
        binding.introTitle.text = getString(R.string.intro_title_3)
        binding.skipBtn.gone()
//        binding.intro2.visible()
//     binding.intro3.gone()
//        binding.intro4.gone()
    }

    private fun setUiIntro2() {
        println("setUiIntro2")
        showNativeIntro(1)
        binding.title.text = getString(R.string.intro_2)
        val second = getString(R.string.desc_2)
        val highlight2 = getString(R.string.high_light_2)
        setSpannableString(second,highlight2,  binding.description)
        binding.image2.setImageResource(R.drawable.bg_intro2)
        binding.slideDot.setImageResource(R.drawable.second_intro)
        binding.introImage.setImageResource(R.drawable.icon_frame)
        binding.introTitle.text = getString(R.string.intro_title_2)
        binding.skipBtn.visible()
//        binding.intro2.visible()
//     binding.intro3.gone()
//        binding.intro4.gone()
    }

    private fun setUiIntro1() {
        showNativeIntro(0)
        binding.title.text = getString(R.string.intro_1)
        val first = getString(R.string.desc_1)
        val highlight1 = getString(R.string.high_light_1)
       setSpannableString(first,highlight1,  binding.description)
        binding.introTitle.text = getString(R.string.intro_title_1)
        binding.image2.setImageResource(R.drawable.bg_intro1)
        binding.introImage.setImageResource(R.drawable.icon_song)
        binding.slideDot.setImageResource(R.drawable.first_intro)
        binding.skipBtn.visible()
//        binding.intro2.visible()
//     binding.intro3.gone()
//        binding.intro4.gone()
    }

    private fun showView(isShow: Boolean) {
        binding.apply {
            if (!isShow && AdmobUtils.isNetworkConnected(requireActivity())) {
                scrollView.gone()
                bottomControlLayout.gone()
                frNative.gone()
                frNativeFull.visible()
                layoutFull.visible()
                closeAds.visible()
            } else {
                scrollView.visible()
                frNative.visible()
                bottomControlLayout.visible()
                frNativeFull.gone()
                layoutFull.gone()
//                binding.lottieSlide.visible()
                closeAds.gone()
            }
        }
    }

    private fun showNativeFull() {
//        binding.lottieSlide.gone()
        showView(false)
    }

    override fun onResume() {
        super.onResume()

        try {
            if(binding.layoutFull.isVisible){
                binding.closeAds.setOnClickListener {
                    callbackIntro.closeAds()
                }
                if(AdmobUtils.isNetworkConnected(requireContext())){
                    callbackIntro.disableSwip()
                    if(position == 1){
                        if(reload){
                            reload = false
                            return
                        }
                    }
                    AdsManager.showNativeFullScreen(requireActivity(), AdsManager.NATIVE_FULL_SCREEN_INTRO
                        , binding.frNativeFull)
                } else {
                    binding.frNativeFull.gone()
                }
            }
        }catch (_: Exception){}
    }

    private fun showAds() {
        println("showAds: ${RemoteConfig.ADS_INTRO_070625}")
        if (RemoteConfig.ADS_INTRO_070625.contains("2")) {
            AdsManager.loadAndShowNative(requireActivity(), binding.frNative, true, AdsManager.NATIVE_INTRO) {}
        } else {
            AdsManager.showNativeLanguageCustom(requireActivity(), binding.frNative, AdsManager.NATIVE_INTRO, isCheckTestDevice = true)
        }
    }

    private fun showNativeIntro(position: Int) {
        when (position) {
            0 -> {
                if (RemoteConfig.NATIVE_INTRO_070625.contains("1")) {
                    binding.frNative.visible()
                    showAds()
                } else {
                    binding.frNative.gone()
                }
            }

            1 -> {
                if (RemoteConfig.NATIVE_INTRO_070625.contains("2")) {
                    binding.frNative.visible()
                    showAds()
                } else {
                    binding.frNative.gone()
                }
            }

            2 -> {
                if (RemoteConfig.NATIVE_INTRO_070625.contains("3")) {
                    binding.frNative.visible()
                    showAds()
                } else {
                    binding.frNative.gone()
                }
            }
        }
    }

    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int): IntroFragmentNew {
            val fragment = IntroFragmentNew()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }

        fun setSpannableString(fullText: String, target: String, textView: TextView) {
            val spannable = SpannableString(fullText)

// Set all text to black (optional if default is black)
            spannable.setSpan(
                ForegroundColorSpan(Color.BLACK),
                0,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

// Highlight "Ringify" in purple
            val start = fullText.indexOf(target)
            if (start >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#8246FF")),
                    start,
                    start + target.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

// Apply to TextView
            textView.text = spannable
        }
    }



    interface CallbackIntro {
        fun onNext(position: Int, introPos : Int)
        fun closeAds()
        fun disableSwip()
    }
}