package com.example.ringtone.screen.wallpaper

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivitySearchWallpaperBinding
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.ringtone.search.SearchRingtoneActivity
import com.example.ringtone.screen.wallpaper.adapter.GridWallpaperAdapter
import com.example.ringtone.screen.wallpaper.adapter.WallpaperAdapter
import com.example.ringtone.screen.wallpaper.adapter.WallpaperTrendingAdapter
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import com.example.ringtone.utils.Utils.hideKeyBoard
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

    private val searchWallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter {

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

        categoryViewModel.loadWallpaperCategories()
        binding.apply {
            backBtn.setOnClickListener {
                finish()
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
                    val query = searchText.text.toString()
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
}