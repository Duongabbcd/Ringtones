package com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentIconBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent

class AllIConAdapter(private val onClickListener: (String, String) -> Unit) :
    RecyclerView.Adapter<AllIConAdapter.AllIconViewHolder>() {
    private val allBackgrounds: MutableList<Pair<ImageContent, ImageContent>> = mutableListOf()
    private lateinit var context: Context
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllIconViewHolder {
        context = parent.context
        val binding =
            ItemContentIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllIconViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AllIconViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<Pair<ImageContent, ImageContent>>) {
        allBackgrounds.clear()
        allBackgrounds.addAll(list)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = allBackgrounds.size

    inner class AllIconViewHolder(private val binding: ItemContentIconBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val imageContent = allBackgrounds[position]

            binding.apply {
                val endCallIcon = imageContent.first.url.full
                val startCallIcon = imageContent.second.url.full

                Glide.with(context).load(endCallIcon).placeholder(R.drawable.icon_end_call)
                    .error(R.drawable.icon_end_call).into(endCall)
                Glide.with(context).load(startCallIcon).placeholder(R.drawable.icon_start_call)
                    .error(R.drawable.icon_start_call).into(startCall)
                // Highlight stroke if selected
                if (position == selectedPosition) {
                    container.setBackgroundResource(R.drawable.background_radius_16_purple)
                } else {
                    container.setBackgroundResource(R.drawable.background_radius_16_gray)
                }

                root.setOnClickListener {
                    println("AllIconViewHolder: $endCallIcon and $startCallIcon")
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onClickListener(endCallIcon, startCallIcon)
                }
            }
        }
    }

}