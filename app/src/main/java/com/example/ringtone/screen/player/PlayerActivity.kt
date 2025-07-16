package com.example.ringtone.screen.player

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityPlayerBinding
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.screen.player.adapter.PlayerAdapter
import com.example.ringtone.utils.RingtonePlayerRemote
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private lateinit var handler: Handler
    private lateinit var playerAdapter: PlayerAdapter
    private var currentRingtone = RingtonePlayerRemote.currentPlayingRingtone

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedRingtones
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        initViewPager()

        binding.apply {

            backBtn.setOnClickListener {
                finish()
            }
            val index = allRingtones.indexOf(currentRingtone)

        }
    }

    private fun initViewPager() {
        playerAdapter = PlayerAdapter(RingtonePlayerRemote.allSelectedRingtones)
        val carousel = Carousel(this, binding.viewPager2, playerAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        binding.viewPager2.adapter = playerAdapter

        carousel.addCarouselListener(object : CarouselListener {
            override fun onPositionChange(position: Int) {
                currentRingtone = allRingtones[position]
                Log.d("PlayerActivity",  "onPositionChange $currentRingtone")
            }

            override fun onScroll(dx: Int, dy: Int) {
                Log.d("PlayerActivity",  "onScroll dx : $dx -- dy : $dx")
            }
        })

    }


    override fun onPause() {
        super.onPause()
//        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
//        handler.postDelayed(runnable, 3000)
    }




}
