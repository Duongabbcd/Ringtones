package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ezt.ringify.ringtonewallpaper.base.BaseFragment
import com.ezt.ringify.ringtonewallpaper.databinding.FragmentCallscreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallScreenFragment: BaseFragment<FragmentCallscreenBinding>(FragmentCallscreenBinding::inflate) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = CallScreenFragment().apply { }

    }

}