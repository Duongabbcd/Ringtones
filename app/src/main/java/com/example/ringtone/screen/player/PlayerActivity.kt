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
import androidx.media3.common.Player
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SimpleItemAnimator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private lateinit var handler: Handler
    private val playerAdapter: PlayerAdapter by lazy {
        PlayerAdapter(onRequestScrollToPosition = { newPosition ->
                binding.viewPager2.smoothScrollToPosition(newPosition)
            setUpNewPlayer(newPosition)
            }
        ){  result , id ->
            if(result) {
                if(id != currentId) {
                    playRingtone(true)
                    currentId = id
                } else {
                    if (exoPlayer.currentPosition >= exoPlayer.duration) {
                        exoPlayer.seekTo(0)
                    }
                    exoPlayer.play()
                }
            } else {
                exoPlayer.pause()
            }
        }
    }

    private var currentId = -10


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
        println("▶️ Starting playback & posting progressUpdater")
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                println("🎧 onIsPlayingChanged: $isPlaying")
                if (isPlaying) {
                    handler.post(progressUpdater)
                } else {
                    handler.removeCallbacks(progressUpdater)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    println("🎵 Song ended")
                    playerAdapter.onSongEnded()
                }
            }
        })
    }

    private val progressUpdater = object : Runnable {
        override fun run() {
            println("⏱ progressUpdater running...")
            if (exoPlayer.isPlaying) {
                val progress = exoPlayer.currentPosition.toFloat()
                println("⏳ Progress: $progress")
                playerAdapter.updateProgress(progress)
                handler.postDelayed(this, 1000)
            } else {
                println("⏸️ Not playing, stopping progress updates.")
            }
        }
    }


    private var currentRingtone = RingtonePlayerRemote.currentPlayingRingtone

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedRingtones
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        playRingtone(false)
        initViewPager()

        binding.apply {
            currentRingtoneName.isSelected = true
            backBtn.setOnClickListener {
                finish()
            }
            val index = allRingtones.indexOf(currentRingtone)
            viewPager2.scrollToPosition(index)
        }
    }

    private fun setUpNewPlayer(position: Int) {
        currentRingtone = allRingtones[position]
        RingtonePlayerRemote.currentPlayingRingtone = allRingtones[position]
        playerAdapter.setCurrentPlayingPosition(position)
        Log.d("PlayerActivity",  "onPositionChange $currentRingtone")
        binding.currentRingtoneName.text = currentRingtone.name
        binding.currentRingtoneAuthor.text = currentRingtone.author.name
        exoPlayer.release()
        handler.removeCallbacks(progressUpdater)
    }

    private fun initViewPager() {
        playerAdapter.submitList(allRingtones)
        val carousel = Carousel(this, binding.viewPager2, playerAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scrollSpeed(100f)
        carousel.scaleView(true)

        val recyclerView = binding.viewPager2.getChildAt(0) as? RecyclerView

        recyclerView?.let {
            it.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            PagerSnapHelper().attachToRecyclerView(it)

            // Disable flickering during item change animations
            it.itemAnimator?.changeDuration = 0
            (it.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }


        binding.apply {
            viewPager2.adapter = playerAdapter


            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    currentId = -10
                    setUpNewPlayer(position)
                    // 🔁 force rebind to update playingHolder
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
        handler.removeCallbacks(progressUpdater)
        exoPlayer.release()
    }





}
