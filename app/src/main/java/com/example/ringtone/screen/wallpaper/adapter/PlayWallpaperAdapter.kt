package com.example.ringtone.screen.wallpaper.adapter

import alirezat775.lib.carouselview.CarouselAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemPhotoBinding
import com.example.ringtone.remote.model.Wallpaper
import com.example.ringtone.utils.RingtonePlayerRemote


class PlayWallpaperAdapter(private val onRequestScrollToPosition: (Int) -> Unit, private val onClickListener: (Boolean, Int) -> Unit) : CarouselAdapter() {

    private val items = mutableListOf<Wallpaper>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    private  var isPlaying = false
    private  var isEnded = false

    private lateinit var context: Context


    override fun getItemCount() = items.size

    fun submitList(new: List<Wallpaper>) {
//        val diffCallback = WallpaperDiffCallback(items, new)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(new)
        notifyDataSetChanged()
    }

    fun setCurrentPlayingPosition(position: Int, playingSong : Boolean = false) {
        val previous = currentPos
        currentPos = position
        isPlaying = playingSong
        println("setCurrentPlayingPosition: $isPlaying")
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
        notifyItemChanged(currentPos)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        context = parent.context
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        (holder as PlayerViewHolder).bind(items[position], position)
        if (position == currentPos) {
            playingHolder = holder
            println("✅ playingHolder set at position $position")
        }
    }

    fun onSongEnded() {
        isPlaying = false
        isEnded = true
        notifyItemChanged(currentPos)
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
                startSlideshow(images)
            } else {
                val url = wallpaper.contents.firstOrNull()?.url?.medium
                if (url != null) {
                    wallpaper.contents.first().url.medium.let {
                        Glide.with(context).load(it).placeholder(R.drawable.item_wallpaper_default)
                            .error(
                                R.drawable.item_wallpaper_default
                            ).into(binding.wallpaper)
                    }
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


        private fun startSlideshow(imageUrls: List<String>) {
            slideshowHandler = Handler(Looper.getMainLooper())
            currentImageIndex = 0
            slideshowRunnable = object : Runnable {
                override fun run() {
                    val context = binding.wallpaper.context
                    if (currentImageIndex >= imageUrls.size) currentImageIndex = 0

                    if (context is Activity && context.isDestroyed) return
                    Glide.with(context.applicationContext)
                        .load(imageUrls[currentImageIndex])
                        .error(R.drawable.icon_default_category)
                        .into(binding.wallpaper)

                    currentImageIndex++
                    slideshowHandler?.postDelayed(this, 3000L) // Slide every 3 seconds
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

class WallpaperDiffCallback(
    private val oldList: List<Wallpaper>,
    private val newList: List<Wallpaper>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos].id == newList[newPos].id
    }

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }
}
