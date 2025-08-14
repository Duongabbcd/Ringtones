package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.endCall
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.photoBackgroundUrl
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.videoBackgroundUrl
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.setIcon
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.startCall
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PlayerManager
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible

class PreviewCallScreenActivity :
    BaseActivity<ActivityPreviewCallscreenBinding>(ActivityPreviewCallscreenBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            Glide.with(this@PreviewCallScreenActivity).load(avatarUrl)
                .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
                .into(binding.avatar)

            displayIcon(endCall, startCall)

            closeBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@PreviewCallScreenActivity,
                    "INTER_CALLSCREEN"
                )
            }

            println("videoBackgroundUrl: $videoBackgroundUrl")
            if (videoBackgroundUrl.isNotEmpty()) {
                playerView.visible()
                callScreenImage.gone()
                attachPlayer(videoBackgroundUrl)
            } else {
                playerView.gone()
                callScreenImage.visible()
                if (photoBackgroundUrl.isEmpty()) {
                    binding.callScreenImage.setBackgroundResource(R.drawable.default_callscreen)
                } else {
                    Glide.with(this@PreviewCallScreenActivity)
                        .load(photoBackgroundUrl)
                        .placeholder(R.drawable.default_callscreen)
                        .error(R.drawable.default_callscreen)
                        .override(1080, 1920)
                        .into(binding.callScreenImage)
                }
            }
        }
    }


    @OptIn(UnstableApi::class)
    fun attachPlayer(videoUrl: String) {
        Log.d("PlayerViewHolder", "attachPlayer() called with url: $videoUrl")
        val player = PlayerManager.getPlayer(this)
        val simpleCache = CacheUtil.getSimpleCache(this)

        val dataSourceFactory = DefaultDataSource.Factory(this)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player.apply {
            stop()
            clearMediaItems()
            setMediaSource(mediaSource)
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true

            // 👇 Delay prepare() to ensure playerView is ready
            binding.playerView.player = this
            binding.playerView.post {
                Log.d("PlayerViewHolder", "Calling prepare() after post")
                prepare()
            }
        }
    }


    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@PreviewCallScreenActivity, "INTER_CALLSCREEN")
    }

    private fun displayIcon(endCallIcon: String, startCallIcon: String) {
        binding.apply {
            setIcon(endCallIcon, callEnd, endCallLottie, R.drawable.icon_end_call)
            setIcon(startCallIcon, callAccept, startCallLottie, R.drawable.icon_start_call)
        }

    }

    override fun onStart() {
        super.onStart()
        if (videoBackgroundUrl.isNotEmpty()) {
            attachPlayer(videoBackgroundUrl)
        }
    }
}