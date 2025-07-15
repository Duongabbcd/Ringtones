package com.example.ringtone.screen.player.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.databinding.ItemMusicBinding
import com.example.ringtone.remote.model.Ringtone

class PlayerAdapter(private val allRingtones: MutableList<Ringtone> = mutableListOf()) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        context = parent.context
        return PlayerViewHolder(
            ItemMusicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: PlayerViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }


    override fun getItemCount(): Int = allRingtones.size

    inner class PlayerViewHolder(private val binding: ItemMusicBinding )  : RecyclerView.ViewHolder(binding.root){
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