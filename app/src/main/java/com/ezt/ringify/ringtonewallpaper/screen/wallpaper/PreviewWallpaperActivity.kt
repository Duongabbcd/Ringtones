package com.ezt.ringify.ringtonewallpaper.screen.wallpaper

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
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
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@PreviewWallpaperActivity,
                    "INTER_WALLPAPER"
                )
            }
            allCategories.layoutManager = GridLayoutManager(this@PreviewWallpaperActivity, 3)
            allCategories.adapter = wallpaperAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }

        loadBanner(this, BANNER_HOME)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@PreviewWallpaperActivity, "INTER_WALLPAPER")
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
                    previewFavouriteItems()
                }

                -2 -> {
                    nameScreen.text = getString(R.string.trending)
                    wallPaperViewModel.loadTrendingWallpapers()
                    previewSelectItems(
                        wallPaperViewModel.trendingWallpaper,
                        wallPaperViewModel.loading1
                    )
                }

                -1 -> {
                    nameScreen.text = getString(R.string.new_wallpaper)
                    wallPaperViewModel.loadNewWallpapers()
                    previewSelectItems(wallPaperViewModel.newWallpaper, wallPaperViewModel.loading2)
                }

                else -> {
                    if (categoryId == 75) {
                        when (type) {
                            2 -> {
                                nameScreen.text = getString(R.string.slide)
                                wallPaperViewModel.loadSlideWallpaper()
                                previewSelectItems(
                                    wallPaperViewModel.slideWallpaper,
                                    wallPaperViewModel.loading2, true
                                )
                            }

                            3 -> {
                                nameScreen.text = getString(R.string.single)
                                wallPaperViewModel.loadSingleWallpaper()
                                previewSelectItems(
                                    wallPaperViewModel.singleWallpapers,
                                    wallPaperViewModel.loading3, true
                                )
                            }

                            1 -> {
                                nameScreen.text = getString(R.string.video)
                                wallPaperViewModel.loadPremiumVideoWallpaper()
                                previewSelectItems(
                                    wallPaperViewModel.premiumWallpapers,
                                    wallPaperViewModel.loading1, true
                                )
                            }
                        }
                    } else {
                        categoryViewModel.getCategoryByName(categoryId)
                        categoryViewModel.category.observe(this@PreviewWallpaperActivity) { category ->
                            nameScreen.text = category.name
                        }
                        wallPaperViewModel.loadSubWallpapers1(categoryId)
                        previewSelectItems(
                            wallPaperViewModel.subWallpaper1,
                            wallPaperViewModel.loading3
                        )

                    }
                }
            }
        }
    }

    private fun previewSelectItems(
        data: LiveData<List<Wallpaper>>,
        loading: LiveData<Boolean>,
        isPremium: Boolean = false
    ) {
        binding.apply {
            loading.observe(this@PreviewWallpaperActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(15) {
                        Wallpaper.EMPTY_WALLPAPER
                    }
                    // Disable scrolling
                    wallpaperAdapter.submitList(loadingItems)
                    this@PreviewWallpaperActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    data.observe(this@PreviewWallpaperActivity) { items ->
                        if (items.isEmpty()) {
                            allCategories.gone()
                            noDataLayout.visible()
                        } else {
                            noDataLayout.gone()
                            allCategories.visible()
                            wallpaperAdapter.submitList(items, isPremium)
                        }
                    }
                    // Re-enable touch
                    this@PreviewWallpaperActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
    }


    private fun previewTrendingItems() {
        binding.apply {
            nameScreen.text = getString(R.string.trending)
            // Attach observer only once
            wallPaperViewModel.loadTrendingWallpapers()
            wallPaperViewModel.loading1.observe(this@PreviewWallpaperActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(15) {
                        Wallpaper.EMPTY_WALLPAPER
                    }
                    // Disable scrolling
                    wallpaperAdapter.submitFavouriteList(loadingItems)
                    this@PreviewWallpaperActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    wallPaperViewModel.trendingWallpaper.observe(this@PreviewWallpaperActivity) { items ->
                        if (items.isEmpty()) {
                            allCategories.gone()
                            noDataLayout.visible()
                        } else {
                            noDataLayout.gone()
                            allCategories.visible()
                            wallpaperAdapter.submitList(items)
                        }
                    }
                    // Re-enable touch
                    this@PreviewWallpaperActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
    }

    private fun previewFavouriteItems() {
        binding.apply {
            nameScreen.text = getString(R.string.favourite)
            // Attach observer only once
            favourite.loadLiveAllWallpapers()
            favourite.loading1.observe(this@PreviewWallpaperActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(15) {
                        Wallpaper.EMPTY_WALLPAPER
                    }
                    // Disable scrolling
                    wallpaperAdapter.submitFavouriteList(loadingItems)
                    this@PreviewWallpaperActivity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
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
                    // Re-enable touch
                    this@PreviewWallpaperActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
