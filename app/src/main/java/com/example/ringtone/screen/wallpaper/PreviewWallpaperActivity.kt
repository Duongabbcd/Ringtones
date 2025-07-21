package com.example.ringtone.screen.wallpaper

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityPreviewWallpaperBinding
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.wallpaper.adapter.GridWallpaperAdapter
import com.example.ringtone.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewWallpaperActivity : BaseActivity<ActivityPreviewWallpaperBinding>(ActivityPreviewWallpaperBinding::inflate){
    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val wallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter {

        }
    }

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -1)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            allCategories.adapter = wallpaperAdapter
            allCategories.layoutManager = GridLayoutManager(this@PreviewWallpaperActivity, 3)

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
                    wallPaperViewModel.loadSubWallpapers1(categoryId)
                    wallPaperViewModel.subWallpaper1.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items)
                    }
                }
            }

        }
    }
}