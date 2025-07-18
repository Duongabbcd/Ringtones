package com.example.ringtone.screen.ringtone.bottomsheet

import android.content.Context
import android.widget.ImageView
import com.example.ringtone.databinding.BottomSheetSortingBinding
import com.example.ringtone.utils.Common
import com.example.ringtone.R
import com.example.ringtone.base.BaseBottomSheetDialog

class SortBottomSheet(private val context: Context,private val onClickListener: (String) -> Unit) :
    BaseBottomSheetDialog<BottomSheetSortingBinding>(context) {
    private var sortOrder = Common.getSortOrder(context)
    private var currentOrder = ""

    override fun getViewBinding(): BottomSheetSortingBinding {
        return BottomSheetSortingBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)

        binding.apply {

            when(sortOrder) {
                "created_at+desc" -> updateDisplayIcons(firstSortIcon, listOf(secondSortIcon, thirdSortIcon, forthSongIcon))
                "created_at+asc" -> updateDisplayIcons(secondSortIcon, listOf(firstSortIcon, thirdSortIcon, forthSongIcon))
                "name+asc" -> updateDisplayIcons(thirdSortIcon, listOf(secondSortIcon, firstSortIcon, forthSongIcon))
                "name+desc" ->updateDisplayIcons(forthSongIcon, listOf(secondSortIcon, thirdSortIcon, firstSortIcon))
            }

            fromNewToOld.setOnClickListener {
                currentOrder ="created_at+desc"
                updateDisplayIcons(firstSortIcon, listOf(secondSortIcon, thirdSortIcon, forthSongIcon))
            }

            fromOldToNew.setOnClickListener {
                currentOrder ="created_at+asc"
                updateDisplayIcons(secondSortIcon, listOf(firstSortIcon, thirdSortIcon, forthSongIcon))
            }

            fromAToZ.setOnClickListener {
                currentOrder ="name+asc"
                updateDisplayIcons(thirdSortIcon, listOf(firstSortIcon, secondSortIcon, forthSongIcon))
            }

            fromZtoA.setOnClickListener {
                currentOrder ="name+desc"
                updateDisplayIcons(forthSongIcon, listOf(firstSortIcon, thirdSortIcon, secondSortIcon))
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
        fun updateDisplayIcons(selected: ImageView, unselected: List<ImageView>) {
            selected.setImageResource(R.drawable.icon_select_circle)
            unselected.onEach {
                it.setImageResource(R.drawable.icon_unselect_circle)
            }
        }
    }

}