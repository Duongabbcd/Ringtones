package com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentIconBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible

class AllIConAdapter(private val onClickListener: (ImageContent, ImageContent) -> Unit) :
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

    fun submitList(list: List<Pair<ImageContent, ImageContent>>, index: Int? = null) {
        allBackgrounds.clear()
        allBackgrounds.addAll(list)
        selectedPosition = index ?: RecyclerView.NO_POSITION
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

                println("startCallIcon: $startCallIcon")
                println("endCallIcon: $endCallIcon")

                if (startCallIcon.endsWith(".json", true)) {
                    gifIcon.visible()
                } else {
                    gifIcon.gone()
                }

                if (imageContent.first == ImageContent.IMAGE_EMPTY) {
                    startCall.setImageResource(R.drawable.icon_start_call)
                    endCall.setImageResource(R.drawable.icon_end_call)
                } else {
                    displayIcons(startCallIcon, endCallIcon)
                }

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
                    onClickListener(imageContent.first, imageContent.second)
                }
            }
        }

        private fun displayIcons(startCallIcon: String, endCallIcon: String) {
            binding.apply {
                if (startCallIcon.endsWith(".json", true)) {
                    LottieCompositionFactory.fromUrl(context, startCallIcon)
                        .addListener { composition ->
                            val lottieDrawable = LottieDrawable().apply {
                                setComposition(composition)
                                progress = 0f // First frame only
                            }
                            startCall.setImageDrawable(lottieDrawable)
                        }
                } else {
                    Glide.with(context).load(startCallIcon).placeholder(R.drawable.icon_start_call)
                        .error(R.drawable.icon_start_call).into(startCall)
                }

                if (endCallIcon.endsWith(".json", true)) {
                    LottieCompositionFactory.fromUrl(context, endCallIcon)
                        .addListener { composition ->
                            val lottieDrawable = LottieDrawable().apply {
                                setComposition(composition)
                                progress = 0f // First frame only
                            }
                            endCall.setImageDrawable(lottieDrawable)
                        }
                } else {
                    Glide.with(context).load(endCallIcon).placeholder(R.drawable.icon_end_call)
                        .error(R.drawable.icon_end_call).into(endCall)
                }
            }
        }


    }

}