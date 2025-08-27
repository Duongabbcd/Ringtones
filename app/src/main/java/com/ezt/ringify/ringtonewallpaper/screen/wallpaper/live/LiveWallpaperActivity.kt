package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityLiveWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter.Companion.VIEW_TYPE_LOADING
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.SortWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LiveWallpaperActivity : BaseActivity<ActivityLiveWallpaperBinding>(
    ActivityLiveWallpaperBinding::inflate
) {
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private val wallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter {
            Log.d(TAG, "Wallpaper: $it")
            startActivity(
                Intent(
                    this@LiveWallpaperActivity,
                    PreviewLiveWallpaperActivity::class.java
                ).apply {
                    putExtra("type", 2)
                }
            )
        }
    }
    private lateinit var sortOrder: String
    private lateinit var bottomSheet: SortWallpaperBottomSheet


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this)
        sortOrder = ""

        bottomSheet = SortWallpaperBottomSheet(this@LiveWallpaperActivity) { result ->
            sortOrder = result
            Common.setSortWppOrder(this@LiveWallpaperActivity, result)
            displayItems()
        }
        bottomSheet.sortOrder = ""

        connectionViewModel.isConnectedLiveData.observe(this@LiveWallpaperActivity) { isConnected ->
            Log.d(TAG, "isConnected: $isConnected and $sortOrder")
            checkInternetConnected(isConnected)
        }

        wallpaperViewModel.liveWallpapers.observe(this@LiveWallpaperActivity) { items ->
            binding.allCategories.visible()
            binding.noDataLayout.gone()
            if (items.isEmpty()) {
                binding.allCategories.gone()
                binding.noDataLayout.visible()
            } else {
                wallpaperAdapter.submitList(items, live = true, premium = false)
            }
        }

        wallpaperViewModel.loading1.observe(this@LiveWallpaperActivity) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@LiveWallpaperActivity, "INTER_WALLPAPER")
            }

            sort.setOnClickListener {
                bottomSheet.show()
            }

            val layoutManager = GridLayoutManager(this@LiveWallpaperActivity, 3)

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

            nameScreen.text = resources.getString(R.string.live)
        }
    }

    private fun displayItems() {
        Log.d(TAG, "displayItems 123: $sortOrder")
        when (sortOrder) {
            "Default" -> wallpaperViewModel.loadLiveWallpapers(0)
            "Trending" -> wallpaperViewModel.loadLiveWallpapers(1)
            "New" -> wallpaperViewModel.loadLiveWallpapers(2)
            else -> wallpaperViewModel.loadLiveWallpapers(0)
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
        binding.apply {
            allCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    val isAtBottom =
                        firstVisibleItemPosition + visibleItemCount >= totalItemCount - 5
                    if (isAtBottom) {
                        when (sortOrder) {
                            "Default" -> wallpaperViewModel.loadLiveWallpapers(0)
                            "Trending" -> wallpaperViewModel.loadLiveWallpapers(1)
                            "New" -> wallpaperViewModel.loadLiveWallpapers(2)
                            else -> wallpaperViewModel.loadLiveWallpapers(0)
                        }
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@LiveWallpaperActivity, "INTER_WALLPAPER")
    }

    companion object {
        val TAG = LiveWallpaperActivity::class.java.simpleName
    }
}