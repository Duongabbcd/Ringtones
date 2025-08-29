package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySearchWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.TagViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.TagTrendingAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.Utils.hideKeyBoard
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchWallpaperActivity : BaseActivity<ActivitySearchWallpaperBinding>(
    ActivitySearchWallpaperBinding::inflate
) {
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger
    private var now = 0L

    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private val tagViewModel: TagViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private var tagId = 0
    private var isFromTagClick = false

    private enum class ScreenState { TRENDING, SEARCH_RESULT }

    private var currentState = ScreenState.TRENDING

    // Trending Adapter
    private val tagTrendingAdapter: TagTrendingAdapter by lazy {
        TagTrendingAdapter { tag ->
            Log.d(TAG, "TagTrendingAdapter clicked: $tag")
            isFromTagClick = true
            tagId = tag.id

            wallpaperViewModel.resetSearchPaging()

            // Temporarily mark that we're setting text programmatically
            binding.searchText.setText(tag.name)
            binding.searchText.setSelection(tag.name.length)

            // Show search result
            showSearchResult()

            // Load wallpapers
            wallpaperViewModel.searchWallpaperByTag(tag.id)
            wallpaperViewModel.searchSingleWallpaperByTag(tag.id, 5)
            wallpaperViewModel.searchSlideWallpaperByTag(tag.id, 5)
            wallpaperViewModel.searchVideoWallpaperByTag(tag.id, 5)
        }
    }


    // Search result adapters
    private val searchWallpaperAdapter1: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "search_wallpaper_screen",
                "slide_wallpaper_screen",
                duration
            )
            startActivity(Intent(this@SearchWallpaperActivity, SlideWallpaperActivity::class.java))
        }
    }

    private val searchWallpaperAdapter2: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "search_wallpaper_screen",
                "slide_wallpaper_screen",
                duration
            )
            startActivity(Intent(this@SearchWallpaperActivity, SlideWallpaperActivity::class.java))
        }
    }

    private val searchWallpaperAdapter3: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "search_wallpaper_screen",
                "preview_live_wallpaper_screen",
                duration
            )
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
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }
        now = System.currentTimeMillis()
        loadBanner(this, BANNER_HOME)
        tagViewModel.loadAllTags()
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
            trendingRecyclerView.adapter = tagTrendingAdapter
            val layoutManager = FlexboxLayoutManager(this@SearchWallpaperActivity).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            trendingRecyclerView.layoutManager = layoutManager

            // Search result RecyclerViews
            allResults1.adapter = searchWallpaperAdapter1
            allResults2.adapter = searchWallpaperAdapter2
            allResults3.adapter = searchWallpaperAdapter3

            displayByCondition("")

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
                        wallpaperViewModel.searchSingleWallpaperByTag(tagId, 5)
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
                        wallpaperViewModel.searchSlideWallpaperByTag(tagId, 5)
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
                        wallpaperViewModel.searchVideoWallpaperByTag(tagId, 5)
                    }
                }
            })


            // Observe Tags
            tagViewModel.tag.observe(this@SearchWallpaperActivity) { items ->
                val query = searchText.text.toString()
                displayByCondition(query)
                if (query.isEmpty()) {
                    trendingTitle.text = resources.getString(R.string.hot_search)
                    trendingIcon.setImageResource(R.drawable.icon_fire)
                } else {
                    trendingTitle.text =
                        resources.getString(R.string.total_result, items.size.toString())
                    trendingIcon.setImageResource(R.drawable.icon_search)
                }
                if (items.isEmpty() && query.isEmpty()) {
                    trendingRecyclerView.visible()
                } else {
                    noDataLayout.gone()
                    if (currentState == ScreenState.TRENDING) {
                        trendingTitle.visible()
                        trendingIcon.visible()
                        trendingRecyclerView.visible()
                    }
                    tagTrendingAdapter.submitList(items)

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
                    openAll1.gone()
                    singleWallpaper.gone()
                } else {
                    allResults1.visible()
                    openAll1.visible()
                    singleWallpaper.visible()
                    searchWallpaperAdapter1.submitList(items)
                }
            }

            wallpaperViewModel.searchWallpapers2.observe(this@SearchWallpaperActivity) { items ->
                updateUIBySearchQuery(searchText.text.toString())
                if (items.isNullOrEmpty()) {
                    allResults2.gone()
                    openAll2.gone()
                    slideWallpaper.gone()
                } else {
                    allResults2.visible()
                    openAll2.visible()
                    slideWallpaper.visible()
                    searchWallpaperAdapter2.submitList(items)
                }
            }

            wallpaperViewModel.searchWallpapers3.observe(this@SearchWallpaperActivity) { items ->
                updateUIBySearchQuery(searchText.text.toString())
                if (items.isNullOrEmpty()) {
                    allResults3.gone()
                    openAll3.gone()
                    videoWallpaper.gone()
                } else {
                    allResults3.visible()
                    openAll3.visible()
                    videoWallpaper.visible()
                    searchWallpaperAdapter3.submitList(items)
                }
            }

            // Search text watcher
            searchText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    val query = s.toString()
                    if (query.isEmpty()) {
                        tagViewModel.loadAllTags(true)
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
                displayByCondition("")
                tagViewModel.loadAllTags(isReset = true)
                showTrending()
            }

            openAll1.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "search_wallpaper_screen",
                    "preview_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@SearchWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -201)
                        putExtra("tagId", tagId)
                    })
            }

            openAll2.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "search_wallpaper_screen",
                    "preview_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@SearchWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -202)
                        putExtra("tagId", tagId)
                    })
            }

            openAll3.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "search_wallpaper_screen",
                    "preview_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@SearchWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -203)
                        putExtra("tagId", tagId)
                    })
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

    private fun displayByCondition(input: String) {
        binding.apply {
            if (input.isEmpty()) {
                trendingTitle.visible()
                trendingIcon.visible()
                trendingRecyclerView.visible()

                allResults1.gone()
                allResults2.gone()
                allResults3.gone()
                closeButton.gone()
            } else {
                trendingTitle.gone()
                trendingIcon.gone()
                trendingRecyclerView.gone()

                allResults1.visible()
                allResults2.visible()
                allResults3.visible()
                closeButton.visible()
            }
        }

    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
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
        val TAG = SearchWallpaperActivity::class.java.simpleName
    }
}
