package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.ezt.ringify.ringtonewallpaper.databinding.DialogSetWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet.Companion.updateDisplayIcons

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

    private var optionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        binding.apply {
            okBtn.setOnClickListener {
                if(optionIndex != 0) {
                    onClickOptionListener(optionIndex)
                }

                dismiss()
            }

            cancelBtn.setOnClickListener {
                dismiss()
            }

            wallpaperOption.setOnClickListener {
                updateDisplayIcons(firstSortIcon, listOf(secondSortIcon, thirdSortIcon))
                optionIndex = 1
            }

            homeScreenOption.setOnClickListener {
                updateDisplayIcons(secondSortIcon, listOf(firstSortIcon, thirdSortIcon))
                optionIndex = 2
            }

            bothOfThemOption.setOnClickListener {
                updateDisplayIcons(thirdSortIcon, listOf(firstSortIcon, secondSortIcon))
                optionIndex = 3
            }
        }
    }
}