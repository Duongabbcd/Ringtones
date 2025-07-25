package com.example.ringtone.screen.wallpaper.bottomsheet

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.example.ringtone.base.BaseBottomSheetDialog
import com.example.ringtone.R
import com.example.ringtone.databinding.BottomSheetRingtoneDownloadBinding
import com.example.ringtone.databinding.BottomSheetWallpaperDownloadBinding

class DownloadWallpaperBottomSheet(private val context: Context) : BaseBottomSheetDialog<BottomSheetWallpaperDownloadBinding>(context) {
    override fun getViewBinding(): BottomSheetWallpaperDownloadBinding {
        return BottomSheetWallpaperDownloadBinding.inflate(layoutInflater)
    }


    fun setType(type: String = "wallpaper") {
        title = when(type) {
            "lock" -> context.getString(R.string.set_wallpaper_op1)
            "home" -> context.getString(R.string.set_wallpaper_op2)
            "both" -> context.getString(R.string.set_wallpaper_op3)
            else -> context.getString(R.string.set_wallpaper_op1)
        }


        desc = when(type) {
            "lock" -> context.getString(R.string.set_wallpaper_1)
            "home" -> context.getString(R.string.set_wallpaper_2)
            "both" -> context.getString(R.string.set_wallpaper_3)
            else -> context.getString(R.string.set_wallpaper_1)
        }
    }
    private var title = ""

    private var desc = ""

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
