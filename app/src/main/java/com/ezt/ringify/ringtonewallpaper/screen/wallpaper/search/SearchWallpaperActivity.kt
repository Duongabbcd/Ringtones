package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySearchWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.TagViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.TagTrendingAdapter
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
    private val tagViewModel: TagViewModel by viewModels()
    private val wallpaperAdapter: TagTrendingAdapter by lazy {
        TagTrendingAdapter { tag ->
            Log.d(TAG, "TagTrendingAdapter: $tag")
            binding.searchText.setText(tag.name)
            binding.searchText.setSelection(tag.name.length)
            binding.trendingIcon.gone()
            binding.trendingTitle.gone()
            binding.trendingRecyclerView.gone()
            wallpaperViewModel.searchWallpaperByTag(tag.id)
        }
    }

    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val searchWallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter {
            startActivity(Intent(this@SearchWallpaperActivity, SlideWallpaperActivity::class.java))
        }.apply {
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
        loadBanner(this, BANNER_HOME)
        
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@SearchWallpaperActivity, "INTER_WALLPAPER")
            }

            connectionViewModel.isConnectedLiveData.observe(this@SearchWallpaperActivity) { isConnected ->
                Log.d(TAG, "isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }


            trendingRecyclerView.adapter = wallpaperAdapter
            trendingRecyclerView.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 2)

            allResults.adapter = searchWallpaperAdapter
            allResults.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 3)

            tagViewModel.tag.observe(this@SearchWallpaperActivity) { items ->
                if (items.isEmpty()) {
                    trendingTitle.gone()
                    trendingIcon.gone()
                    trendingRecyclerView.gone()
                    noDataLayout.visible()
                } else {
                    trendingTitle.visible()
                    trendingIcon.visible()
                    trendingRecyclerView.visible()
                    noDataLayout.gone()

                    wallpaperAdapter.submitList(items)
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
                    if (s.isNullOrEmpty()) {
                        tagViewModel.loadAllTags()
                    } else {
                        tagViewModel.searchTag(s.toString())
                        allResults.gone()
                    }
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
                tagViewModel.loadAllTags()
                displayByCondition("")
                noDataLayout.gone()
            }

        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            tagViewModel.loadAllTags()
            binding.noInternet.root.gone()
        }
    }

    private fun displayByCondition(input: String) {
        binding.apply {
            noDataLayout.gone()
            trendingTitle.visible()
            trendingIcon.visible()
            trendingRecyclerView.visible()
            if (input.isEmpty()) {
                allResults.gone()
                closeButton.gone()
            } else {
                closeButton.visible()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@SearchWallpaperActivity, "INTER_WALLPAPER")

    }

    companion object {
        val TAG = SearchWallpaperActivity::class.java.name
    }
}