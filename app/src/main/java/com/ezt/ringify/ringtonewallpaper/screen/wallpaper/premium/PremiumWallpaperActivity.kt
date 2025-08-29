package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPremiumWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class PremiumWallpaperActivity : BaseActivity<ActivityPremiumWallpaperBinding>(
    ActivityPremiumWallpaperBinding::inflate
) {
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger
    private var now = 0L

    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val liveAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "preview_live_wallpaper_screen",
                "premium_wallpaper_screen",
                duration
            )
            Log.d(TAG, "Wallpaper: $it")
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
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "slide_wallpaper_screen",
                "premium_wallpaper_screen",
                duration
            )
            Log.d(TAG, "Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", 75)
                putExtra("type", 3)
            })
        }
    }
    private val slideAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "slide_wallpaper_screen",
                "premium_wallpaper_screen",
                duration
            )
            Log.d(TAG, "Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", 75)
                putExtra("type", 2)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }
        now = System.currentTimeMillis()
        loadBanner(this@PremiumWallpaperActivity, BANNER_HOME)
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@PremiumWallpaperActivity,
                    "INTER_WALLPAPER"
                )
            }
            connectionViewModel.isConnectedLiveData.observe(this@PremiumWallpaperActivity) { isConnected ->
                Log.d(TAG, "isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }
            allTrending.adapter = liveAdapter
            allNewWallpaper.adapter = slideAdapter
            allSub1.adapter = singleAdapter

            allTrending.layoutManager =
                LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allNewWallpaper.layoutManager =
                LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)
            allSub1.layoutManager =
                LinearLayoutManager(this@PremiumWallpaperActivity, RecyclerView.HORIZONTAL, false)


            openAll1.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "preview_wallpaper_screen",
                    "premium_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@PremiumWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", 75)
                        putExtra("type", 4)
                    })
            }

            openAll2.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "preview_wallpaper_screen",
                    "premium_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@PremiumWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", 75)
                        putExtra("type", 2)
                    })
            }

            openAll3.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "preview_wallpaper_screen",
                    "premium_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@PremiumWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", 75)
                        putExtra("type", 3)
                    })
            }

            wallPaperViewModel.loading1.observe(this@PremiumWallpaperActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        Wallpaper.EMPTY_WALLPAPER
                    }
                    openAll1.isEnabled = false
                    allTrending.isEnabled = false
                    liveAdapter.submitList(loadingItems)

                } else {
                    wallPaperViewModel.premiumWallpapers.observe(this@PremiumWallpaperActivity) { items ->
                        liveAdapter.submitList(items, true)
                    }
                    openAll1.isEnabled = true
                    allTrending.isEnabled = true
                }
            }

            wallPaperViewModel.loading2.observe(this@PremiumWallpaperActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        Wallpaper.EMPTY_WALLPAPER
                    }

                    openAll2.isEnabled = false
                    allNewWallpaper.isEnabled = false
                    slideAdapter.submitList(loadingItems)

                } else {
                    wallPaperViewModel.slideWallpaper.observe(this@PremiumWallpaperActivity) { items ->
                        slideAdapter.submitList(items, true)
                    }

                    openAll2.isEnabled = true
                    allNewWallpaper.isEnabled = true
                }
            }


            wallPaperViewModel.loading3.observe(this@PremiumWallpaperActivity) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        Wallpaper.EMPTY_WALLPAPER
                    }

                    openAll3.isEnabled = false
                    allSub1.isEnabled = false
                    singleAdapter.submitList(loadingItems)

                } else {
                    wallPaperViewModel.singleWallpapers.observe(this@PremiumWallpaperActivity) { items ->
                        singleAdapter.submitList(items, true)
                    }

                    openAll3.isEnabled = true
                    allSub1.isEnabled = true
                }
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

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@PremiumWallpaperActivity, "INTER_WALLPAPER")

    }

    companion object {
        val TAG = PreviewWallpaperActivity::class.java.simpleName
    }
}