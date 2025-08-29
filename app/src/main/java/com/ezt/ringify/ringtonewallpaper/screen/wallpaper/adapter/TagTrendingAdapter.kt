package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.model.Tag

class TagTrendingAdapter(private val onClickListener: (Tag) -> Unit) :
    RecyclerView.Adapter<TagTrendingAdapter.HotSearchViewHolder>() {
    private lateinit var context: Context
    private val items: MutableList<Tag> = mutableListOf()
    inner class HotSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagText: TextView = itemView.findViewById(R.id.tagText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotSearchViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_trending, parent, false)
        return HotSearchViewHolder(view)
    }

    fun submitList(list: List<Tag>) {
        println("TagTrendingAdapter: $list")
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: HotSearchViewHolder, position: Int) {
        val tag = items[position]
        holder.tagText.text = "${tag.name}"
        holder.itemView.setOnClickListener {
            onClickListener(tag)
        }
    }

    override fun getItemCount() = items.size
}