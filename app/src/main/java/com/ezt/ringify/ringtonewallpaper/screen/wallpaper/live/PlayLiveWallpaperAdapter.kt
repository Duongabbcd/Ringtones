package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import alirezat775.lib.carouselview.CarouselAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.databinding.ItemVideoBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide

@OptIn(UnstableApi::class)
class PlayLiveWallpaperAdapter(
    private val onRequestScrollToPosition: (Int) -> Unit,
    private val onClickListener: (Boolean, Int) -> Unit
) : CarouselAdapter() {
    private val items = mutableListOf<Wallpaper>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    // ✅ Shared ExoPlayer instance
    private var exoPlayer: ExoPlayer? = null


    fun submitList(newList: List<Wallpaper>) {
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
        exoPlayer?.release()
        exoPlayer = null
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
            currentUrl = videoUrl

            binding.ccIcon.setOnClickListener {
                // TODO: Show credit info if needed
            }

            if (isCurrent && !videoUrl.isNullOrEmpty()) {
                attachPlayer(videoUrl)
            } else {
                detachPlayer()
                showThumbnail(videoUrl)
            }
        }

        fun attachPlayer(videoUrl: String) {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(context).build()
            }

            binding.playerView.player = exoPlayer
            binding.playerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.loading.visibility = View.GONE
            binding.videoThumbnail.visibility = View.VISIBLE

            val mediaItem = MediaItem.fromUri(videoUrl)
            val cacheFactory = ExoPlayerCache.buildCacheDataSourceFactory(context)
            val mediaSource =
                ProgressiveMediaSource.Factory(cacheFactory).createMediaSource(mediaItem)

            exoPlayer?.apply {
                setMediaSource(mediaSource)
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
                prepare()

                // Remove old listener if present
                currentListener?.let { removeListener(it) }

                // Create and save new listener
                currentListener = object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            binding.progressBar.visibility = View.GONE
                            binding.videoThumbnail.visibility = View.GONE
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
            // Remove the listener on detach to avoid callbacks on non-visible ViewHolder
            currentListener?.let {
                exoPlayer?.removeListener(it)
                currentListener = null
            }
            binding.playerView.player = null
            binding.playerView.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
        }

        private fun showThumbnail(videoUrl: String?) {
            binding.videoThumbnail.visibility = View.VISIBLE
            videoUrl?.let {
                Glide.with(context)
                    .asBitmap()
                    .load(it)
                    .frame(0)
                    .override(400, 400)
                    .centerCrop()
                    .into(binding.videoThumbnail)
            }
        }
    }

}
