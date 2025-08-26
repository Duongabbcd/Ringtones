package com.ezt.ringify.ringtonewallpaper.screen.ringtone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFilteredCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilteredRingtonesActivity : BaseActivity<ActivityFilteredCategoryBinding>(
    ActivityFilteredCategoryBinding::inflate
) {
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val favourite: FavouriteRingtoneViewModel by viewModels()
    private val ringtoneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter { ringTone ->
            RingtonePlayerRemote.setCurrentRingtone(ringTone)
            startActivity(Intent(this, RingtoneActivity::class.java).apply {
                putExtra("categoryId", categoryId)
            })
        }
    }

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -100)
    }

    private val categoryName by lazy {
        intent.getStringExtra("categoryName")
    }

    private lateinit var sortOrder: String

    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this@FilteredRingtonesActivity, BANNER_HOME)
        sortOrder = "name+asc"
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@FilteredRingtonesActivity)
            }

            connectionViewModel.isConnectedLiveData.observe(this@FilteredRingtonesActivity) { isConnected ->
                Log.d(TAG, "isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }


            Log.d(TAG, "category: $categoryId")
            allCategories.adapter = ringtoneAdapter

            sortIcon.setOnClickListener {
                val dialog = SortBottomSheet(this@FilteredRingtonesActivity) { newSort ->
                    println("SortBottomSheet: $newSort and $sortOrder")
                    // Reset pagination state before loading sorted data
                    ringtoneViewModel.apply {
                        hasMorePages1 = true
                        hasMorePages2 = true
                        hasMorePages3 = true
                        currentPage1 = 1
                        currentPage2 = 1
                        currentPage3 = 1
                        allWallpapers1.clear()
                        allWallpapers2.clear()
                        allWallpapers3.clear()
                    }
                    sortOrder = newSort
                    Common.setSortOrder(this@FilteredRingtonesActivity, newSort)
                    displayItems(true)
                }
                dialog.currentSortOrder = ""
                dialog.show()
            }

            ringtoneViewModel.loading1.observe(this@FilteredRingtonesActivity) { isLoading ->
                progressBar.isVisible = isLoading
            }

            ringtoneViewModel.loading.observe(this@FilteredRingtonesActivity) { isLoading ->
                progressBar.isVisible = isLoading
            }

            ringtoneViewModel.popular.observe(this@FilteredRingtonesActivity) { items ->
                handleDataResult(items)
            }

            ringtoneViewModel.customRingtones.observe(this@FilteredRingtonesActivity) { items ->
                handleDataResult(items)
            }

            favourite.allRingtones.observe(this@FilteredRingtonesActivity) { items ->
                handleDataResult(items)
            }

            ringtoneViewModel.selectedRingtone.observe(this@FilteredRingtonesActivity) { items ->
                println("selectedRingtone: $items")
                handleDataResult(items)
            }

        }
    }

    private fun handleDataResult(items: List<Ringtone>) {
        println
        if (items.isEmpty()) {
            binding.noDataLayout.visible()
            binding.allCategories.gone()
        } else {
            binding.noDataLayout.gone()
            binding.allCategories.visible()
            ringtoneAdapter.submitList1(items)
        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            binding.nameScreen.isSelected = true
            displayItems()
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }

    private fun loadMoreData() {
        binding.allCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isAtBottom = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 5
                if (isAtBottom && categoryId !in listOf<Int>(-101, -102, -103)) {
                    when(categoryId) {
//                        -101 -> {
//                            binding.nameScreen.text = getString(R.string.new_ringtones)
//                            ringtoneViewModel.loadNewRingtones()
//                        }
//
//                        -102 -> {
//                            binding.nameScreen.text = getString(R.string.weekly_trending)
//                            ringtoneViewModel.loadWeeklyTrendingRingtones()
//                        }
//
//                        -103 -> {
//                            binding.nameScreen.text = getString(R.string.editor_s_choices)
//                            ringtoneViewModel.loadEditorChoicesRingtones()
//                        }
                        -100 -> {
                            ringtoneViewModel.loadPopular(sortOrder)
                            binding.nameScreen.text = getString(R.string.popular)
                        }
                        -99 -> {
                            favourite.loadAllRingtones()
                            binding.nameScreen.text = getString(R.string.favourite)
                        }
                        else ->{
                            binding.nameScreen.text = categoryName ?: getString(R.string.unknown_cat)
                            ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                        }
                    }
                }
            }
        })

    }

    private fun displayItems(isSortOrder: Boolean = false) {
        println("displayItems: $categoryId and $sortOrder")
        binding.apply {
            when(categoryId) {
                -101 -> {
                    ringtoneViewModel.loadNewRingtones(if (isSortOrder) sortOrder else "updated_at+desc")
                    nameScreen.text = getString(R.string.new_ringtones)
                }

                -102 -> {
                    ringtoneViewModel.loadWeeklyTrendingRingtones(if (isSortOrder) sortOrder else "updated_at+desc")
                    nameScreen.text = getString(R.string.weekly_trending)
                }

                -103 -> {
                    ringtoneViewModel.loadEditorChoicesRingtones(if (isSortOrder) sortOrder else "updated_at+desc")
                    nameScreen.text = getString(R.string.editor_s_choices)
                }

                -100 -> {
                    ringtoneViewModel.loadPopular(sortOrder)
                    nameScreen.text = getString(R.string.popular)
                }

                -99 -> {
                    favourite.loadAllRingtones()
                    nameScreen.text = getString(R.string.favourite)
                }

                else -> {
                    nameScreen.text = categoryName ?: getString(R.string.unknown_cat)
                    ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(
            this,
            alias = InterAds.ALIAS_INTER_RINGTONE,
            adUnit = InterAds.INTER_RINGTONE
        )
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@FilteredRingtonesActivity)
    }

    companion object {
        private const val TAG = "FilteredRingtonesActivity"
    }
}