package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet

import android.content.Context
import android.widget.ImageView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseBottomSheetDialog
import com.ezt.ringify.ringtonewallpaper.databinding.BottomSheetSortingWallpaperBinding

class SortWallpaperBottomSheet(
    private val context: Context,
    private val onClickListener: (String) -> Unit
) :
    BaseBottomSheetDialog<BottomSheetSortingWallpaperBinding>(context) {
    var sortOrder = ""
    private var currentOrder = ""

    override fun getViewBinding(): BottomSheetSortingWallpaperBinding {
        return BottomSheetSortingWallpaperBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)

        binding.apply {

            when (sortOrder) {
                "Default" -> updateDisplayIcons(
                    listOf(firstSortIcon),
                    listOf(secondSortIcon, thirdSortIcon)
                )

                "Trending" -> updateDisplayIcons(
                    listOf(secondSortIcon),
                    listOf(firstSortIcon, thirdSortIcon)
                )

                "New" -> updateDisplayIcons(
                    listOf(thirdSortIcon),
                    listOf(secondSortIcon, firstSortIcon)
                )

                else -> updateDisplayIcons(
                    listOf(),
                    listOf(firstSortIcon, secondSortIcon, thirdSortIcon)
                )
            }

            firstOption.setOnClickListener {
                currentOrder = "Default"
                updateDisplayIcons(listOf(firstSortIcon), listOf(secondSortIcon, thirdSortIcon))
            }

            secondOption.setOnClickListener {
                currentOrder = "Trending"
                updateDisplayIcons(listOf(secondSortIcon), listOf(firstSortIcon, thirdSortIcon))
            }

            thirdOption.setOnClickListener {
                currentOrder = "New"
                updateDisplayIcons(listOf(thirdSortIcon), listOf(firstSortIcon, secondSortIcon))
            }


            cancelBtn.setOnClickListener {
                dismiss()
            }

            okBtn.setOnClickListener {
                onClickListener(currentOrder)
                dismiss()
            }
        }

    }

    companion object {
        fun updateDisplayIcons(selected: List<ImageView>, unselected: List<ImageView>) {
            selected.onEach {
                it.setImageResource(R.drawable.icon_select_circle)
            }
            unselected.onEach {
                it.setImageResource(R.drawable.icon_unselect_circle)
            }
        }
    }

}