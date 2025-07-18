package com.example.ringtone.screen.player.bottomsheet

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.ringtone.base.BaseBottomSheetDialog
import com.example.ringtone.databinding.BottomSheetDownloadBinding
import com.example.ringtone.R
class DownloadBottomSheet(private val context: Context) : BaseBottomSheetDialog<BottomSheetDownloadBinding>(context) {
    override fun getViewBinding(): BottomSheetDownloadBinding {
        return BottomSheetDownloadBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)

        binding.apply {
            // Start in processing mode
            processing.visibility = View.VISIBLE
            result.visibility = View.GONE
            done.visibility = View.GONE
            songAvatar.text = context.getString(R.string.download_title)
        }

        binding.done.setOnClickListener {
            dismiss()
        }
    }

    /** Update the progress bar progress (0-100) */
    fun updateProgress(progress: Int) {
        binding.processingBar.setProgress(progress)
    }

    /** Call when download finishes successfully */
    fun showSuccess() {
        binding.apply {
            processing.visibility = View.GONE
            result.visibility = View.VISIBLE
            done.visibility = View.VISIBLE
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
            resultIcon.setImageResource(R.drawable.icon_tick) // You need to provide a failure icon
            (result.getChildAt(1) as TextView).text = context.getString(R.string.failed)
        }
    }
}
