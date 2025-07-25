package com.ezt.ringify.ringtonewallpaper.screen.ringtone

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityRingtoneCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.model.Category.Companion.EMPTY_CATEGORY
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.adapter.CategoryDetailAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingtoneCategoryActivity: BaseActivity<ActivityRingtoneCategoryBinding>(ActivityRingtoneCategoryBinding::inflate){
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val viewModel: FavouriteRingtoneViewModel by viewModels()

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
            viewModel.loadAllRingtones()

            categoryViewModel.ringtoneCategory.observe(this@RingtoneCategoryActivity) {items ->
                val allCategories = mutableListOf<Category>()

                viewModel.allRingtones.observe(this@RingtoneCategoryActivity ) { result ->
                    println("categoryViewModel: $result")
                    allCategories.add(EMPTY_CATEGORY.copy(contentCount = result.size))

                }

                allCategories.addAll(items)
                categoryDetailAdapter.submitList(allCategories)
            }

        }
    }
}