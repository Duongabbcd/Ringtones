package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemBigCategoryBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote


class WallpaperAdapter(private val onClickListener: (Wallpaper) -> Unit): RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {
    private val allWallpapers : MutableList<Wallpaper> = mutableListOf()
    private val limitedWallpapers: MutableList<Wallpaper> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WallpaperViewHolder {
        context = parent.context
        return WallpaperViewHolder(
            ItemWallpaperBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var premium = false
    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: WallpaperViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<Wallpaper>, isPremium: Boolean = false) {
        allWallpapers.clear()
        allWallpapers.addAll(list)


        limitedWallpapers.clear()
        limitedWallpapers.addAll(list.take(10))

        premium = isPremium
        println("WallpaperAdapter: ${list.firstOrNull()}")
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = limitedWallpapers.size

    inner class WallpaperViewHolder(private val binding: ItemWallpaperBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val wallpaper = limitedWallpapers[position]
            binding.apply {
                val content = wallpaper.contents.firstOrNull()
                if (content == null) {
                    wallPaper.setImageResource(R.drawable.icon_default_category)
                    return@apply
                }
                val url = wallpaper.thumbnail?.url?.small ?: content.url.small
                url?.let {
                    Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.icon_default_category)
                        .error(R.drawable.icon_default_category)
                        .listener(object : RequestListener<Drawable> {

                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                e?.logRootCauses("GlideError")  // logs detailed causes
                                Log.e("Glide", "Load failed", e)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean = false
                        })
                        .into(wallPaper)
                } ?: wallPaper.setImageResource(R.drawable.icon_default_category)

                root.setOnClickListener {
                    RingtonePlayerRemote.setCurrentWallpaper(wallpaper)
                    RingtonePlayerRemote.setWallpaperQueue(allWallpapers)
                    onClickListener(wallpaper)
                }
            }
        }

    }
}