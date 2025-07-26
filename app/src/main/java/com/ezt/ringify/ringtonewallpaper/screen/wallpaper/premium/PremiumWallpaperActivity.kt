package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPremiumWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.LiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class PremiumWallpaperActivity : BaseActivity<ActivityPremiumWallpaperBinding>(
    ActivityPremiumWallpaperBinding::inflate){
    private val wallPaperViewModel: WallpaperViewModel by viewModels()

    private val liveAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(Intent(this@PremiumWallpaperActivity, PreviewLiveWallpaperActivity::class.java))
        }
    }

    private val singleAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java))
        }
    }
    private val slideAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallPaperViewModel.loadLiveWallpapers()
        wallPaperViewModel.loadSlideWallpaper()
        wallPaperViewModel.loadSingleWallpaper()
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            allTrending.adapter = liveAdapter
            allNewWallpaper.adapter = slideAdapter
            allSub1.adapter = singleAdapter

            allTrending.layoutManager = LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allNewWallpaper.layoutManager = LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allSub1.layoutManager = LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)

            wallPaperViewModel.liveWallpapers.observe(this@PremiumWallpaperActivity) { items ->
                liveAdapter.submitList(items)
            }

            wallPaperViewModel.slideWallpaper.observe(this@PremiumWallpaperActivity) { items ->
                slideAdapter.submitList(items)
            }

            wallPaperViewModel.singleWallpapers.observe(this@PremiumWallpaperActivity) { items ->
                singleAdapter.submitList(items)
            }

            openAll1.setOnClickListener {
                startActivity(Intent(this@PremiumWallpaperActivity, LiveWallpaperActivity::class.java))
            }

            openAll2.setOnClickListener {
                startActivity(Intent(this@PremiumWallpaperActivity, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 75)
                    putExtra("isPremium", true)
                })
            }

            openAll3.setOnClickListener {
                startActivity(Intent(this@PremiumWallpaperActivity, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 75)
                    putExtra("isPremium", false)
                })
            }

        }
    }
}