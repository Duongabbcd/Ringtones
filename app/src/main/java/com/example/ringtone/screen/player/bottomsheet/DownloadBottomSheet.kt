package com.example.ringtone.screen.player.bottomsheet

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.ringtone.base.BaseBottomSheetDialog
import com.example.ringtone.databinding.BottomSheetDownloadBinding
import com.example.ringtone.R
class DownloadBottomSheet(private val context: Context, private val type: String = "download") : BaseBottomSheetDialog<BottomSheetDownloadBinding>(context) {
    override fun getViewBinding(): BottomSheetDownloadBinding {
        return BottomSheetDownloadBinding.inflate(layoutInflater)
    }

    private val title = when(type) {
        "download" -> context.getString(R.string.download_title)
        "ringtone" -> context.getString(R.string.ringtone_title)
        "notification" -> context.getString(R.string.set_title)
        else -> context.getString(R.string.download_title)
    }

    private val desc = when(type) {
        "download" -> context.getString(R.string.download_title)
        "ringtone" -> context.getString(R.string.processing)
        "notification" -> context.getString(R.string.processing)
        else -> context.getString(R.string.download_title)
    }

    override fun initViews() {
        setContentView(binding.root)

        binding.apply {
            // Start in processing mode
            processing.visibility = View.VISIBLE
            result.visibility = View.GONE
            done.visibility = View.GONE
            songAvatar.text = title
            processTxt.text = desc
            autoProcess()
        }

        binding.done.setOnClickListener {
            dismiss()
        }
    }

    /** Update the progress bar progress (0-100) */
    fun updateProgress(progress: Int) {
        binding.processingBar.setProgress(progress)
    }

    private fun autoProcess() {
        val animator = ObjectAnimator.ofInt(binding.processingBar, "progress", 0, 100).apply {
            duration = 5000L // 3 seconds
            interpolator = DecelerateInterpolator()
        }
        animator.start()
    }

    /** Call when download finishes successfully */
    fun showSuccess() {
        binding.apply {
            processing.visibility = View.GONE
            result.visibility = View.VISIBLE
            done.visibility = View.VISIBLE
            done.text = context.getString(R.string.done)
            resultIcon.setImageResource(R.drawable.icon_tick)
            // Assuming you have a string "successfully"
            (result.getChildAt(1) as TextView).text = context.getString(R.string.successfully)
        }
    }

    /** Call when download fails */
    fun showFailure() {
        binding.apply {
            processing.visibility = View.GONE
            result.visibility = View.VISIBLE
            done.visibility = View.VISIBLE
            done.text = context.getString(R.string.retry)
            resultIcon.setImageResource(R.drawable.icon_red_fail) // You need to provide a failure icon
            (result.getChildAt(1) as TextView).text = context.getString(R.string.failed)
        }
    }
}
