package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.adapter

import alirezat775.lib.carouselview.CarouselAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemMusicBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.CreditDialog
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.ezt.ringify.ringtonewallpaper.utils.Utils


class PlayRingtoneAdapter(
    private val onRequestScrollToPosition: (Int) -> Unit,
    private val onClickListener: (Boolean, Int) -> Unit,
    private val onCurrentIdChanged: (Int) -> Unit
) : CarouselAdapter() {

    private val items = mutableListOf<Ringtone>()
    private var currentPos = RecyclerView.NO_POSITION
    private var previousPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    private var isPlaying = false

    private lateinit var context: Context

    fun updateProgress(progressMs: Float) {
        val durationSeconds = RingtonePlayerRemote.currentPlayingRingtone.duration.toFloat()

        if (durationSeconds <= 0f) {
            println("❌ Invalid duration: $durationSeconds")
            return
        }

        val progressSeconds = (progressMs / 1000f).coerceAtMost(durationSeconds)
        println("✅ updateProgress: $progressMs ms -> $progressSeconds sec of $durationSeconds sec")

        playingHolder?.binding?.csb?.progress = progressSeconds
    }

    override fun getItemCount() = items.size

    fun submitList(new: List<Ringtone>) {
        items.clear()
        items.addAll(new)
        notifyDataSetChanged()
    }

    fun setCurrentPlayingPosition(position: Int, playingSong: Boolean = false) {
        previousPos = currentPos
        currentPos = position
        isPlaying = playingSong
        println("setCurrentPlayingPosition: $position isPlaying=$isPlaying")
        notifyItemChanged(previousPos)
        notifyItemChanged(currentPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        context = parent.context
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

    inner class PlayerViewHolder(val binding: ItemMusicBinding) : CarouselViewHolder(binding.root) {
        private var boundId: Int = -1

        @SuppressLint("ClickableViewAccessibility")
        fun bind(ringtone: Ringtone, pos: Int) {
            println("boundId: $boundId and ${ringtone.id} and ${RingtonePlayerRemote.currentPlayingRingtone.id}")
            if (boundId != ringtone.id) {
                boundId = ringtone.id
                binding.tvTime.text = Utils.formatDuration(ringtone.duration.toLong())
                binding.csb.max = ringtone.duration.toFloat()
            }
            // Disable manual seeking
            binding.csb.setOnTouchListener { _, _ -> true }

            val isCurrent = ringtone == RingtonePlayerRemote.currentPlayingRingtone
            val currentProgressSec = if (isCurrent) {
                RingtonePlayerRemote.exoPlayer?.currentPosition?.toFloat()?.div(1000f)
                    ?.coerceAtMost(ringtone.duration.toFloat()) ?: 0f
            } else {
                0f
            }

            binding.csb.progress = currentProgressSec

            if (ringtone == RingtonePlayerRemote.currentPlayingRingtone) {
                playingHolder = this
            }

            // Previous / Next buttons
            binding.previous.setOnClickListener {
                if (pos > 0) onRequestScrollToPosition(pos - 1)
            }
            binding.next.setOnClickListener {
                if (pos < items.lastIndex) onRequestScrollToPosition(pos + 1)
            }

            // Update play button icon based on isPlaying & currentPos
            val currentlyPlayingThis = (pos == currentPos) && isPlaying
            binding.play.setImageResource(
                if (currentlyPlayingThis) R.drawable.icon_pause else R.drawable.icon_play
            )

            // Play button click: just notify activity; no local toggle here
            binding.play.setOnClickListener {
                onCurrentIdChanged(ringtone.id)
                onClickListener(!currentlyPlayingThis, ringtone.id)
            }

            // Credit dialog click
            binding.ccIcon.setOnClickListener {
                val dialog = CreditDialog(context)
                dialog.setCreditRingtone(ringtone)
                dialog.show()
            }
        }
    }
}
