package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemRingtoneBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class RingtoneAdapter(private val onClickListener: (Ringtone) -> Unit) :
    RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {
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
        val start = allRingtones.size
        allRingtones.clear()
        allRingtones.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }

    fun submitList1(list: List<Ringtone>) {
        allRingtones.clear()
        allRingtones.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = allRingtones.size

    inner class RingtoneViewHolder(private val binding: ItemRingtoneBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val ringTone = allRingtones[position]
            binding.apply {
                println("RingtoneViewHolder: $ringTone")
                ringToneName.text = ringTone.name
                ringToneAuthor.text =
                    ringTone.author?.name ?: context.resources.getString(R.string.unknwon_author)

                if (ringTone == Ringtone.EMPTY_RINGTONE) {
                    ringToneName.text = context.resources.getString(R.string.unknwon_title)
                    ringToneAuthor.text = context.resources.getString(R.string.unknwon_author)
                }

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
                    onClickListener(ringTone)
                    RingtonePlayerRemote.setRingtoneQueue(allRingtones)
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