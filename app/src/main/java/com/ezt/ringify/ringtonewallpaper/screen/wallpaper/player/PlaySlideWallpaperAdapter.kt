package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player

import alirezat775.lib.carouselview.CarouselAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemPhotoBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemVideoBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PlayerManager
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote

@OptIn(UnstableApi::class)
class PlaySlideWallpaperAdapter(
    private val context: Context,
    private val onRequestScrollToPosition: (Int) -> Unit,
    private val onClickListener: (Boolean, Int) -> Unit
) : CarouselAdapter() {

    private val items = mutableListOf<Wallpaper>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null
    private var livePlayingHolder: LivePlayerViewHolder? = null

    private  var isPlaying = false
    private  var isEnded = false



    override fun getItemCount() = items.size

    fun submitList(new: List<Wallpaper>) {
//        val diffCallback = WallpaperDiffCallback(items, new)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(new)
        notifyDataSetChanged()
    }

    fun setCurrentPlayingPosition(position: Int, playingSong: Boolean = false) {
        val previous = currentPos
        currentPos = position
        isPlaying = playingSong
        println("setCurrentPlayingPosition: $isPlaying")
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
        notifyItemChanged(currentPos)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val binding =
                ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PlayerViewHolder(binding)

    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        if (holder is LivePlayerViewHolder) {
            Log.d("Adapter", "onBindViewHolder called for position $position")
            val wallpaper = items[position]
            val isCurrent = position == currentPos

            holder.bind(wallpaper, isCurrent)

            if (isCurrent) {
                livePlayingHolder = holder
            }
        } else {
            (holder as PlayerViewHolder).bind(items[position], position)
            if (position == currentPos) {
                playingHolder = holder
                println("✅ playingHolder set at position $position")
            }
        }

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        PlayerManager.release()
        CacheUtil.release(context)
        super.onDetachedFromRecyclerView(recyclerView)
    }

    fun isItemFullyVisible(recyclerView: RecyclerView, position: Int): Boolean {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return false
        return( layoutManager.findFirstCompletelyVisibleItemPosition() == position ||
                layoutManager.findLastCompletelyVisibleItemPosition() == position).also {
                    println("isItemFullyVisible: $it")
        }
    }

    inner class PlayerViewHolder(val binding: ItemPhotoBinding) : CarouselViewHolder(binding.root) {
        private var slideshowHandler: Handler? = null
        private var slideshowRunnable: Runnable? = null
        private var currentImageIndex = 0

        private var isFirstImage = true // flag to track first image load

        @SuppressLint("ClickableViewAccessibility")
        fun bind(wallpaper: Wallpaper, pos: Int) {
            // Avoid unnecessary rebind

            // Set playingHolder for external progress updates
            if (wallpaper == RingtonePlayerRemote.currentPlayingRingtone) {
                playingHolder = this
            }

            stopSlideshow()
            val images = wallpaper.contents.map { it.url.medium }


            if (currentPos == position && wallpaper.contents.size > 1) {

                startSlideshowCrossfade(images)
            } else {
                binding.progressBar.visible()
                binding.loading.visible()
                binding.wallpaper3.visible()
                println("images 123: ${wallpaper.id}")
                val url = wallpaper.contents.firstOrNull()?.url?.medium
                if (url != null) {
                    wallpaper.contents.first().url.medium.let {
                        Glide.with(context).load(it).placeholder(R.color.white)
                            .error(
                                R.color.white
                            ).into(binding.wallpaper3)
                    }
                    binding.progressBar.gone()
                    binding.loading.gone()
                }
            }

            // Scroll to previous
            binding.previous.setOnClickListener {
                if (pos > 0) {
                    onRequestScrollToPosition(pos - 1)
                }
            }

            binding.leftView.setOnClickListener {
                val recyclerView = itemView.parent as? RecyclerView ?: return@setOnClickListener
                val position = bindingAdapterPosition

                if (!isItemFullyVisible(recyclerView, position)) return@setOnClickListener

                if (pos > 0) {
                    onRequestScrollToPosition(pos - 1)
                }
            }

            // Scroll to next
            binding.next.setOnClickListener {
                if (pos < items.lastIndex) {
                    onRequestScrollToPosition(pos + 1)
                }
            }

            binding.rightView.setOnClickListener {
                val recyclerView = itemView.parent as? RecyclerView ?: return@setOnClickListener
                val position = bindingAdapterPosition

                if (!isItemFullyVisible(recyclerView, position)) return@setOnClickListener

                if (pos < items.lastIndex) {
                    onRequestScrollToPosition(pos + 1)
                }
            }


            binding.ccIcon.setOnClickListener {
                println("ccIcon is here!")
//                val dialog = CreditDialog(context)
//                dialog.setCreditContent(ringtone)
//                dialog.show()
            }
        }

        private var isImageView1Visible = true
        private fun startSlideshowCrossfade(imageUrls: List<String>) {
            if (imageUrls.isEmpty()) return
            binding.progressBar.gone()
            binding.loading.gone()
            binding.wallpaper3.gone()

            slideshowHandler = Handler(Looper.getMainLooper())
            currentImageIndex = 0

            slideshowRunnable = object : Runnable {
                override fun run() {
                    if (currentImageIndex >= imageUrls.size) currentImageIndex = 0

                    val context = binding.wallpaper.context
                    val activityContext = context as? Activity ?: return
                    if (activityContext.isDestroyed) return

                    val nextImageUrl = imageUrls[currentImageIndex]

                    val visibleImageView =
                        if (isImageView1Visible) binding.wallpaper else binding.wallpaper2
                    val hiddenImageView =
                        if (isImageView1Visible) binding.wallpaper2 else binding.wallpaper

                    // Load next image into hidden ImageView
                    Glide.with(activityContext)
                        .load(nextImageUrl)
                        .error(R.drawable.icon_default_category)
                        .into(hiddenImageView)

                    // Animate crossfade
                    hiddenImageView.alpha = 0f
                    hiddenImageView.visibility = View.VISIBLE

                    hiddenImageView.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start()

                    visibleImageView.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            visibleImageView.visibility = View.GONE
                        }
                        .start()

                    // Swap visible ImageView flag
                    isImageView1Visible = !isImageView1Visible

                    currentImageIndex++
                    slideshowHandler?.postDelayed(this, 3000L)
                }
            }

            slideshowHandler?.post(slideshowRunnable!!)
        }



        fun stopSlideshow() {
            slideshowRunnable?.let { slideshowHandler?.removeCallbacks(it) }
            slideshowRunnable = null
            slideshowHandler = null
        }

    }

    inner class LivePlayerViewHolder(
        private val binding: ItemVideoBinding,
        private val context: Context
    ) : CarouselViewHolder(binding.root) {
        private var currentUrl: String? = null
        private var currentListener: Player.Listener? = null
        private var hasRenderedFirstFrame = false

        fun bind(wallpaper: Wallpaper, isCurrent: Boolean) {
            val videoUrl = wallpaper.contents.firstOrNull()?.url?.full

            println("LivePlayerViewHolder: $videoUrl and $isCurrent")

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
            val player = PlayerManager.getPlayer(context)

            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))

            val simpleCache = CacheUtil.getSimpleCache(context.applicationContext)
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val cacheFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

            val mediaSource = ProgressiveMediaSource.Factory(cacheFactory).createMediaSource(mediaItem)

            player.apply {
                stop()
                clearMediaItems()
                setMediaSource(mediaSource)
                clearVideoSurface()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1f

                currentListener?.let { removeListener(it) }
                currentListener = object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d("ExoPlayer", "State: $state")
                    }

                    override fun onRenderedFirstFrame() {
                        Log.d("ExoPlayer", "Rendered First Frame")
                        hasRenderedFirstFrame = true
                        binding.videoThumbnail.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        binding.playerView.alpha = 1f
                    }
                }
                addListener(currentListener!!)
            }

            binding.playerView.player = null
            binding.playerView.player = player
            binding.playerView.alpha = 0f
            binding.playerView.visibility = View.VISIBLE

            waitForPlayerViewAndPrepare(player)
        }

        private fun waitForPlayerViewAndPrepare(player: ExoPlayer) {
            if (binding.playerView.width > 0 && binding.playerView.height > 0 && binding.playerView.isAttachedToWindow) {
                Log.d("PlayerDebug", "View is ready: ${binding.playerView.width}x${binding.playerView.height}, preparing player")
                player.prepare()
            } else {
                binding.playerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (binding.playerView.width > 0 && binding.playerView.height > 0 && binding.playerView.isAttachedToWindow) {
                            binding.playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            Log.d("PlayerDebug", "Delayed prepare after full layout: ${binding.playerView.width}x${binding.playerView.height}")
                            player.prepare()
                        }
                    }
                })
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

    companion object {
        const val ITEM_VIDEO = 1
        const val ITEM_PHOTO = 0
    }
}