package com.example.ringtone.screen.player.adapter

import alirezat775.lib.carouselview.CarouselAdapter
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemMusicBinding
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.utils.RingtonePlayerRemote
import com.example.ringtone.utils.Utils


class PlayerAdapter(private val onRequestScrollToPosition: (Int) -> Unit, private val onClickListener: (Boolean) -> Unit) : CarouselAdapter() {

    private val items = mutableListOf<Ringtone>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    private  var isPlaying = false
    private  var isEnded = false

    fun updateProgress(progress: Float) {
        val currentDuration = RingtonePlayerRemote.currentPlayingRingtone.duration.toFloat()
        val result = progress / 1000 % currentDuration
        println("updateProgress: $progress and $currentDuration and $result ")
        playingHolder?.binding?.csb?.progress = result
    }

    override fun getItemCount() = items.size

    fun submitList(new: List<Ringtone>) {
        val diffCallback = RingtoneDiffCallback(items, new)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(new)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setCurrentPlayingPosition(position: Int, playingSong : Boolean = false) {
        val previous = currentPos
        currentPos = position
        isPlaying = playingSong
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
        notifyItemChanged(currentPos)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        (holder as PlayerViewHolder).bind(items[position], position)
        if (position == currentPos) {
            playingHolder = holder
            println("✅ playingHolder set at position $position")
        }
    }

    fun onSongEnded() {
        isPlaying = false
        isEnded = true
        notifyItemChanged(currentPos)
    }

    inner class PlayerViewHolder(val binding: ItemMusicBinding) : CarouselViewHolder(binding.root) {
        private var boundId: Int = -1

        fun bind(ringtone: Ringtone, pos: Int) {
            // Avoid unnecessary rebind
            if (boundId != ringtone.id) {
                boundId = ringtone.id
                binding.tvTime.text = Utils.formatDuration(ringtone.duration.toLong())
                binding.csb.max = ringtone.duration.toFloat()
                binding.csb.progress = 0f
            }

            // Disable touch on seekbar
            binding.csb.setOnTouchListener { _, _ -> true }

            // Set playingHolder for external progress updates
            if (ringtone == RingtonePlayerRemote.currentPlayingRingtone) {
                playingHolder = this
            }


            // Scroll to previous
            binding.previous.setOnClickListener {
                if (pos > 0) {
                    onRequestScrollToPosition(pos - 1)
                }
            }

            binding.leftView.setOnClickListener {
                if (pos > 0) {
                    onRequestScrollToPosition(pos - 1)
                }
            }

            // Scroll to next
            binding.next.setOnClickListener {
                if (pos < items.lastIndex) {
                    onRequestScrollToPosition(pos + 1)
                }
            }

            binding.rightView.setOnClickListener {
                if (pos < items.lastIndex) {
                    onRequestScrollToPosition(pos + 1)
                }
            }

            if(isEnded) {
                updateProgress(0f)
                binding.play.setImageResource(R.drawable.icon_pause)
            }

            binding.play.setOnClickListener {
                isPlaying = !isPlaying
                isEnded = !isEnded
                if(isEnded) {
                    updateProgress(0f)
                    binding.play.setImageResource(R.drawable.icon_pause)
                }
                binding.play.setImageResource(
                    if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
                )
                onClickListener(isPlaying)
            }
        }
    }

}

class RingtoneDiffCallback(
    private val oldList: List<Ringtone>,
    private val newList: List<Ringtone>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos].id == newList[newPos].id
    }

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }
}
