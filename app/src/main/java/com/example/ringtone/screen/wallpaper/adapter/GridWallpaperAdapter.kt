package com.example.ringtone.screen.wallpaper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemBigCategoryBinding
import com.example.ringtone.databinding.ItemGridWallpaperBinding
import com.example.ringtone.databinding.ItemWallpaperBinding
import com.example.ringtone.remote.model.Wallpaper


class GridWallpaperAdapter(private val onClickListener: (Wallpaper) -> Unit): RecyclerView.Adapter<GridWallpaperAdapter.GridWallpaperViewHolder>() {
    private val allWallpapers : MutableList<Wallpaper> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GridWallpaperViewHolder {
        context = parent.context
        return GridWallpaperViewHolder(
            ItemGridWallpaperBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: GridWallpaperViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<Wallpaper>) {
        allWallpapers.clear()
        allWallpapers.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =allWallpapers.size

    inner class GridWallpaperViewHolder(private val binding: ItemGridWallpaperBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val wallpaper = allWallpapers[position]
            binding.apply {
                println("ringTone: ${wallpaper.contents.first().url.full}")
                if(wallpaper.contents.first() == null) {
                    return@apply
                }
                wallpaper.contents.first().url.medium.let {
                    Glide.with(wallPaper).load(it).placeholder(R.drawable.icon_default_category).error(
                        R.drawable.icon_default_category).into(binding.wallPaper)
                }

                root.setOnClickListener {
                    onClickListener(wallpaper)
                }
            }
        }
    }
}