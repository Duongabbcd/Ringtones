package com.example.ringtone.screen.home.subscreen.first_screen.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.databinding.ItemRingtoneBinding
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.screen.player.PlayerActivity
import com.example.ringtone.utils.RingtonePlayerRemote
import com.example.ringtone.R
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class RingtoneAdapter(private val isPopular: Boolean = false): RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {
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

    override fun getItemCount(): Int = allRingtones.size

    inner class RingtoneViewHolder(private val binding: ItemRingtoneBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val ringTone = allRingtones[position]
            binding.apply {
                ringToneName.text = ringTone.name
                ringToneAuthor.text = ringTone.author.name

                if(ringTone.trend == 1) {
                    marker.visible()
                    marker.text = context.getString(R.string.hot_title)
                    marker.setBackgroundResource(R.drawable.orange_bg_gradient)
                } else if(isWithin7Days(ringTone.createdAt)) {
                    marker.visible()
                    marker.text = context.getString(R.string.new_title)
                    marker.setBackgroundResource(R.drawable.green_bg_gradient)
                } else {
                    marker.gone()
                }

                root.setOnClickListener {
                    RingtonePlayerRemote.setCurrentRingtone(ringTone)
                    if(!isPopular) {
                        RingtonePlayerRemote.setPlayingQueue(allRingtones)
                    }
                    context.startActivity(Intent(context, PlayerActivity::class.java))
                }
            }
        }
    }

    private fun isWithin7Days(timestamp: String): Boolean {
        // Define the format used in the timestamp
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH)
        format.timeZone = TimeZone.getTimeZone("UTC")

        return try {
            // Parse the timestamp
            val date = format.parse(timestamp)
            val timeInMillis = date?.time ?: return false

            // Get current time
            val now = System.currentTimeMillis()

            // Calculate difference in milliseconds
            val diff = now - timeInMillis

            // Check if the timestamp is within the last 7 days
            diff <= TimeUnit.DAYS.toMillis(7)
        } catch (e: Exception) {
            false
        }
    }
}