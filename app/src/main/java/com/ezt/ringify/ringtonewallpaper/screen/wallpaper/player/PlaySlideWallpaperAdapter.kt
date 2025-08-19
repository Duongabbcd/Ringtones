package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player

import alirezat775.lib.carouselview.CarouselAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.core.view.isVisible
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemPhotoBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.CreditDialog
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

    private  var isPlaying = false
    private  var isEnded = false
    private  var isPremiumType = false

    override fun getItemCount() = items.size

    fun submitList(new: List<Wallpaper>, isPremium: Boolean = false) {
        println("PlaySlideWallpaperAdapter: ${new.firstOrNull()}")
//        isPremiumType = isPremium
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
        (holder as PlayerViewHolder).bind(items[position], position)
        if (position == currentPos) {
            playingHolder = holder
            println("✅ playingHolder set at position $position")
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

    private var slideshowInterval: Long = 3000L // default 3s

    fun setSlideshowInterval(interval: Long) {
        slideshowInterval = interval
        notifyDataSetChanged() // rebind to apply new interval
    }

    inner class PlayerViewHolder(val binding: ItemPhotoBinding) : CarouselViewHolder(binding.root) {
        private var slideshowHandler: Handler? = null
        private var slideshowRunnable: Runnable? = null
        private var currentImageIndex = 0

        private var isFirstImage = true // flag to track first image load


        @SuppressLint("ClickableViewAccessibility")
        fun bind(wallpaper: Wallpaper, pos: Int) {
            binding.ccIcon.setOnClickListener {
                val dialog = CreditDialog(context)
                dialog.setCreditWallpaper()
                dialog.show()
            }
            // Avoid unnecessary rebind

            binding.premiumIcon.isVisible = isPremiumType

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
                        Glide.with(context).load(it).placeholder(R.drawable.item_wallpaper_default)
                            .error(
                                R.drawable.item_wallpaper_default
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
                    slideshowHandler?.postDelayed(this, slideshowInterval)
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
}