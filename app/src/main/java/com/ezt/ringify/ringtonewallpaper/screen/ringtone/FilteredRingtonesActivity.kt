package com.ezt.ringify.ringtonewallpaper.screen.ringtone

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFilteredCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
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
        sortOrder = Common.getSortOrder(this)
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@FilteredRingtonesActivity)
            }

            connectionViewModel.isConnectedLiveData.observe(this@FilteredRingtonesActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }


            println("category: $categoryId")
            allCategories.adapter = ringtoneAdapter

            sortIcon.setOnClickListener {
                val dialog = SortBottomSheet(this@FilteredRingtonesActivity) { newSort ->
                    if (newSort != sortOrder) {
                        Common.setSortOrder(this@FilteredRingtonesActivity, newSort)
                        sortOrder = newSort
                        displayItems()
                    }
                }
                dialog.show()
            }

            ringtoneViewModel.popular.observe(this@FilteredRingtonesActivity) { items ->
                handleDataResult(items)
            }

            favourite.allRingtones.observe(this@FilteredRingtonesActivity) { items ->
                handleDataResult(items)
            }

            ringtoneViewModel.selectedRingtone.observe(this@FilteredRingtonesActivity) { items ->
                println("selectedRingtone: $items")
                handleDataResult(items)
            }

            loadMoreData()
        }
    }

    private fun handleDataResult(items: List<Ringtone>) {
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
                if (isAtBottom) {
                    when(categoryId) {
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

    private fun displayItems() {
        binding.apply {
            when(categoryId) {
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
        loadBanner(this, BANNER_HOME)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@FilteredRingtonesActivity)
    }
}