package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemGridWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemLoadingBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote

class GridWallpaperAdapter(private val onClickListener: (Wallpaper) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onAllImagesLoaded: (() -> Unit)? = null
    private val allWallpapers: MutableList<Wallpaper> = mutableListOf()
    private lateinit var context: Context

    private var isPremium: Boolean = false
    private var isLive: Boolean = false

    private var imagesToLoadInBatch = 0
    private var imagesLoadedInBatch = 0

    companion object {
        const val VIEW_TYPE_CATEGORY = 0
        const val VIEW_TYPE_LOADING = 1
        const val INTERVAL = 12 // Show loading view every 12 items
    }

    override fun getItemCount(): Int {
        return allWallpapers.size
    }

    override fun getItemViewType(position: Int): Int {
        // Example: show loading view every INTERVAL items
        // For now, no loading view implemented; always return category view
        return VIEW_TYPE_CATEGORY
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return if (viewType == VIEW_TYPE_LOADING) {
            LoadingViewHolder(
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            GridWallpaperViewHolder(
                ItemGridWallpaperBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GridWallpaperViewHolder) {
            holder.bind(position)
        } else if (holder is LoadingViewHolder) {
            holder.bind()
        }
    }

    fun submitList(list: List<Wallpaper>, live: Boolean = false, premium: Boolean = false) {
        isPremium = premium
        isLive = live

        val start = allWallpapers.size
        allWallpapers.clear()
        allWallpapers.addAll(list)

        imagesToLoadInBatch = INTERVAL
        imagesLoadedInBatch = 0

        notifyItemRangeInserted(start, allWallpapers.size)
    }


    fun submitFavouriteList(
        list: List<Wallpaper>,
        live: Boolean = false,
        premium: Boolean = false
    ) {
        isPremium = premium
        isLive = live

        imagesToLoadInBatch = INTERVAL
        imagesLoadedInBatch = 0

        allWallpapers.clear()
        allWallpapers.addAll(list)

        // Because you clear the list and add new items,
        // call notifyDataSetChanged() instead of notifyItemRangeInserted()
        notifyDataSetChanged()
    }

    inner class GridWallpaperViewHolder(private val binding: ItemGridWallpaperBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val wallpaper = allWallpapers.getOrNull(position) ?: return

            binding.apply {
                liveIcon.visibility = if (isLive) View.VISIBLE else View.GONE
                premiumIcon.visibility = if (isPremium) View.VISIBLE else View.GONE

                val url = wallpaper.thumbnail?.url?.medium
                    ?: wallpaper.contents.firstOrNull()?.url?.full

                println("GridWallpaperViewHolder: ${wallpaper.id} and ${wallpaper.contents.first().url.full}")
                if (url != null) {
                    progressBar.visibility = View.VISIBLE
                    loading.visibility = View.VISIBLE

                    wallPaper.load(url) {
                        crossfade(true) // Optional fade animation
                        placeholder(R.drawable.item_wallpaper_default) // 👈 Show while loading
                        error(R.drawable.item_wallpaper_default) // 👈 Show if failed
                        listener(
                            onSuccess = { _, _ ->
                                progressBar.visibility = View.GONE
                                loading.visibility = View.GONE
                            },
                            onError = { _, _ ->
                                progressBar.visibility = View.GONE
                                loading.visibility = View.GONE
                            }
                        )
                    }
                } else {
                    progressBar.visibility = View.GONE
                    loading.visibility = View.GONE
                    wallPaper.setImageResource(R.drawable.item_wallpaper_default)
                }

                root.setOnClickListener {
                    RingtonePlayerRemote.setCurrentWallpaper(wallpaper)
                    RingtonePlayerRemote.setWallpaperQueue(allWallpapers)
                    onClickListener(wallpaper)

                }
            }
        }

        private fun onImageLoaded() {
            imagesLoadedInBatch++
            if (imagesLoadedInBatch >= imagesToLoadInBatch) {
                onAllImagesLoaded?.invoke()
            }
        }
    }


    inner class LoadingViewHolder(private val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            // Show or hide the loading spinner depending on image load state
            if (imagesLoadedInBatch < imagesToLoadInBatch) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
