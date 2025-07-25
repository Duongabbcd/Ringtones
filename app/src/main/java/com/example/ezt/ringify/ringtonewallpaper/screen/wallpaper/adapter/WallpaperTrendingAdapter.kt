package com.example.ringtone.screen.wallpaper.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.R
import com.example.ringtone.remote.model.Category

class WallpaperTrendingAdapter() : RecyclerView.Adapter<WallpaperTrendingAdapter.HotSearchViewHolder>() {
    private lateinit var context: Context
    private val items : MutableList<Category> = mutableListOf()
    inner class HotSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagText: TextView = itemView.findViewById(R.id.tagText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotSearchViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hot_search, parent, false)
        return HotSearchViewHolder(view)
    }

    fun submitList(list: List<Category>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: HotSearchViewHolder, position: Int) {
        val wallpaper = items[position]
        holder.tagText.text = "${position + 1}. ${wallpaper.name}"
        holder.itemView.setOnClickListener {
            //do nothing
        }
    }

    override fun getItemCount() = items.size
}