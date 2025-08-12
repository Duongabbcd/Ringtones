package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.endCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.startCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.backgroundUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.currentBackgroundUrl
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PlayerManager
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import androidx.core.net.toUri

class PreviewCallScreenActivity :
    BaseActivity<ActivityPreviewCallscreenBinding>(ActivityPreviewCallscreenBinding::inflate) {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)

        val start = prefs.getString("ANSWER", "") ?: ""
        if (start.isNotEmpty()) {
            startCall = start
        }
        val end = prefs.getString("CANCEL", "") ?: ""
        if (end.isNotEmpty()) {
            endCall = end
        }
        val avatar = prefs.getString("AVATAR", "") ?: ""
        if (avatar.isNotEmpty()) {
            avatarUrl = avatar
        }

        println("avatarUrl: $avatarUrl")

        binding.apply {

            Glide.with(this@PreviewCallScreenActivity).load(avatarUrl)
                .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
                .into(binding.avatar)

            Glide.with(this@PreviewCallScreenActivity).load(endCall)
                .placeholder(R.drawable.icon_end_call).error(R.drawable.icon_end_call)
                .into(binding.callEnd)
            Glide.with(this@PreviewCallScreenActivity).load(startCall)
                .placeholder(R.drawable.icon_start_call).error(R.drawable.icon_start_call)
                .into(binding.callAccept)

            closeBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@PreviewCallScreenActivity,
                    "INTER_CALLSCREEN"
                )
            }
            println("backgroundURL: $backgroundUrl")
            println("currentBackgroundUrl: $currentBackgroundUrl")
            player = ExoPlayer.Builder(this@PreviewCallScreenActivity).build()
            playerView.player = player as Player
            if(currentBackgroundUrl.isNotEmpty()) {
                attachPlayer(currentBackgroundUrl)
            } else {
                callScreenImage.visible()
                playerView.gone()
                if (backgroundUrl.isEmpty()) {
                    binding.callScreenImage.setBackgroundResource(R.drawable.default_callscreen)
                } else {
                    Glide.with(this@PreviewCallScreenActivity)
                        .load(backgroundUrl)
                        .placeholder(R.drawable.default_callscreen)
                        .error(R.drawable.default_callscreen)
                        .into(binding.callScreenImage)
                }
            }


            val placeholderDrawable = ContextCompat.getDrawable(
                this@PreviewCallScreenActivity,
                R.drawable.default_callscreen
            )
            Glide.with(this@PreviewCallScreenActivity)
                .load(backgroundUrl)
                .placeholder(placeholderDrawable)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        binding.callScreenImage.background = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        binding.callScreenImage.background = placeholder ?: placeholderDrawable
                    }
                })
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

        val mediaItem = MediaItem.fromUri(videoUrl.toUri())
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

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}