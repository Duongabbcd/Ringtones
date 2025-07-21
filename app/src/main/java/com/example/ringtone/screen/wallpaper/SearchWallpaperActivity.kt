package com.example.ringtone.screen.wallpaper

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivitySearchWallpaperBinding
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.wallpaper.adapter.WallpaperTrendingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SearchWallpaperActivity : BaseActivity<ActivitySearchWallpaperBinding>(
    ActivitySearchWallpaperBinding::inflate
) {
    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val wallpaperAdapter: WallpaperTrendingAdapter by lazy {
        WallpaperTrendingAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryViewModel.loadWallpaperCategories()
        binding.apply {
            trendingRecyclerView.adapter = wallpaperAdapter
            trendingRecyclerView.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 2)

            categoryViewModel.wallpaperCategory.observe(this@SearchWallpaperActivity) { items ->
                wallpaperAdapter.submitList(items)
            }
        }
    }
}