package com.ezt.ringify.ringtonewallpaper.screen.wallpaper

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewWallpaperActivity :
    BaseActivity<ActivityPreviewWallpaperBinding>(ActivityPreviewWallpaperBinding::inflate) {
    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val favourite: FavouriteWallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val wallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter { wallpaper ->
            startActivity(
                Intent(this, SlideWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", categoryId)
                    if (categoryId == 75) putExtra("type", type)
                }
            )
        }.apply {
            onAllImagesLoaded = {
                binding.allCategories.post { notifyDataSetChanged() }
            }
        }
    }

    private val categoryId by lazy { intent.getIntExtra("wallpaperCategoryId", -1) }
    private val type by lazy { intent.getIntExtra("type", 1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener { finish() }
            allCategories.layoutManager = GridLayoutManager(this@PreviewWallpaperActivity, 3)
            allCategories.adapter = wallpaperAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            displayItems()
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }

    private fun loadMoreData() {
        binding.allCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(rv, dx, dy)
                val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

                if (firstVisiblePosition + visibleItemCount >= totalItemCount - 5) {
                    when (categoryId) {
                        -3 -> favourite.loadLiveAllWallpapers()
                        -2 -> wallPaperViewModel.loadTrendingWallpapers()
                        -1 -> wallPaperViewModel.loadNewWallpapers()
                        else -> {
                            if (categoryId == 75) {
                                when (type) {
                                    2 -> wallPaperViewModel.loadSlideWallpaper()
                                    3 -> wallPaperViewModel.loadSingleWallpaper()
                                    else -> wallPaperViewModel.loadPremiumVideoWallpaper()
                                }
                            }
                            categoryViewModel.getCategoryByName(categoryId)
                            wallPaperViewModel.loadSubWallpapers1(categoryId)
                        }
                    }
                }
            }
        })
    }

    private fun displayItems() {
        // Remove all observers first
        removeAllObservers()

        binding.apply {
            allCategories.visible()
            when (categoryId) {
                -3 -> {
                    nameScreen.text = getString(R.string.favourite)
                    // Attach observer only once
                    favourite.loadLiveAllWallpapers()
                    favourite.allLiveWallpapers.observe(this@PreviewWallpaperActivity) { items ->
                        if (items.isEmpty()) {
                            allCategories.gone()
                            noDataLayout.visible()
                        } else {
                            noDataLayout.gone()
                            allCategories.visible()
                            wallpaperAdapter.submitFavouriteList(items)
                        }
                    }
                }

                -2 -> {
                    nameScreen.text = getString(R.string.trending)
                    wallPaperViewModel.loadTrendingWallpapers()
                    wallPaperViewModel.trendingWallpaper.observe(this@PreviewWallpaperActivity) { items ->
                        wallpaperAdapter.submitList(items)
                    }
                }

                -1 -> {
                    nameScreen.text = getString(R.string.new_wallpaper)
                    wallPaperViewModel.loadNewWallpapers()
                    wallPaperViewModel.newWallpaper.observe(this@PreviewWallpaperActivity) { items ->
                        wallpaperAdapter.submitList(items)
                    }
                }

                else -> {
                    if (categoryId == 75) {
                        when (type) {
                            2 -> {
                                wallPaperViewModel.loadSlideWallpaper()
                                wallPaperViewModel.slideWallpaper.observe(this@PreviewWallpaperActivity) {
                                    wallpaperAdapter.submitList(it, premium = true)
                                }
                            }

                            3 -> {
                                wallPaperViewModel.loadSingleWallpaper()
                                wallPaperViewModel.singleWallpapers.observe(this@PreviewWallpaperActivity) {
                                    wallpaperAdapter.submitList(it, premium = true)
                                }
                            }

                            else -> {
                                wallPaperViewModel.loadPremiumVideoWallpaper()
                                wallPaperViewModel.premiumWallpapers.observe(this@PreviewWallpaperActivity) {
                                    wallpaperAdapter.submitList(it, premium = true)
                                }
                                return@apply
                            }
                        }
                    }

                    categoryViewModel.getCategoryByName(categoryId)
                    categoryViewModel.category.observe(this@PreviewWallpaperActivity) { category ->
                        nameScreen.text = category.name
                    }

                    wallPaperViewModel.loadSubWallpapers1(categoryId)
                    wallPaperViewModel.subWallpaper1.observe(this@PreviewWallpaperActivity) { items ->
                        wallpaperAdapter.submitList(items, premium = categoryId == 75)
                    }
                }
            }
        }
    }

    private fun removeAllObservers() {
        favourite.allLiveWallpapers.removeObservers(this)
        favourite.liveWallpaper.removeObservers(this)
        wallPaperViewModel.trendingWallpaper.removeObservers(this)
        wallPaperViewModel.newWallpaper.removeObservers(this)
        wallPaperViewModel.slideWallpaper.removeObservers(this)
        wallPaperViewModel.singleWallpapers.removeObservers(this)
        wallPaperViewModel.premiumWallpapers.removeObservers(this)
        wallPaperViewModel.subWallpaper1.removeObservers(this)
        categoryViewModel.category.removeObservers(this)
    }
}
