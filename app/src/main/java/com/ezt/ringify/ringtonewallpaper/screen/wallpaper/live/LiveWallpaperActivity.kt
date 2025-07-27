package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityLiveWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter.Companion.VIEW_TYPE_LOADING
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LiveWallpaperActivity : BaseActivity<ActivityLiveWallpaperBinding>(
    ActivityLiveWallpaperBinding::inflate
) {
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val wallpaperViewModel: WallpaperViewModel by viewModels()
    private val wallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter().apply {
            onAllImagesLoaded = {
                // Safely post notifyDataSetChanged on RecyclerView's message queue
                binding.allCategories.post {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            connectionViewModel.isConnectedLiveData.observe(this@LiveWallpaperActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }
            val layoutManager = GridLayoutManager(this@LiveWallpaperActivity, 3)

            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (wallpaperAdapter.getItemViewType(position) == VIEW_TYPE_LOADING) {
                        3 // full-width for progress bar
                    } else {
                        1 // normal items take 1 span
                    }
                }
            }

            allCategories.layoutManager = layoutManager
            allCategories.adapter = wallpaperAdapter

            nameScreen.text = resources.getString(R.string.live)
            wallpaperViewModel.liveWallpapers.observe(this@LiveWallpaperActivity) { items ->
                wallpaperAdapter.submitList(items, true, false)
            }

        }
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            wallpaperViewModel.loadLiveWallpapers()
            binding.noInternet.root.gone()
        }
    }
}