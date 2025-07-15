package com.example.ringtone.screen.home.subscreen.ringtone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.databinding.ItemRingtoneBinding
import com.example.ringtone.remote.model.Ringtone

class RingtoneAdapter: RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {
    private val allRingtones : MutableList<Ringtone> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RingtoneViewHolder {
        context = parent.context
        return RingtoneViewHolder(
            ItemRingtoneBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: RingtoneViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<Ringtone>) {
        allRingtones.clear()
        allRingtones.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =allRingtones.size

    inner class RingtoneViewHolder(private val binding: ItemRingtoneBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val ringTone = allRingtones[position]
            binding.apply {
                ringToneName.text = ringTone.name
                ringToneAuthor.text = ringTone.author.name

            }
        }
    }
}