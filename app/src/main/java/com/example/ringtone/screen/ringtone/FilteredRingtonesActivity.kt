package com.example.ringtone.screen.ringtone

import android.os.Bundle
import androidx.activity.viewModels
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityFilteredCategoryBinding
import com.example.ringtone.remote.viewmodel.RingtoneViewModel
import com.example.ringtone.screen.home.subscreen.first_screen.adapter.RingtoneAdapter
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
        intent.getIntExtra("categoryId", 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            allCategories.adapter = ringtoneAdapter
            ringtoneViewModel.loadSelectedCategories(categoryId)
            ringtoneViewModel.selectedRingtone.observe(this@FilteredRingtonesActivity) { items ->
                ringtoneAdapter.submitList(items)
            }
        }
    }
}