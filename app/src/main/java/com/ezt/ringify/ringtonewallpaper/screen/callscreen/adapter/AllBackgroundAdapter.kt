package com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentBackgroundBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent


class AllBackgroundAdapter(private val onClickListener: (String) -> Unit) :
    RecyclerView.Adapter<AllBackgroundAdapter.AllBackgroundViewHolder>() {
    private val allBackgrounds: MutableList<ImageContent> = mutableListOf()
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllBackgroundViewHolder {
        context = parent.context
        val binding =
            ItemContentBackgroundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllBackgroundViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AllBackgroundViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<ImageContent>) {
        allBackgrounds.clear()
        allBackgrounds.addAll(list)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = allBackgrounds.size

    inner class AllBackgroundViewHolder(private val binding: ItemContentBackgroundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val imageContent = allBackgrounds[position]

            binding.apply {
                val input = imageContent.url.medium

                Glide.with(context).load(input).placeholder(R.drawable.default_callscreen)
                    .error(R.drawable.default_callscreen).into(callScreenBackground)

                // Highlight stroke if selected
                if (position == selectedPosition) {
                    binding.callScreenBackground.strokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context, R.color.selectBottom
                        )
                    )
                    binding.callScreenBackground.strokeWidth = 4f
                } else {
                    binding.callScreenBackground.strokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context, R.color.customGray
                        )
                    )
                    binding.callScreenBackground.strokeWidth = 0f
                }


                root.setOnClickListener {
                    println("AllBackgroundViewHolder: $input")
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onClickListener(input)
                }
            }
        }
    }

}