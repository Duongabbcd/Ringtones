package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPremiumWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class PremiumWallpaperActivity : BaseActivity<ActivityPremiumWallpaperBinding>(
    ActivityPremiumWallpaperBinding::inflate){
    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val liveAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(
                Intent(
                    this@PremiumWallpaperActivity,
                    PreviewLiveWallpaperActivity::class.java
                ).apply {
                    putExtra("type", 4)
                })
        }
    }

    private val singleAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", 75)
                putExtra("type", 3)
            })
        }
    }
    private val slideAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", 75)
                putExtra("type", 2)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            connectionViewModel.isConnectedLiveData.observe(this@PremiumWallpaperActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }
            allTrending.adapter = liveAdapter
            allNewWallpaper.adapter = slideAdapter
            allSub1.adapter = singleAdapter

            allTrending.layoutManager = LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allNewWallpaper.layoutManager = LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allSub1.layoutManager = LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)

            wallPaperViewModel.premiumWallpapers.observe(this@PremiumWallpaperActivity) { items ->
                liveAdapter.submitList(items)
            }

            wallPaperViewModel.slideWallpaper.observe(this@PremiumWallpaperActivity) { items ->
                slideAdapter.submitList(items)
            }

            wallPaperViewModel.singleWallpapers.observe(this@PremiumWallpaperActivity) { items ->
                singleAdapter.submitList(items)
            }

            openAll1.setOnClickListener {
                startActivity(
                    Intent(
                        this@PremiumWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", 75)
                        putExtra("type", 1)
                    })
            }

            openAll2.setOnClickListener {
                startActivity(Intent(this@PremiumWallpaperActivity, PreviewWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", 75)
                    putExtra("type", 2)
                })
            }

            openAll3.setOnClickListener {
                startActivity(Intent(this@PremiumWallpaperActivity, PreviewWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", 75)
                    putExtra("type", 3)
                })
            }

            wallPaperViewModel.loading1.observe(this@PremiumWallpaperActivity) {
                loading1.isVisible = it
                newWallpaperCount.isVisible = !it

            }

            wallPaperViewModel.loading2.observe(this@PremiumWallpaperActivity) {
                loading2.isVisible = it
                trendingCount.isVisible = !it
            }

            wallPaperViewModel.loading3.observe(this@PremiumWallpaperActivity) {
                loading3.isVisible = it
                sub1Count.isVisible = !it
            }

        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            wallPaperViewModel.loadPremiumVideoWallpaper()
            wallPaperViewModel.loadSlideWallpaper()
            wallPaperViewModel.loadSingleWallpaper()
            binding.noInternet.root.gone()
        }
    }
}