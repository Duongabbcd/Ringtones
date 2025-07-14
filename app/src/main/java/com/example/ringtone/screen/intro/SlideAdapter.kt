package com.example.ringtone.screen.intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ringtone.screen.intro.IntroFragmentNew

class SlideAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
       return IntroActivityNew.numberPage
    }

    override fun createFragment(position: Int): Fragment {
        return IntroFragmentNew.newInstance(position)
    }
}
