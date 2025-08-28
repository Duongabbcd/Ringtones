package com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet

import android.content.Context
import android.widget.ImageView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseBottomSheetDialog
import com.ezt.ringify.ringtonewallpaper.databinding.BottomSheetSortingBinding
import com.ezt.ringify.ringtonewallpaper.utils.Common

class SortBottomSheet(private val context: Context,private val onClickListener: (String) -> Unit) :
    BaseBottomSheetDialog<BottomSheetSortingBinding>(context) {
    var currentSortOrder = Common.getSortOrder(context)
    private var currentOrder = ""

    override fun getViewBinding(): BottomSheetSortingBinding {
        return BottomSheetSortingBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)

        binding.apply {

            when (currentSortOrder) {
                "created_at+desc" -> updateDisplayIcons(
                    listOf(firstSortIcon),
                    listOf(secondSortIcon, thirdSortIcon, forthSongIcon)
                )

                "created_at+asc" -> updateDisplayIcons(
                    listOf(secondSortIcon),
                    listOf(firstSortIcon, thirdSortIcon, forthSongIcon)
                )

                "name+asc" -> updateDisplayIcons(
                    listOf(thirdSortIcon),
                    listOf(secondSortIcon, firstSortIcon, forthSongIcon)
                )

                "name+desc" -> updateDisplayIcons(
                    listOf(forthSongIcon),
                    listOf(secondSortIcon, thirdSortIcon, firstSortIcon)
                )

                else -> updateDisplayIcons(
                    listOf(),
                    listOf(forthSongIcon, secondSortIcon, thirdSortIcon, firstSortIcon)
                )
            }

            fromNewToOld.setOnClickListener {
                currentOrder ="created_at+desc"
                updateDisplayIcons(
                    listOf(firstSortIcon),
                    listOf(secondSortIcon, thirdSortIcon, forthSongIcon)
                )
            }

            fromOldToNew.setOnClickListener {
                currentOrder ="created_at+asc"
                updateDisplayIcons(
                    listOf(secondSortIcon),
                    listOf(firstSortIcon, thirdSortIcon, forthSongIcon)
                )
            }

            fromAToZ.setOnClickListener {
                currentOrder ="name+asc"
                updateDisplayIcons(
                    listOf(thirdSortIcon),
                    listOf(firstSortIcon, secondSortIcon, forthSongIcon)
                )
            }

            fromZtoA.setOnClickListener {
                currentOrder ="name+desc"
                updateDisplayIcons(
                    listOf(forthSongIcon),
                    listOf(firstSortIcon, thirdSortIcon, secondSortIcon)
                )
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
                it.setImageResource(R.drawable.icon_unselect_gray_circle)
            }
        }
    }

}