package com.example.ringtone.screen.ringtone

import android.os.Bundle
import androidx.activity.viewModels
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityFilteredCategoryBinding
import com.example.ringtone.remote.viewmodel.RingtoneViewModel
import com.example.ringtone.screen.home.subscreen.first_screen.adapter.RingtoneAdapter
import com.example.ringtone.R
import com.example.ringtone.screen.ringtone.bottomsheet.SortBottomSheet
import com.example.ringtone.utils.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilteredRingtonesActivity : BaseActivity<ActivityFilteredCategoryBinding>(
    ActivityFilteredCategoryBinding::inflate
) {
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val ringtoneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter()
    }

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -100)
    }

    private val categoryName by lazy {
        intent.getStringExtra("categoryName")
    }

    private lateinit var sortOrder: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortOrder = Common.getSortOrder(this)
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            println("category: $categoryId")
            allCategories.adapter = ringtoneAdapter
            displayItems()

            sortIcon.setOnClickListener {
                val dialog = SortBottomSheet(this@FilteredRingtonesActivity) { string ->
                    Common.setSortOrder(this@FilteredRingtonesActivity, string)
                    sortOrder = Common.getSortOrder(this@FilteredRingtonesActivity)
                    displayItems()
                }
                dialog.show()
            }
        }
    }

    private fun displayItems() {
        binding.apply {
            if(categoryId == -100) {
                ringtoneViewModel.loadPopular(sortOrder)
                nameScreen.text = getString(R.string.popular)
                ringtoneViewModel.popular.observe(this@FilteredRingtonesActivity) { items ->
                    ringtoneAdapter.submitList(items)
                }
            } else {
                nameScreen.text = categoryName ?: getString(R.string.unknown_cat)
                ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                ringtoneViewModel.selectedRingtone.observe(this@FilteredRingtonesActivity) { items ->
                    ringtoneAdapter.submitList(items)
                }
            }
        }
    }
}