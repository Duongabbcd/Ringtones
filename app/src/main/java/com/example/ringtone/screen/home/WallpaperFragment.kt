package com.example.ringtone.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentRingtoneBinding
import com.example.ringtone.databinding.FragmentRingtoneBinding.inflate
import com.example.ringtone.databinding.FragmentWallpaperBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WallpaperFragment: BaseFragment<FragmentWallpaperBinding>(FragmentWallpaperBinding::inflate) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = WallpaperFragment().apply { }

    }

}