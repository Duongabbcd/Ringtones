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
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.CreditDialog

@OptIn(UnstableApi::class)
class PlayLiveWallpaperAdapter(private val context: Context) : CarouselAdapter() {
    private val items = mutableListOf<Wallpaper>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    fun submitList(newList: List<Wallpaper>) {
        println("submitList: ${newList.size}")
        val start = items.size
        items.clear()
        items.addAll(newList)

        notifyItemRangeInserted(start, newList.size)
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
        private var hasRenderedFirstFrame = false

        fun bind(wallpaper: Wallpaper, isCurrent: Boolean) {
            val videoUrl = wallpaper.contents.firstOrNull()?.url?.full

            binding.ccIcon.setOnClickListener {
                val dialog = CreditDialog(context)
                dialog.setCreditWallpaper()
                dialog.show()
            }

            if (!isCurrent) {
                // Not current item — reset state and show thumbnail only
                hasRenderedFirstFrame = false
                detachPlayer()
                currentUrl = null

                binding.videoThumbnail.visibility = View.VISIBLE
                binding.videoThumbnail.alpha = 1f

                binding.progressBar.visibility = View.GONE

                binding.playerView.visibility = View.GONE
                binding.playerView.alpha = 0f
                showThumbnail(wallpaper.thumbnail?.url?.medium)
                return
            }

            // Current item
            if (videoUrl == null) {
                // No video URL — treat as non-current
                hasRenderedFirstFrame = false
                detachPlayer()
                currentUrl = null
                binding.videoThumbnail.visibility = View.VISIBLE
                binding.videoThumbnail.alpha = 1f
                binding.progressBar.visibility = View.GONE
                binding.playerView.visibility = View.GONE
                binding.playerView.alpha = 0f
                showThumbnail(wallpaper.thumbnail?.url?.medium)
                return
            }

            if (videoUrl != currentUrl) {
                // New video URL — reset state and attach new player
                hasRenderedFirstFrame = false
                currentUrl = videoUrl

                binding.videoThumbnail.visibility = View.VISIBLE
                binding.videoThumbnail.alpha = 1f

                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.alpha = 1f


                binding.playerView.visibility = View.VISIBLE
                binding.playerView.alpha = 0f

                showThumbnail(wallpaper.thumbnail?.url?.medium)
                attachPlayer(videoUrl)
            } else {
                // Same video URL, restore visibility & alpha based on first frame rendered
                if (hasRenderedFirstFrame) {
                    binding.playerView.visibility = View.VISIBLE
                    binding.playerView.alpha = 1f

                    binding.videoThumbnail.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.playerView.visibility = View.VISIBLE
                    binding.playerView.alpha = 0f

                    binding.videoThumbnail.visibility = View.VISIBLE
                    binding.videoThumbnail.alpha = 1f

                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.alpha = 1f

                }
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

                // 👇 Delay prepare() to ensure playerView is ready
                binding.playerView.player = this
                binding.playerView.post {
                    Log.d("PlayerViewHolder", "Calling prepare() after post")
                    prepare()
                }

                currentListener?.let { removeListener(it) }
                currentListener = object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d("ExoPlayer", "player state = $state")
                        if (state == Player.STATE_READY && !hasRenderedFirstFrame) {
                            binding.videoThumbnail.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                            binding.playerView.alpha = 0f
                            binding.playerView.visibility = View.VISIBLE
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        if (!hasRenderedFirstFrame) {
                            hasRenderedFirstFrame = true
                            binding.videoThumbnail.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                            binding.playerView.visibility = View.VISIBLE
                            binding.playerView.alpha = 1f
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }

                addListener(currentListener!!)
            }
        }

        fun detachPlayer() {
            currentListener?.let {
                PlayerManager.getPlayer(context).removeListener(it)
                currentListener = null
            }
            binding.playerView.player = null
            binding.playerView.visibility = View.GONE
            binding.playerView.alpha = 0f
            binding.progressBar.visibility = View.GONE
            currentUrl = null
            hasRenderedFirstFrame = false
            if (context is Activity && context.isDestroyed) return
            Glide.with(binding.videoThumbnail)
                .clear(binding.videoThumbnail)
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
