package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.TagTrendingAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.Utils.hideKeyBoard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchWallpaperActivity : BaseActivity<ActivitySearchWallpaperBinding>(
    ActivitySearchWallpaperBinding::inflate
) {

    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private val tagViewModel: TagViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private var tagId = 0
    private var isFromTagClick = false

    private enum class ScreenState { TRENDING, SEARCH_RESULT }

    private var currentState = ScreenState.TRENDING
    private var isProgrammaticallySettingText = false

    // Trending Adapter
    private val wallpaperAdapter: TagTrendingAdapter by lazy {
        TagTrendingAdapter { tag ->
            Log.d(TAG, "TagTrendingAdapter clicked: $tag")
            isFromTagClick = true
            tagId = tag.id

            wallpaperViewModel.resetSearchPaging()

            // Temporarily mark that we're setting text programmatically
            isProgrammaticallySettingText = true
            binding.searchText.setText(tag.name)
            binding.searchText.setSelection(tag.name.length)
            isProgrammaticallySettingText = false

            // Show search result
            showSearchResult()

            // Load wallpapers
            wallpaperViewModel.searchWallpaperByTag(tag.id)
            wallpaperViewModel.searchSingleWallpaperByTag(tag.id)
            wallpaperViewModel.searchSlideWallpaperByTag(tag.id)
            wallpaperViewModel.searchVideoWallpaperByTag(tag.id)
        }
    }


    // Search result adapters
    private val searchWallpaperAdapter1: WallpaperAdapter by lazy {
        WallpaperAdapter {
            startActivity(Intent(this@SearchWallpaperActivity, SlideWallpaperActivity::class.java))
        }
    }

    private val searchWallpaperAdapter2: WallpaperAdapter by lazy {
        WallpaperAdapter {
            startActivity(Intent(this@SearchWallpaperActivity, SlideWallpaperActivity::class.java))
        }
    }

    private val searchWallpaperAdapter3: WallpaperAdapter by lazy {
        WallpaperAdapter {
            startActivity(
                Intent(
                    this@SearchWallpaperActivity,
                    PreviewLiveWallpaperActivity::class.java
                ).apply {
                    putExtra("type", -10)
                    putExtra("tagId", tagId)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBanner(this, BANNER_HOME)

        binding.apply {
            // Back button
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@SearchWallpaperActivity, "INTER_WALLPAPER")
            }

            // Internet connection
            connectionViewModel.isConnectedLiveData.observe(this@SearchWallpaperActivity) { isConnected ->
                Log.d(TAG, "isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            // Trending RecyclerView
            trendingRecyclerView.adapter = wallpaperAdapter
            trendingRecyclerView.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 2)

            // Search result RecyclerViews
            allResults1.adapter = searchWallpaperAdapter1
            allResults2.adapter = searchWallpaperAdapter2
            allResults3.adapter = searchWallpaperAdapter3

            allResults1.layoutManager = LinearLayoutManager(
                this@SearchWallpaperActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            allResults2.layoutManager = LinearLayoutManager(
                this@SearchWallpaperActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            allResults3.layoutManager = LinearLayoutManager(
                this@SearchWallpaperActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            allResults1.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val lastPos = lm.findLastVisibleItemPosition()
                    val total = lm.itemCount
                    if (lastPos >= total - 5) { // threshold = 5 items
                        wallpaperViewModel.searchSingleWallpaperByTag(tagId)
                    }
                }
            })

            allResults2.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val lastPos = lm.findLastVisibleItemPosition()
                    val total = lm.itemCount
                    if (lastPos >= total - 5) {
                        wallpaperViewModel.searchSlideWallpaperByTag(tagId)
                    }
                }
            })

            allResults3.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val lastPos = lm.findLastVisibleItemPosition()
                    val total = lm.itemCount
                    if (lastPos >= total - 5) {
                        wallpaperViewModel.searchVideoWallpaperByTag(tagId)
                    }
                }
            })


            // Observe Tags
            tagViewModel.tag.observe(this@SearchWallpaperActivity) { items ->
                val query = searchText.text.toString()
                if (items.isEmpty() && query.isEmpty()) {
                    noDataLayout.visible()
                    trendingTitle.gone()
                    trendingIcon.gone()
                    trendingRecyclerView.gone()
                } else {
                    if (currentState == ScreenState.TRENDING) {
                        trendingTitle.visible()
                        trendingIcon.visible()
                        trendingRecyclerView.visible()
                    }
                    wallpaperAdapter.submitList(items)
                    noDataLayout.gone()
                }
            }

            wallpaperViewModel.searchWallpapers.observe(this@SearchWallpaperActivity) { items ->
                if (items.isNullOrEmpty()) {
                    noDataLayout.visible()
                    searchResult.gone()
                } else {
                    noDataLayout.gone()
                    searchResult.visible()
                }
            }

            // Observe wallpapers
            wallpaperViewModel.searchWallpapers1.observe(this@SearchWallpaperActivity) { items ->
                updateUIBySearchQuery(searchText.text.toString())
                if (items.isNullOrEmpty()) {
                    allResults1.gone()
                    singleWallpaper.gone()
                } else {
                    allResults1.visible()
                    singleWallpaper.visible()
                    searchWallpaperAdapter1.submitList(items)
                }
            }

            wallpaperViewModel.searchWallpapers2.observe(this@SearchWallpaperActivity) { items ->
                updateUIBySearchQuery(searchText.text.toString())
                if (items.isNullOrEmpty()) {
                    allResults2.gone()
                    slideWallpaper.gone()
                } else {
                    allResults2.visible()
                    slideWallpaper.visible()
                    searchWallpaperAdapter2.submitList(items)
                }
            }

            wallpaperViewModel.searchWallpapers3.observe(this@SearchWallpaperActivity) { items ->
                updateUIBySearchQuery(searchText.text.toString())
                if (items.isNullOrEmpty()) {
                    allResults3.gone()
                    videoWallpaper.gone()
                } else {
                    allResults3.visible()
                    videoWallpaper.visible()
                    searchWallpaperAdapter3.submitList(items)
                }
            }

            // Search text watcher
            searchText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isProgrammaticallySettingText) return // ignore programmatic changes

                    val query = s.toString()
                    if (query.isEmpty()) {
                        tagViewModel.loadAllTags()
                    } else {
                        tagViewModel.searchTag(query)
                    }
                    // Whenever typing, show trending and hide search result
                    showTrending()
                }

                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            })

            // Keyboard search action
            searchText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyBoard(binding.searchText)
                    true
                } else false
            }

            // Scroll hide keyboard
            allResults1.addHideKeyboardOnScroll()
            allResults2.addHideKeyboardOnScroll()
            allResults3.addHideKeyboardOnScroll()
            trendingRecyclerView.addHideKeyboardOnScroll()

            searchResult.setOnClickListener { hideKeyBoard(binding.searchText) }
            noDataLayout.setOnClickListener { hideKeyBoard(binding.searchText) }

            // Clear button
            closeButton.setOnClickListener {
                searchText.setText("")
                tagViewModel.loadAllTags()
                showTrending()
            }
        }
    }


    private fun RecyclerView.addHideKeyboardOnScroll() {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    hideKeyBoard(binding.searchText)
                }
            }
        })
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

    private fun showTrending() {
        binding.apply {
            trendingTitle.visible()
            trendingIcon.visible()
            trendingRecyclerView.visible()
            searchResult.gone()
        }
        currentState = ScreenState.TRENDING
    }

    private fun showSearchResult() {
        binding.apply {
            trendingTitle.gone()
            trendingIcon.gone()
            trendingRecyclerView.gone()
            searchResult.visible()
        }
        currentState = ScreenState.SEARCH_RESULT
    }

    private fun updateUIBySearchQuery(query: String) {
        if (query.isEmpty()) showTrending() else showSearchResult()
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)

        // Restore last state
        when (currentState) {
            ScreenState.TRENDING -> showTrending()
            ScreenState.SEARCH_RESULT -> showSearchResult()
        }
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@SearchWallpaperActivity, "INTER_WALLPAPER")
    }

    companion object {
        val TAG = SearchWallpaperActivity::class.java.name
    }
}
