package com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentAvatarBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent

class AllAvatarAdapter(private val onClickListener: (String) -> Unit) :
    RecyclerView.Adapter<AllAvatarAdapter.AllIconViewHolder>() {
    private val allBackgrounds: MutableList<ImageContent> = mutableListOf()
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllIconViewHolder {
        context = parent.context
        val binding =
            ItemContentAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllIconViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AllIconViewHolder,
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

    inner class AllIconViewHolder(private val binding: ItemContentAvatarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val imageContent = allBackgrounds[position]

            binding.apply {
                val input = imageContent.url.medium


                // Highlight stroke if selected
                if (position == selectedPosition) {
                    binding.circleImage.strokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context, R.color.selectBottom
                        )
                    )
                    binding.circleImage.strokeWidth = 4f
                } else {
                    binding.circleImage.strokeColor = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context, R.color.customGray
                        )
                    )
                    binding.circleImage.strokeWidth = 0f
                }

                Glide.with(context).load(input).placeholder(R.drawable.default_callscreen)
                    .error(R.drawable.default_callscreen).into(circleImage)

                root.setOnClickListener {
                    println("AllIconViewHolder: $input")
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