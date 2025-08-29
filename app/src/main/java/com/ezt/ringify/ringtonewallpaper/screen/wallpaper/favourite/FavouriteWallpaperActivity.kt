package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.favourite

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.media3.common.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFavouriteWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
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
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class FavouriteWallpaperActivity :
    BaseActivity<ActivityFavouriteWallpaperBinding>(ActivityFavouriteWallpaperBinding::inflate) {
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger
    private var now = 0L

    private val favouriteWallpaperViewModel: FavouriteWallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val liveAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "preview_live_wallpaper_screen",
                "favourite_wallpaper_screen",
                duration
            )
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
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "slide_wallpaper_activity",
                "favourite_wallpaper_screen",
                duration
            )
            Log.d(TAG, "Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", -3)
            })
        }
    }

    private val singleAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            val duration = System.currentTimeMillis() - now
            analyticsLogger.logScreenGo(
                "slide_wallpaper_activity",
                "favourite_wallpaper_screen",
                duration
            )
            Log.d(TAG, "Wallpaper: $it")
            startActivity(Intent(this, SlideWallpaperActivity::class.java).apply {
                putExtra("wallpaperCategoryId", -3)
            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }
        now = System.currentTimeMillis()

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

            favouriteWallpaperViewModel.allDataEmpty.observe(this@FavouriteWallpaperActivity) { isEmpty ->
                noDataLayout1.isVisible = isEmpty
            }

            favouriteWallpaperViewModel.allLiveWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                allTrending.visible()
                openAll1.visible()
                trendingCount.visible()
                trending.visible()
                noDataLayout1.gone()
                if (items.isNullOrEmpty()) {
                    openAll1.gone()
                    trendingCount.gone()
                    trending.gone()
                    allTrending.gone()
                    allTrending.isEnabled = false
                    return@observe
                }
                allTrending.isEnabled = true
                liveAdapter.submitList(items)
            }

            favouriteWallpaperViewModel.allWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                allSingleWallpaper.visible()
                imageTitle.visible()
                imageCount.visible()
                openAll3.visible()
                noDataLayout3.gone()
                if (items.isNullOrEmpty()) {
                    allSingleWallpaper.gone()
                    imageTitle.gone()
                    imageCount.gone()
                    openAll3.gone()
                    allSingleWallpaper.isEnabled = false
                    return@observe
                }
                allSingleWallpaper.isEnabled = true
                singleAdapter.submitList(items)
            }

            favouriteWallpaperViewModel.allSlideWallpapers.observe(this@FavouriteWallpaperActivity) { items ->
                allNewWallpaper.visible()
                newWallpaper.visible()
                newWallpaperCount.visible()
                openAll2.visible()
                noDataLayout2.gone()
                if (items.isNullOrEmpty()) {
                    allNewWallpaper.gone()
                    newWallpaper.gone()
                    newWallpaperCount.gone()
                    openAll2.gone()
                    allNewWallpaper.isEnabled = false
                    return@observe
                }
                allNewWallpaper.isEnabled = true
                slideAdapter.submitList(items)

            }


            openAll1.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "preview_wallpaper_activity",
                    "favourite_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -3)
                        putExtra("type", 4)
                    })
            }

            openAll2.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "preview_wallpaper_activity",
                    "favourite_wallpaper_screen",
                    duration
                )
                startActivity(
                    Intent(
                        this@FavouriteWallpaperActivity,
                        PreviewWallpaperActivity::class.java
                    ).apply {
                        putExtra("wallpaperCategoryId", -4)
                    })
            }

            openAll3.setOnClickListener {
                val duration = System.currentTimeMillis() - now
                analyticsLogger.logScreenGo(
                    "preview_wallpaper_activity",
                    "favourite_wallpaper_screen",
                    duration
                )
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
        val TAG = FavouriteWallpaperActivity::class.java.simpleName
    }

}