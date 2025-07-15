package com.example.ringtone.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentCallscreenBinding
import com.example.ringtone.databinding.FragmentRingtoneBinding
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