package com.example.ringtone.screen.wallpaper

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityPreviewWallpaperBinding
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.wallpaper.adapter.GridWallpaperAdapter
import com.example.ringtone.R
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.screen.wallpaper.adapter.GridWallpaperAdapter.Companion.VIEW_TYPE_LOADING
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewWallpaperActivity : BaseActivity<ActivityPreviewWallpaperBinding>(ActivityPreviewWallpaperBinding::inflate){
    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val wallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter {
        }.apply {
            onAllImagesLoaded = {
                // Safely post notifyDataSetChanged on RecyclerView's message queue
                binding.allCategories.post {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -1)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("PreviewWallpaperActivity: $categoryId")
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            val layoutManager = GridLayoutManager(this@PreviewWallpaperActivity, 3)

            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (wallpaperAdapter.getItemViewType(position) == VIEW_TYPE_LOADING) {
                        3 // full-width for progress bar
                    } else {
                        1 // normal items take 1 span
                    }
                }
            }

            allCategories.layoutManager = layoutManager
            allCategories.adapter = wallpaperAdapter


            when(categoryId) {
                -2 -> {
                    nameScreen.text = resources.getString(R.string.trending)
                    wallPaperViewModel.loadTrendingWallpapers()
                    wallPaperViewModel.trendingWallpaper.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items)
                    }
                }

                -1 -> {
                    nameScreen.text = resources.getString(R.string.new_wallpaper)
                    wallPaperViewModel.loadNewWallpapers()
                    wallPaperViewModel.newWallpaper.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items)
                    }
                }

                else -> {
                    println("category: $categoryId")
                    categoryViewModel.getCategoryByName(categoryId = categoryId)
                    categoryViewModel.category.observe(this@PreviewWallpaperActivity){ category ->
                        nameScreen.text = category.name
                    }

                    wallPaperViewModel.loadSubWallpapers1(categoryId)
                    wallPaperViewModel.subWallpaper1.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items, premium = categoryId == 75)
                    }
                }
            }

        }
    }
}