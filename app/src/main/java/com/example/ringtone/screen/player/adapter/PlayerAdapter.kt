package com.example.ringtone.screen.player.adapter

import alirezat775.lib.carouselview.CarouselAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.databinding.ItemMusicBinding
import com.example.ringtone.remote.model.Ringtone

class PlayerAdapter(private val allRingtones: MutableList<Ringtone> = mutableListOf()) :  CarouselAdapter() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarouselViewHolder {
        context = parent.context
        return PlayerViewHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: CarouselViewHolder,
        position: Int
    ) {
      if(holder is PlayerViewHolder) {
          holder.bind(position)
      }
    }

    private lateinit var context: Context


    override fun getItemCount(): Int = allRingtones.size

    inner class PlayerViewHolder(private val binding: ItemMusicBinding )  :CarouselViewHolder(binding.root){
        fun bind(position: Int) {
            val ringtone = allRingtones[position]
            binding.apply {
                    println("PlayerViewHolder: $ringtone")
//                root.setOnClickListener {
//                }
            }
        }
    }
}