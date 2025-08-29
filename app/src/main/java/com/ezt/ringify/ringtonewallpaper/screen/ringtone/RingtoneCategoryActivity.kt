package com.ezt.ringify.ringtonewallpaper.screen.ringtone

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityRingtoneCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.model.Category.Companion.EMPTY_CATEGORY
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.now
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.adapter.CategoryDetailAdapter
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity.Companion.backToScreen
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RingtoneCategoryActivity: BaseActivity<ActivityRingtoneCategoryBinding>(ActivityRingtoneCategoryBinding::inflate){
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val favouriteViewModel: FavouriteRingtoneViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val categoryDetailAdapter: CategoryDetailAdapter by lazy {
        CategoryDetailAdapter { category ->
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "filter_ringtone_screen",
                "ringtone_category_screen",
                duration
            )

            startActivity(Intent(this, FilteredRingtonesActivity::class.java).apply {
                putExtra("categoryId", category.id)
                putExtra("categoryName", category.name)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this, BANNER_HOME)

        binding.apply {
            allCategories.adapter = categoryDetailAdapter
            connectionViewModel.isConnectedLiveData.observe(this@RingtoneCategoryActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            backBtn.setOnClickListener {
                backToScreen(this@RingtoneCategoryActivity)
            }


            categoryViewModel.ringtoneCategory.observe(this@RingtoneCategoryActivity) {items ->
                val allCategories = mutableListOf<Category>()

                favouriteViewModel.allRingtones.observe(this@RingtoneCategoryActivity) { result ->
                    println("categoryViewModel: $result")
                    allCategories.add(EMPTY_CATEGORY.copy(contentCount = result.size))

                }

                allCategories.addAll(items)
                categoryDetailAdapter.submitList(allCategories)
            }

        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            categoryViewModel.loadRingtoneCategories()
            favouriteViewModel.loadAllRingtones()
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
                if (isAtBottom) {
                    categoryViewModel.loadRingtoneCategories()
                }
            }
        })

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
        backToScreen(this)
    }
}