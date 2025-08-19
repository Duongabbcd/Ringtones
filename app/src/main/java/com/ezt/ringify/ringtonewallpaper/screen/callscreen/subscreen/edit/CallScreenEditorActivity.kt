package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallscreenBackgroundBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentItem
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.ContentViewModel
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter.AllAvatarAdapter
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter.AllBackgroundAdapter
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter.AllIConAdapter
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview.PreviewCallScreenActivity
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.endCall
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.photoBackgroundUrl
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.setIcon
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.startCall
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen.CallScreenFragment.Companion.videoBackgroundUrl
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PlayerManager
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class CallScreenEditorActivity :
    BaseActivity<ActivityCallscreenBackgroundBinding>(ActivityCallscreenBackgroundBinding::inflate) {
    private val contentViewModel: ContentViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val allBackgroundAdapter: AllBackgroundAdapter by lazy {
        AllBackgroundAdapter { input ->
            if (input.contents.isEmpty()) {
                return@AllBackgroundAdapter
            }

            hasRenderedFirstFrame = false
            currentListener?.let {
                PlayerManager.getPlayer(this).removeListener(it)
                currentListener = null
            }
            val allContents = input.contents
            val result = if (allContents.size >= 2) "" else allContents.first().url.medium
            val currentUrl = if (allContents.size >= 2) allContents.first().url.full else ""
            photoBackgroundUrl = result
            videoBackgroundUrl = currentUrl

            Log.d(TAG, "AllBackgroundAdapter: $photoBackgroundUrl ----- $videoBackgroundUrl")
            if (videoBackgroundUrl.isNotEmpty()) {
                binding.playerContainer.visible()
                binding.currentCallScreen.gone()
                displayVideoBackground(videoBackgroundUrl)
            } else {
                binding.playerContainer.gone()
                binding.currentCallScreen.visible()
                displayPhotoBackground(photoBackgroundUrl)
            }
        }
    }

    private var player: Player? = null
    private var currentListener: Player.Listener? = null
    private var hasRenderedFirstFrame = false
    private val allAvatarAdapter: AllAvatarAdapter by lazy {
        AllAvatarAdapter { input ->
            Log.d(TAG, "AllBackgroundAdapter: $input")
            if (input.isEmpty()) {
                return@AllAvatarAdapter
            }
            avatarUrl = input
            displayAvatar(input)
        }
    }

    private val allIconAdapter: AllIConAdapter by lazy {
        AllIConAdapter { end, start ->
            Log.d(TAG, "AllBackgroundAdapter: $end and $start")
            if (end.isEmpty() || start.isEmpty()) {
                return@AllIConAdapter
            }
            endCall = end
            startCall = start
            displayIcon(end, start)
        }
    }

    private val editorType by lazy {
        intent.getIntExtra("editorType", 0)
    }

    @OptIn(UnstableApi::class)
    private fun displayVideoBackground(videoBackgroundUrl: String) {
        Log.d(
            "PlayerViewHolder",
            "attachPlayer() called with url: $videoBackgroundUrl"
        )
        val player = PlayerManager.getPlayer(this)
        val simpleCache = CacheUtil.getSimpleCache(this)

        val dataSourceFactory = DefaultDataSource.Factory(this)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaItem = MediaItem.fromUri(Uri.parse(videoBackgroundUrl))
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
                Log.d(
                    "PlayerViewHolder",
                    "Calling prepare() after post"
                )
                prepare()
            }

            currentListener?.let { removeListener(it) }
            currentListener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    Log.d("ExoPlayer", "player state = $state")
                    if (state == Player.STATE_READY && !hasRenderedFirstFrame) {
                        binding.progressBar2.visibility = View.VISIBLE
                        binding.playerView.alpha = 0f
                        binding.playerView.visibility = View.VISIBLE
                    }
                }

                override fun onRenderedFirstFrame() {
                    if (!hasRenderedFirstFrame) {
                        hasRenderedFirstFrame = true
                        binding.progressBar2.visibility = View.GONE
                        binding.playerView.visibility = View.VISIBLE
                        binding.playerView.alpha = 1f
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        binding.progressBar2.visibility = View.GONE
                    }
                }
            }

            addListener(currentListener!!)
        }
    }

    private fun displayPhotoBackground(input: String) {
        Glide.with(this@CallScreenEditorActivity).load(input)
            .placeholder(R.drawable.default_callscreen).error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)
    }

    private fun displayAvatar(input: String) {
        Glide.with(this@CallScreenEditorActivity).load(input)
            .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
            .into(binding.defaultAvatar)
    }

    private fun displayIcon(endCallIcon: String, startCallIcon: String) {
        binding.apply {
            setIcon(endCallIcon, endImage, endCallLottie, R.drawable.icon_end_call)
            setIcon(startCallIcon, startImage, startCallLottie, R.drawable.icon_start_call)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBanner(this)
        Glide.with(this@CallScreenEditorActivity).load(avatarUrl)
            .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
            .into(binding.defaultAvatar)

        displayIcon(endCallIcon = endCall, startCallIcon = startCall)

        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@CallScreenEditorActivity,
                    "INTER_CALLSCREEN"
                )
            }

            when (editorType) {
                1 -> {
                    allBackground.adapter = allBackgroundAdapter
                    editorScreenName.text = getString(R.string.background)
                }

                2 -> {
                    allBackground.adapter = allAvatarAdapter
                    editorScreenName.text = getString(R.string.avatar)
                }

                3 -> {
                    allBackground.adapter = allIconAdapter
                    editorScreenName.text = getString(R.string.icon)
                }

                else -> {
                    allBackground.adapter = allBackgroundAdapter
                    editorScreenName.text = getString(R.string.background)
                }
            }

            allBackground.layoutManager =
                LinearLayoutManager(
                    this@CallScreenEditorActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            displayPhotoBackground(photoBackgroundUrl)

            contentViewModel.loading.observe(this@CallScreenEditorActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        ContentItem.CONTENT_EMPTY
                    }
                    allBackgroundAdapter.submitList(loadingItems)

                    // Disable scrolling
                    this@CallScreenEditorActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    contentViewModel.selectCallScreenContent.value?.let { items ->
                        Log.d(TAG, "allBackgroundAdapter: $items")
                        allBackgroundAdapter.submitList(items)
                    }

                    // Re-enable touch
                    this@CallScreenEditorActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }

            contentViewModel.loading1.observe(this@CallScreenEditorActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        ImageContent.IMAGE_EMPTY
                    }
                    allAvatarAdapter.submitList(loadingItems)

                    // Disable scrolling
                    this@CallScreenEditorActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    contentViewModel.backgroundContent.value?.let { items ->
                        Log.d(TAG, "allAvatarAdapter: $items")
                        allAvatarAdapter.submitList(items)
                    }
                    // Re-enable touch
                    this@CallScreenEditorActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }

            contentViewModel.loading2.observe(this@CallScreenEditorActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        Pair<ImageContent, ImageContent>(
                            ImageContent.IMAGE_EMPTY,
                            ImageContent.IMAGE_EMPTY
                        )
                    }
                    allIconAdapter.submitList(loadingItems)

                    // Disable scrolling
                    this@CallScreenEditorActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    contentViewModel.iconContent.value?.let { items ->
                        Log.d(TAG, "allIconAdapter: $items")
                        allIconAdapter.submitList(items)
                    }
                    // Re-enable touch
                    this@CallScreenEditorActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }

            previewBtn.setOnClickListener {
                startActivity(
                    Intent(
                        this@CallScreenEditorActivity,
                        PreviewCallScreenActivity::class.java
                    )
                )
            }

            applyBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@CallScreenEditorActivity)
            }
        }
    }

    private fun checkInternetConnected(isConnected: Boolean) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            when (editorType) {
                1 -> contentViewModel.getAllCallScreenBackgrounds()
                2 -> contentViewModel.getAllCallScreenAvatars()
                3 -> contentViewModel.getAllCallScreenIcons()
            }

            loadMoreData()
            binding.noInternet.root.gone()
        }
    }


    private var isLoadingMore = false
    private fun loadMoreData() {
        binding.allBackground.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx <= 0) return  // Only when scrolling right
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isAtBottom = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 2
                if (isAtBottom && !isLoadingMore) {
                    isLoadingMore = true
                    when (editorType) {
                        1 -> contentViewModel.getAllCallScreenBackgrounds()
                        2 -> contentViewModel.getAllCallScreenAvatars()
                        3 -> contentViewModel.getAllCallScreenIcons()
                    }
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_CALLSCREEN, InterAds.INTER_CALLSCREEN)

        if (videoBackgroundUrl.isNotEmpty()) {
            binding.playerContainer.visible()
            binding.currentCallScreen.gone()
            displayVideoBackground(videoBackgroundUrl)
        } else {
            binding.playerContainer.gone()
            binding.currentCallScreen.visible()
            displayPhotoBackground(photoBackgroundUrl)
        }
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
        binding.playerView.player = null  // Detach view safely
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@CallScreenEditorActivity, "INTER_CALLSCREEN")
    }

    companion object {
        val TAG = CallScreenEditorActivity.javaClass.simpleName
    }

}