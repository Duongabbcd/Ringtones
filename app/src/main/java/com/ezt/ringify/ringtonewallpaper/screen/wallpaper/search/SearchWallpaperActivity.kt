package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySearchWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperTrendingAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.Utils.hideKeyBoard
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

    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val searchWallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter({
            startActivity(Intent(this@SearchWallpaperActivity, SlideWallpaperActivity::class.java))
        }).apply {
            onAllImagesLoaded = {
                // Safely post notifyDataSetChanged on RecyclerView's message queue
                binding.allResults.post {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@SearchWallpaperActivity, "INTER_WALLPAPER")
            }

            connectionViewModel.isConnectedLiveData.observe(this@SearchWallpaperActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }


            trendingRecyclerView.adapter = wallpaperAdapter
            trendingRecyclerView.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 2)

            allResults.adapter = searchWallpaperAdapter
            allResults.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 3)

            categoryViewModel.wallpaperCategory.observe(this@SearchWallpaperActivity) { items ->
                wallpaperAdapter.submitList(items)
            }

            wallpaperViewModel.tags.observe(this@SearchWallpaperActivity) { tag ->
                if (tag == null) {
                    binding.noDataLayout.visible()
                    binding.allResults.gone()
                } else {
                    binding.noDataLayout.gone()
                    binding.allResults.visible()
                    wallpaperViewModel.searchWallpaperByTag(tag.id)
                }
            }

            wallpaperViewModel.searchWallpapers.observe(this@SearchWallpaperActivity) { items ->
                if (items.isNullOrEmpty()) {
                    binding.noDataLayout.visible()
                    binding.allResults.gone()
                } else {
                    binding.noDataLayout.gone()
                    binding.allResults.visible()
                    searchWallpaperAdapter.submitList(items)
                }
            }

            binding.searchText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    wallpaperViewModel.searchTag(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                    displayByCondition(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            })

            searchText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    this@SearchWallpaperActivity.hideKeyBoard(binding.searchText)
                    // Do something with the search query
                    // For example: performSearch(query)
                    true // consume the action
                } else {
                    false
                }
            }

            allResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        this@SearchWallpaperActivity.hideKeyBoard(binding.searchText)
                    }
                }
            })


            closeButton.setOnClickListener {
                searchText.setText("")
                displayByCondition("")
            }

        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            categoryViewModel.loadWallpaperCategories()
            binding.noInternet.root.gone()
        }
    }

    private fun displayByCondition(input: String) {
        binding.apply {
            noDataLayout.gone()
            if(input.isEmpty()) {
                trendingTitle.visible()
                trendingIcon.visible()
                trendingRecyclerView.visible()

                allResults.gone()
                closeButton.gone()
            } else {
                trendingTitle.gone()
                trendingIcon.gone()
                trendingRecyclerView.gone()

                allResults.visible()
                closeButton.visible()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
        loadBanner(this, BANNER_HOME)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@SearchWallpaperActivity, "INTER_WALLPAPER")

    }
}