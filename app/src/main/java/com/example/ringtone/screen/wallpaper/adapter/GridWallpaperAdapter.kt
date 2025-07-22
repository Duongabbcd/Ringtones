package com.example.ringtone.screen.wallpaper.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemGridWallpaperBinding
import com.example.ringtone.databinding.ItemLoadingBinding
import com.example.ringtone.remote.model.Wallpaper
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible


class GridWallpaperAdapter(
    private val onClickListener: (Wallpaper) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onAllImagesLoaded: (() -> Unit)? = null
    private val allCategories: MutableList<Wallpaper> = mutableListOf()
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
//        val extra = allCategories.size / INTERVAL
        return allCategories.size
    }

//    override fun getItemViewType(position: Int): Int {
//        return if ((position + 1) % (INTERVAL + 1) == 0) VIEW_TYPE_LOADING else VIEW_TYPE_CATEGORY
//    }

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
            val dataIndex = position - (position / (INTERVAL + 1))
            holder.bind(dataIndex)
        } else if (holder is LoadingViewHolder) {
            holder.bind()
        }
    }

    fun submitList(list: List<Wallpaper>, live: Boolean = false, premium: Boolean = false) {
        isPremium = premium
        isLive = live

        allCategories.clear()
        allCategories.addAll(list)

        imagesToLoadInBatch = INTERVAL
        imagesLoadedInBatch = 0

        notifyDataSetChanged()
    }

    inner class GridWallpaperViewHolder(private val binding: ItemGridWallpaperBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val wallpaper = allCategories.getOrNull(position) ?: return

            binding.apply {
                liveIcon.visibility = if (isLive) View.VISIBLE else View.GONE
                premiumIcon.visibility = if (isPremium) View.VISIBLE else View.GONE

                val url = wallpaper.contents.firstOrNull()?.url?.medium
                if (url != null) {
                    Glide.with(wallPaper.context)
                        .load(url)
                        .placeholder(R.drawable.icon_default_category)
                        .error(R.drawable.icon_default_category)
                        .into(wallPaper)
                }

                root.setOnClickListener {
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
