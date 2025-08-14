package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.favourite

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.media3.common.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFavouriteWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class FavouriteWallpaperActivity :
    BaseActivity<ActivityFavouriteWallpaperBinding>(ActivityFavouriteWallpaperBinding::inflate) {

    private val favouriteWallpaperViewModel: FavouriteWallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val liveAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            Log.d(TAG, "Wallpaper: $it")
            startActivity(
                Intent(
                    this@FavouriteWallpaperActivity,
                    PreviewLiveWallpaperActivity::class.java
                ).apply {
                    putExtra("wallpaperCategoryId", -3)
                    putExtra("type", 1)
                })
        }
    }

    private val slideAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            Log.d(TAG, "Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", -3)
            })
        }
    }

    private val singleAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            Log.d(TAG, "Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", -3)
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBanner(this)
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@FavouriteWallpaperActivity,
                    "INTER_WALLPAPER"
                )
            }
            connectionViewModel.isConnectedLiveData.observe(this@FavouriteWallpaperActivity) { isConnected ->
                Log.d(TAG, "isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }
            allTrending.adapter = liveAdapter
            allNewWallpaper.adapter = slideAdapter
            allSingleWallpaper.adapter = singleAdapter

            allTrending.layoutManager =
                LinearLayoutManager(this@FavouriteWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allNewWallpaper.layoutManager =
                LinearLayoutManager(this@FavouriteWallpaperActivity, RecyclerView.HORIZONTAL, false)

            allSingleWallpaper.layoutManager =
                LinearLayoutManager(this@FavouriteWallpaperActivity, RecyclerView.HORIZONTAL, false)

            favouriteWallpaperViewModel.allLiveWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                allTrending.visible()
                noDataLayout1.gone()
                if (items.isNullOrEmpty()) {
                    noDataLayout1.visible()
                    allTrending.visibility = View.INVISIBLE
                    allTrending.isEnabled = false
                    return@observe
                }
                allTrending.isEnabled = true
                liveAdapter.submitList(items)
            }

            favouriteWallpaperViewModel.allWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                allSingleWallpaper.visible()
                noDataLayout3.gone()
                if (items.isNullOrEmpty()) {
                    noDataLayout3.visible()
                    allSingleWallpaper.visibility = View.INVISIBLE
                    allSingleWallpaper.isEnabled = false
                    return@observe
                }
                allSingleWallpaper.isEnabled = true
                singleAdapter.submitList(items)
            }

            favouriteWallpaperViewModel.allSlideWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                allNewWallpaper.visible()
                noDataLayout2.gone()
                if (items.isNullOrEmpty()) {
                    noDataLayout2.visible()
                    allNewWallpaper.visibility = View.INVISIBLE
                    allNewWallpaper.isEnabled = false
                    return@observe
                }
                allNewWallpaper.isEnabled = true
                slideAdapter.submitList(items)

            }


            openAll1.setOnClickListener {
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -3)
                        putExtra("type", 1)
                    })
            }

            openAll2.setOnClickListener {
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -4)
                    })
            }

            openAll3.setOnClickListener {
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -5)
                    })
            }



            favouriteWallpaperViewModel.loading1.observe(this@FavouriteWallpaperActivity) {
                loading1.isVisible = it
                newWallpaperCount.isVisible = !it

            }

            favouriteWallpaperViewModel.loading2.observe(this@FavouriteWallpaperActivity) {
                loading2.isVisible = it
                trendingCount.isVisible = !it
            }

            favouriteWallpaperViewModel.loading3.observe(this@FavouriteWallpaperActivity) {
                loading3.isVisible = it
                imageCount.isVisible = !it
            }

        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            favouriteWallpaperViewModel.loadAllWallpapers()
            favouriteWallpaperViewModel.loadLiveAllWallpapers()
            favouriteWallpaperViewModel.loadSlideAllWallpapers()
            binding.noInternet.root.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this)
    }

    companion object {
        val TAG = FavouriteWallpaperActivity::class.java.name
    }

}