package com.example.ringtone.screen.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.admob.max.dktlibrary.AdmobUtils
import kotlin.apply
import androidx.core.view.isVisible
import com.example.ringtone.R
import com.example.ringtone.databinding.ViewpagerIntroItempageBinding
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
        binding.image2.setImageResource(R.drawable.bg_intro3)
        binding.slideDot.setImageResource(R.drawable.icon_intro_3)
        binding.intro2.gone()
//        binding.intro3.visible()
        binding.intro4.visible()
    }

    private fun setUiIntro2() {
        println("setUiIntro2")
        showNativeIntro(1)
        binding.title.text = getString(R.string.intro_2)
        binding.image2.setImageResource(R.drawable.bg_intro2)
        binding.slideDot.setImageResource(R.drawable.icon_intro_2)
        binding.intro2.visible()
//        binding.intro3.gone()
        binding.intro4.gone()
    }

    private fun setUiIntro1() {
        showNativeIntro(0)
        binding.title.text = getString(R.string.intro_1)
        binding.image2.setImageResource(R.drawable.bg_intro1)
        binding.slideDot.setImageResource(R.drawable.icon_intro_1)
        binding.intro2.gone()
//        binding.intro3.gone()
        binding.intro4.gone()
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
    }

    interface CallbackIntro {
        fun onNext(position: Int, introPos : Int)
        fun closeAds()
        fun disableSwip()
    }
}