package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.favourite

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFavouriteWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
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
            println("Wallpaper: $it")
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

    private val singleAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", -3)
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            connectionViewModel.isConnectedLiveData.observe(this@FavouriteWallpaperActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }
            allTrending.adapter = liveAdapter
            allNewWallpaper.adapter = singleAdapter

            allTrending.layoutManager =
                LinearLayoutManager(this@FavouriteWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allNewWallpaper.layoutManager =
                LinearLayoutManager(this@FavouriteWallpaperActivity, RecyclerView.HORIZONTAL, false)

            favouriteWallpaperViewModel.allLiveWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                liveAdapter.submitList(items)
            }

            favouriteWallpaperViewModel.allWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                singleAdapter.submitList(items)
            }


            openAll1.setOnClickListener {
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        PreviewLiveWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -3)
                        putExtra("type", 1)
                    })
            }

            openAll2.setOnClickListener {
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        SlideWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -3)
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
            binding.noInternet.root.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        if (RemoteConfig.BANNER_COLLAP_ALL_070625 != "0") {
            AdsManager.showAdBanner(
                this,
                BANNER_HOME,
                binding.frBanner,
                binding.view,
                isCheckTestDevice = false
            ) {}
        }
    }

    override fun onBackPressed() {
        finish()

    }
}