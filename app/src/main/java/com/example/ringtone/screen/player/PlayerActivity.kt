package com.example.ringtone.screen.player

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityPlayerBinding
import com.example.ringtone.screen.player.adapter.PlayerAdapter
import com.example.ringtone.utils.RingtonePlayerRemote
import androidx.media3.common.MediaItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private lateinit var handler: Handler
    private lateinit var playerAdapter: PlayerAdapter
    lateinit var exoPlayer: ExoPlayer

    private fun playRingtone(isPlaying: Boolean = false) {
        exoPlayer = ExoPlayer.Builder(this).build()
        // Prepare the media item
        if(!isPlaying) return
        val mediaItem = MediaItem.fromUri(currentRingtone.contents.url)
        exoPlayer.setMediaItem(mediaItem)
        // Prepare and start playback
        exoPlayer.prepare()
        exoPlayer.play()
    }



    private var currentRingtone = RingtonePlayerRemote.currentPlayingRingtone

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedRingtones
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        // Initialize ExoPlayer

        playerAdapter = PlayerAdapter(exoPlayer)
        initViewPager()

        binding.apply {

            backBtn.setOnClickListener {
                finish()
            }
            val index = allRingtones.indexOf(currentRingtone)
            viewPager2.scrollToPosition(index)
        }
    }

    private fun initViewPager() {
        playerAdapter.submitList(allRingtones)
        val carousel = Carousel(this, binding.viewPager2, playerAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        binding.apply {
            viewPager2.adapter = playerAdapter


            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    currentRingtone = allRingtones[position]
                    Log.d("PlayerActivity",  "onPositionChange $currentRingtone")
                    currentRingtoneName.text = currentRingtone.name
                    currentRingtoneAuthor.text = currentRingtone.author.name
                    exoPlayer.release()

                }

                override fun onScroll(dx: Int, dy: Int) {
                    Log.d("PlayerActivity",  "onScroll dx : $dx -- dy : $dx")
                }
            })

         viewPager2.addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when(newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            // User started scrolling
                            Log.d("WheelPicker", "User is actively scrolling")
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // User started scrolling

                        }
                    }
                }
            })
        }


    }


    override fun onPause() {
        super.onPause()
        exoPlayer.release()
    }





}
