package com.example.ringtone.screen.favourite

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ringtone.screen.intro.IntroActivityNew
import com.example.ringtone.screen.intro.IntroFragmentNew

class FavouriteAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
       return FavouriteActivity.Companion.numberPage
    }

    override fun createFragment(position: Int): Fragment {
        return FavouriteFragmentNew.newInstance(position)
    }
}
