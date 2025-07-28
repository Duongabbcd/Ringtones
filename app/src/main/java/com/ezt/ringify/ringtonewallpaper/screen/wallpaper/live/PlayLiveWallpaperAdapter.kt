package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import alirezat775.lib.carouselview.CarouselAdapter
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.DefaultDataSource
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.databinding.ItemVideoBinding

@OptIn(UnstableApi::class)
class PlayLiveWallpaperAdapter(private val context: Context) : CarouselAdapter() {
    private val items = mutableListOf<Wallpaper>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    fun submitList(newList: List<Wallpaper>) {
        println("submitList: ${newList.size}")
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun setCurrentPlayingPosition(position: Int) {
        val prev = currentPos
        currentPos = position
        if (prev != RecyclerView.NO_POSITION) notifyItemChanged(prev)
        notifyItemChanged(currentPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        Log.d("Adapter", "onBindViewHolder called for position $position")
        val wallpaper = items[position]
        val isCurrent = position == currentPos

        (holder as PlayerViewHolder).bind(wallpaper, isCurrent)

        if (isCurrent) {
            playingHolder = holder
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: CarouselViewHolder) {
        (holder as? PlayerViewHolder)?.detachPlayer()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        PlayerManager.release()
        CacheUtil.release(context)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    inner class PlayerViewHolder(
        private val binding: ItemVideoBinding,
        private val context: Context
    ) : CarouselViewHolder(binding.root) {

        private var currentUrl: String? = null
        private var currentListener: Player.Listener? = null

        fun bind(wallpaper: Wallpaper, isCurrent: Boolean) {
            val videoUrl = wallpaper.contents.firstOrNull()?.url?.full

            // If URL hasn't changed and current state matches, skip re-attaching player
            println("Before test 0 : $isCurrent")
            println("Before test 1 : $videoUrl")
            println("Before test 2 : $currentUrl")
            if (isCurrent && videoUrl != null && videoUrl == currentUrl) {
                Log.d("PlayerViewHolder", "bind: Already playing URL, skipping attach $videoUrl")
                return
            }

            currentUrl = videoUrl

            binding.ccIcon.setOnClickListener {
                // TODO: Show credit info if needed
            }

            println("bind() - isCurrent: $isCurrent and videoUrl: $videoUrl")

            if (isCurrent && !videoUrl.isNullOrEmpty()) {
                // Show thumbnail first
                showThumbnail(wallpaper.thumbnail?.url?.medium)
                binding.playerView.visibility = View.GONE
                binding.videoThumbnail.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.loading.visibility = View.GONE

                // Attach player with the URL
                attachPlayer(videoUrl)
            } else {
                detachPlayer()
                showThumbnail(wallpaper.thumbnail?.url?.medium)
                binding.videoThumbnail.visibility = View.VISIBLE
                binding.playerView.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                binding.loading.visibility = View.GONE

                // Remove click listener for non-current items
            }
        }

        fun attachPlayer(videoUrl: String) {
            Log.d("PlayerViewHolder", "attachPlayer() called with url: $videoUrl")
            val player = PlayerManager.getPlayer(context.applicationContext)
            val simpleCache = CacheUtil.getSimpleCache(context.applicationContext)

            val dataSourceFactory = DefaultDataSource.Factory(context)
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
                prepare()

                currentListener?.let { removeListener(it) }

                currentListener = object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d("ExoPlayer", "player state = $state")
                        when (state) {
                            Player.STATE_READY -> {
                                binding.progressBar.visibility = View.GONE
                                binding.videoThumbnail.visibility = View.GONE
                                binding.playerView.visibility = View.VISIBLE
                                binding.loading.visibility = View.GONE
                                Log.d("ExoPlayer", "Playback is ready and playing: $isPlaying")
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }


                addListener(currentListener!!)
                binding.playerView.player = this
            }
        }

        fun detachPlayer() {
            currentListener?.let {
                PlayerManager.getPlayer(context).removeListener(it)
                currentListener = null
            }
            binding.playerView.player = null
            binding.playerView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.loading.visibility = View.GONE
            currentUrl = null
            if (context is Activity && context.isDestroyed) return
            Glide.with(binding.videoThumbnail)
                .clear(binding.videoThumbnail) // <- clear thumbnail to prevent leak
        }

        private fun showThumbnail(videoUrl: String?) {
            binding.videoThumbnail.visibility = View.VISIBLE
            videoUrl?.let {
                Glide.with(context)
                    .asBitmap()
                    .load(it)
                    .centerCrop()
                    .into(binding.videoThumbnail)
            }
        }
    }
}
