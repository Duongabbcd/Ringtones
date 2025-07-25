package com.ezt.ringify.ringtonewallpaper.screen.favourite

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroActivityNew
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew

class FavouriteAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
       return FavouriteActivity.Companion.numberPage
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FavouriteRingtoneFragment.newInstance(0)
            else -> FavouriteWallpaperFragment.newInstance(1)
        }
    }
}
