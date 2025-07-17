package com.example.ringtone.screen.ringtone

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityRingtoneCategoryBinding
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.screen.ringtone.adapter.CategoryDetailAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingtoneCategoryActivity: BaseActivity<ActivityRingtoneCategoryBinding>(ActivityRingtoneCategoryBinding::inflate){
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val categoryDetailAdapter: CategoryDetailAdapter by lazy {
        CategoryDetailAdapter { category ->
            startActivity(Intent(this, FilteredRingtonesActivity::class.java).apply {
                putExtra("categoryId", category.id)
                putExtra("categoryName", category.name)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryViewModel.loadRingtoneCategories()
        binding.apply {
            allCategories.adapter = categoryDetailAdapter

            backBtn.setOnClickListener {
                finish()
            }

            categoryViewModel.ringtoneCategory.observe(this@RingtoneCategoryActivity) {items ->
                categoryDetailAdapter.submitList(items)
            }

        }
    }
}