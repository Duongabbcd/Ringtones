package com.example.ringtone.screen.wallpaper.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.ringtone.databinding.DialogSetWallpaperBinding
import com.example.ringtone.screen.ringtone.bottomsheet.SortBottomSheet.Companion.updateDisplayIcons

class SetWallpaperDialog(
    context: Context,
    private val onClickOptionListener: (Int) -> Unit
) : Dialog(context) {
    private val binding by lazy { DialogSetWallpaperBinding.inflate(layoutInflater) }
    private lateinit var handler: Handler
    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        binding.apply {
            okBtn.setOnClickListener {
                dismiss()
            }

            cancelBtn.setOnClickListener {
                dismiss()
            }

            wallpaperOption.setOnClickListener {
                updateDisplayIcons(firstSortIcon, listOf(secondSortIcon, thirdSortIcon))
                handler.postDelayed({
                    onClickOptionListener(1)
                },  300)
                dismiss()
            }

            homeScreenOption.setOnClickListener {
                updateDisplayIcons(secondSortIcon, listOf(firstSortIcon, thirdSortIcon))
                handler.postDelayed({
                    onClickOptionListener(2)
                },  300)
                dismiss()
            }

            bothOfThemOption.setOnClickListener {
                updateDisplayIcons(thirdSortIcon, listOf(firstSortIcon, secondSortIcon))
                handler.postDelayed({
                    onClickOptionListener(3)
                },  300)
                dismiss()
            }
        }
    }
}