package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.adapter

import alirezat775.lib.carouselview.CarouselAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemMusicBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.CreditDialog
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.ezt.ringify.ringtonewallpaper.utils.Utils
import kotlin.math.round


class PlayRingtoneAdapter(private val onRequestScrollToPosition: (Int) -> Unit, private val onClickListener: (Boolean, Int) -> Unit) : CarouselAdapter() {

    private val items = mutableListOf<Ringtone>()
    private var currentPos = RecyclerView.NO_POSITION
    private var playingHolder: PlayerViewHolder? = null

    private  var isPlaying = false
    private  var isEnded = false

    private lateinit var context: Context

    fun updateProgress(progress: Float) {
        val currentDuration = RingtonePlayerRemote.currentPlayingRingtone.duration.toFloat()
        val result = round(progress) / 1000 % currentDuration
        println("updateProgress: $progress and $currentDuration and ${round(result)} ")
        playingHolder?.binding?.csb?.progress = round(result)
    }

    override fun getItemCount() = items.size

    fun submitList(new: List<Ringtone>) {
        val start = items.size

        items.clear()
        items.addAll(new)
        notifyItemRangeInserted(start, items.size)
    }

    fun setCurrentPlayingPosition(position: Int, playingSong : Boolean = false) {
        val previous = currentPos
        currentPos = position
        isPlaying = playingSong
        println("setCurrentPlayingPosition: $position $isPlaying")
        if (previous != RecyclerView.NO_POSITION) notifyItemChanged(previous)
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

    fun onSongEnded() {
        isPlaying = false
        isEnded = true
        notifyItemChanged(currentPos)
    }

    fun isItemFullyVisible(recyclerView: RecyclerView, position: Int): Boolean {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return false
        return( layoutManager.findFirstCompletelyVisibleItemPosition() == position ||
                layoutManager.findLastCompletelyVisibleItemPosition() == position).also {
                    println("isItemFullyVisible: $it")
        }
    }

    inner class PlayerViewHolder(val binding: ItemMusicBinding) : CarouselViewHolder(binding.root) {
        private var boundId: Int = -1

        @SuppressLint("ClickableViewAccessibility")
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
                val recyclerView = itemView.parent as? RecyclerView ?: return@setOnClickListener
                val position = bindingAdapterPosition

                if (!isItemFullyVisible(recyclerView, position)) return@setOnClickListener
            }

            // Scroll to next
            binding.next.setOnClickListener {
                if (pos < items.lastIndex) {
                    onRequestScrollToPosition(pos + 1)
                }
            }

            binding.rightView.setOnClickListener {
                val recyclerView = itemView.parent as? RecyclerView ?: return@setOnClickListener
                val position = bindingAdapterPosition

                if (!isItemFullyVisible(recyclerView, position)) return@setOnClickListener

            }

            if( !isPlaying) {
                binding.play.setImageResource(R.drawable.icon_play)
                updateProgress(0f)
            } else {
                binding.play.setImageResource(R.drawable.icon_pause)
            }

            binding.play.setOnClickListener {
                isPlaying = !isPlaying
                binding.play.setImageResource(
                    if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
                )
                onClickListener(isPlaying, ringtone.id)
            }

            binding.ccIcon.setOnClickListener {
                val dialog = CreditDialog(context)
                dialog.setCreditRingtone(ringtone)
                dialog.show()
            }
        }
    }

}
