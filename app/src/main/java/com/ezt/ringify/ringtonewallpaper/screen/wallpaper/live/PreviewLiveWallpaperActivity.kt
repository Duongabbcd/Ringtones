package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewLiveWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.OneItemSnapHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.DownloadWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog.SetWallpaperDialog
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service.VideoWallpaperService
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PreviewLiveWallpaperActivity :
    BaseActivity<ActivityPreviewLiveWallpaperBinding>(ActivityPreviewLiveWallpaperBinding::inflate) {
    private val viewModel: FavouriteWallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val playSlideWallpaperAdapter: PlayLiveWallpaperAdapter by lazy {
        PlayLiveWallpaperAdapter()
    }

    private lateinit var carousel: Carousel
    private var index = 0
    private var currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedWallpapers
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var downloadedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("wallpaper_index", 0)
            Log.d("PreviewLive", "Restored index: $index")
        } else {
            currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper
            index = allRingtones.indexOf(currentWallpaper).takeIf { it >= 0 } ?: 0
            Log.d("PreviewLive", "Initial index: $index")
        }
        connectionViewModel.isConnectedLiveData.observe(this@PreviewLiveWallpaperActivity) { isConnected ->
            println("isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }


        checkDownloadPermissions()

    }

    private fun checkDownloadPermissions() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                actuallyDownloadWallpaper()
            } else {
                val permanentlyDenied = permissions.keys.any { permission ->
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }
                if (permanentlyDenied) {
                    showGoToSettingsDialog()
                } else {
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this) { result ->
            if (result) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            share.setOnClickListener {
                val videoUrl = currentWallpaper.contents.first().url.full
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check out this video")
                    putExtra(Intent.EXTRA_TEXT, videoUrl)
                }
                startActivity(Intent.createChooser(shareIntent, "Share video via"))
            }

            download.setOnClickListener {
                downloadWallpaper()
            }

            wallpaper.setOnClickListener {
                val videoUrl = currentWallpaper.contents.first().url.full
                setUpVideoByCondition(videoUrl)
            }
        }
    }

    private fun setUpVideoByCondition(videoUrl: String) {
        val dialog = SetWallpaperDialog(this) { result ->
            println("setUpVideoByCondition: $result")
            settingOption = result
            launchLiveWallpaper(this, videoUrl)
        }
        dialog.show()
    }

    fun launchLiveWallpaper(context: Context, videoUrl: String) {
        // Save the video URL to SharedPreferences
        val prefs = context.getSharedPreferences("video_wallpaper", Context.MODE_PRIVATE)
        prefs.edit().putString("video_url", videoUrl).apply()
        Log.d("LivePreview", "Saved wallpaper URL: $videoUrl")

        // Force user to re-apply the same live wallpaper
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, VideoWallpaperService::class.java)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }


    private fun downloadWallpaper() {
        val missingPermissions = RingtoneHelper.getMissingPhotoPermissions(this)
        if (missingPermissions.isEmpty()) {
            actuallyDownloadWallpaper()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun actuallyDownloadWallpaper(isBackground: Boolean = false) {
        val bottomSheet = DownloadWallpaperBottomSheet(this).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnShowListener { dialog ->
                (dialog as? BottomSheetDialog)?.behavior?.apply {
                    isDraggable = false
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        if (!isBackground) bottomSheet.show()

        val ringtoneUrl = currentWallpaper.contents.first().url.full
        lifecycleScope.launch {
            val uri = RingtoneHelper.downloadVideo(this@PreviewLiveWallpaperActivity, ringtoneUrl)
            withContext(Dispatchers.Main) {
                if (uri != null) {
                    downloadedUri = uri
                    delay(5000L)
                    bottomSheet.showSuccess()
                    bottomSheet.setCancelable(true)
                    bottomSheet.setCanceledOnTouchOutside(true)
                    (bottomSheet as? BottomSheetDialog)?.behavior?.apply {
                        isDraggable = true
                        state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    viewModel.increaseDownload(currentWallpaper)
                } else {
                    bottomSheet.showFailure()
                    bottomSheet.setCancelable(true)
                    bottomSheet.setCanceledOnTouchOutside(true)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        playSlideWallpaperAdapter.submitList(allRingtones)
        playSlideWallpaperAdapter.setCurrentPlayingPosition(index)

        carousel = Carousel(this, binding.horizontalWallpapers, playSlideWallpaperAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        snapHelper.attachToRecyclerView(binding.horizontalWallpapers)

        binding.horizontalWallpapers.apply {
            adapter = playSlideWallpaperAdapter
            initialPosition = index
        }

        var isScrolling = false
        var pendingPosition: Int? = null

        carousel.addCarouselListener(object : CarouselListener {
            override fun onPositionChange(position: Int) {
                Log.d("Carousel", "onPositionChange: $position")
                // Immediately update playing position and player
                index = position
                playSlideWallpaperAdapter.setCurrentPlayingPosition(position)
                setUpNewPlayer(position)
                pendingPosition = null
            }

            override fun onScroll(dx: Int, dy: Int) {
                isScrolling = true
            }
        })

        binding.horizontalWallpapers.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> isScrolling = true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isScrolling = false
                    pendingPosition?.let { pos ->
                        if (pos != index) {
                            updateIndex(pos, "TOUCH")
                            setUpNewPlayer(pos)
                            playSlideWallpaperAdapter.setCurrentPlayingPosition(pos)
                        }
                        pendingPosition = null
                    }
                }
            }
            false
        }

        binding.horizontalWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isScrolling = false
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val snapView = snapHelper.findSnapView(layoutManager) ?: return
                    val snappedPosition = layoutManager.getPosition(snapView)
                    if (snappedPosition != index) {
                        updateIndex(snappedPosition, "SCROLL_IDLE")
                        setUpNewPlayer(snappedPosition)
                        playSlideWallpaperAdapter.setCurrentPlayingPosition(snappedPosition)
                    }
                    pendingPosition = null
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isScrolling = true
                }
            }
        })
    }

    private fun setUpNewPlayer(position: Int) {
        binding.horizontalWallpapers.smoothScrollToPosition(position)
        currentWallpaper = allRingtones[position]
        Log.d("PreviewLive", "setUpNewPlayer: position=$position wallpaper=$currentWallpaper")
        viewModel.loadWallpaperById(currentWallpaper.id)
        index = position
    }

    private val snapHelper: OneItemSnapHelper by lazy {
        OneItemSnapHelper()
    }

    private var isFavorite: Boolean = false

    private fun observeRingtoneFromDb() {
        viewModel.wallpaper.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentWallpaper.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite else R.drawable.icon_unfavourite
            )
        }
    }

    private fun displayFavouriteIcon(isManualChange: Boolean = false) {
        if (isFavorite) {
            if (isManualChange) {
                viewModel.deleteWallpaper(currentWallpaper)
                binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                isFavorite = false
            }
        } else {
            if (isManualChange) {
                viewModel.insertWallpaper(currentWallpaper)
                binding.favourite.setImageResource(R.drawable.icon_favourite)
                isFavorite = true
            }
        }
    }

    private fun updateIndex(newIndex: Int, caller: String) {
        Log.d("WallpaperActivity", "Index changed from $index to $newIndex by $caller")
        index = newIndex
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            binding.apply {
                observeRingtoneFromDb()

                backBtn.setOnClickListener { finish() }

                viewModel.loadWallpaperById(currentWallpaper.id)

                favourite.setOnClickListener { displayFavouriteIcon(true) }

                initViewPager()
                setUpNewPlayer(index)
                setupButtons()
            }
            binding.noInternet.root.gone()
        }
    }

    companion object {
        var settingOption = 0
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        PlayerManager.release()
        CacheUtil.release()
    }
}

