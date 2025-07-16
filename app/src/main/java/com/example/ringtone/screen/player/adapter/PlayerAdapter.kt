package com.example.ringtone.screen.player.adapter

import alirezat775.lib.carouselview.CarouselAdapter
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemMusicBinding
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.utils.Utils


class PlayerAdapter(
    private val exoPlayer: ExoPlayer
) : CarouselAdapter() {

    private val items = mutableListOf<Ringtone>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val updater = object : Runnable {
        override fun run() {
            playingHolder?.binding?.csb?.progress = exoPlayer.currentPosition.toFloat()
            handler.postDelayed(this, 500)
        }
    }

    override fun getItemCount() = items.size

    fun addAll(new: List<Ringtone>) {
        items.clear()
        items.addAll(new)
        super.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        (holder as PlayerViewHolder).bind(items[position], position)
    }

    override fun onViewRecycled(holder: CarouselViewHolder) {
        if (holder.layoutPosition == currentPos) {
            handler.removeCallbacks(updater)
            playingHolder = null
        }
        super.onViewRecycled(holder)
    }

    inner class PlayerViewHolder(val binding: ItemMusicBinding) : CarouselViewHolder(binding.root) {

        fun bind(ringtone: Ringtone, pos: Int) {
            binding.tvTime.text = Utils.formatDuration(ringtone.duration.toLong())
            binding.csb.max = ringtone.duration.toFloat()
            binding.csb.progress = if (pos == currentPos) exoPlayer.currentPosition.toFloat() else 0f
            binding.csb.isEnabled = pos == currentPos

            binding.play.setImageResource(
                if (pos == currentPos && exoPlayer.isPlaying) R.drawable.icon_pause
                else R.drawable.icon_play
            )

            binding.play.setOnClickListener {
                if (pos == currentPos) togglePlayPause()
                else switchTrack(ringtone, pos, this)
            }

        }

        private fun togglePlayPause() {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                handler.removeCallbacks(updater)
            } else {
                exoPlayer.play()
                handler.post(updater)
            }
            updatePlayIcon()
        }

        private fun switchTrack(ringtone: Ringtone, pos: Int, vh: PlayerViewHolder) {
            val prev = currentPos
            currentPos = pos
            notifyItemChanged(prev)
            notifyItemChanged(currentPos)

            handler.removeCallbacks(updater)
            exoPlayer.stop()
            exoPlayer.setMediaItem(MediaItem.fromUri(ringtone.contents.url))
            exoPlayer.prepare()
            exoPlayer.play()

            playingHolder = vh
            handler.post(updater)
        }

        private fun updatePlayIcon() {
            binding.play.setImageResource(
                if (exoPlayer.isPlaying) R.drawable.icon_pause else R.drawable.icon_play
            )
        }
    }
}
