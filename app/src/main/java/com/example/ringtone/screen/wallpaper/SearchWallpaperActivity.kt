package com.example.ringtone.screen.wallpaper

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivitySearchWallpaperBinding
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.ringtone.search.SearchRingtoneActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryViewModel.loadWallpaperCategories()
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            trendingRecyclerView.adapter = wallpaperAdapter
            trendingRecyclerView.layoutManager = GridLayoutManager(this@SearchWallpaperActivity, 2)

            categoryViewModel.wallpaperCategory.observe(this@SearchWallpaperActivity) { items ->
                wallpaperAdapter.submitList(items)
            }

            searchText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // Before text is changed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Text is changing
                    val searchText = s.toString()
                    categoryViewModel.search(searchText)
                    categoryViewModel.search.observe(this@SearchWallpaperActivity) { result ->
                        if(result.isEmpty()) {
                            noDataLayout.visible()
                            allResults.gone()
                        } else {
                            noDataLayout.gone()
                            allResults.visible()
                            categoryViewModel.submitList(result)
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // After text has changed
//                    if (ignoreTextChange) return
                    val input = s.toString()
//                    displayByCondition(input)
                }
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
        }
    }
}