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
import android.os.PersistableBundle
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
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewLiveWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.OneItemSnapHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.DownloadWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog.SetWallpaperDialog
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity.Companion.currentIndex
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
    private val favouriteViewModel: FavouriteWallpaperViewModel by viewModels()
    private val wallpaperViewModel: WallpaperViewModel by viewModels()

    private var isLoadingMore = false
    private val addedWallpaperIds = mutableSetOf<Int>() // Track already added

    private val type by lazy {
        intent.getIntExtra("type", -1)
    }

    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val playSlideWallpaperAdapter: PlayLiveWallpaperAdapter by lazy {
        PlayLiveWallpaperAdapter(this)
    }

    private lateinit var carousel: Carousel
    private var index = 0
    private var currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper

    private val allWallpapers by lazy {
        RingtonePlayerRemote.allSelectedWallpapers
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var downloadedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkDownloadPermissions()
        Log.d("PreviewLive", "savedInstanceState: $savedInstanceState")
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("current_index", 0)
            index = currentIndex
            Log.d("PreviewLive", "Restored index: $index")
        } else {
            currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper
            index = allWallpapers.indexOf(currentWallpaper).takeIf { it >= 0 } ?: 0
            currentIndex = allWallpapers.indexOf(currentWallpaper).takeIf { it >= 0 } ?: 0
            Log.d("PreviewLive", "Initial index: $index")
        }

        addedWallpaperIds.addAll(allWallpapers.map { it.id })
        connectionViewModel.isConnectedLiveData.observe(this@PreviewLiveWallpaperActivity) { isConnected ->
            println("isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }
        when (type) {
            1 -> {
                favouriteViewModel.allLiveWallpapers.observe(this@PreviewLiveWallpaperActivity) { items ->
                    appendNewRingtones(items)
                }
            }

            2 -> wallpaperViewModel.liveWallpapers.observe(this@PreviewLiveWallpaperActivity) { items ->
                appendNewRingtones(items)
            }

            4 -> wallpaperViewModel.premiumWallpapers.observe(this@PreviewLiveWallpaperActivity) { items ->
                appendNewRingtones(items)
            }

            else -> wallpaperViewModel.liveWallpapers.observe(this@PreviewLiveWallpaperActivity) { items ->
                appendNewRingtones(items)
            }
        }

    }

    private fun appendNewRingtones(newItems: List<Wallpaper>) {
        val oldSize = allWallpapers.size
        println("appendNewRingtones 0: ${newItems.size}")
        val distinctItems = newItems.filter { it.id !in addedWallpaperIds }

        if (distinctItems.isNotEmpty()) {
            allWallpapers.addAll(distinctItems)
            distinctItems.forEach { addedWallpaperIds.add(it.id) }
            println("appendNewRingtones 1: ${allWallpapers.size}")
            playSlideWallpaperAdapter.submitList(allWallpapers.toList())
            playSlideWallpaperAdapter.notifyItemRangeInserted(oldSize, distinctItems.size)
        }

        isLoadingMore = false
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
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
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
                    favouriteViewModel.increaseDownload(currentWallpaper)
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
        playSlideWallpaperAdapter.submitList(allWallpapers)
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
                currentIndex = position
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
        currentWallpaper = allWallpapers[position]
        Log.d("PreviewLive", "setUpNewPlayer: position=$position wallpaper=$currentWallpaper")
        favouriteViewModel.loadLiveWallpaperById(currentWallpaper.id)
        index = position
        currentIndex = position
    }

    private val snapHelper: OneItemSnapHelper by lazy {
        OneItemSnapHelper()
    }

    private var isFavorite: Boolean = false

    private fun observeRingtoneFromDb() {
        favouriteViewModel.liveWallpaper.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentWallpaper.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite else R.drawable.icon_unfavourite
            )
        }
    }

    private fun displayFavouriteIcon(isManualChange: Boolean = false) {
        if (isFavorite) {
            if (isManualChange) {
                favouriteViewModel.deleteLiveWallpaper(currentWallpaper)
                binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                isFavorite = false
            }
        } else {
            if (isManualChange) {
                favouriteViewModel.insertLiveWallpaper(currentWallpaper)
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

                favouriteViewModel.loadLiveWallpaperById(currentWallpaper.id)
                favouriteViewModel.loadLiveAllWallpapers()

                favourite.setOnClickListener { displayFavouriteIcon(true) }
                loadMoreData()
                initViewPager()
                setUpNewPlayer(index)
                setupButtons()
            }
            binding.noInternet.root.gone()
        }
    }

    private fun loadMoreData() {
        binding.horizontalWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dx <= 0) return  // Only when scrolling right

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isAtEnd = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 2

                if (isAtEnd && !isLoadingMore && type != 1) {
                    isLoadingMore = true
                    when (type) {
                        2 -> wallpaperViewModel.loadLiveWallpapers()

                        4 -> wallpaperViewModel.loadPremiumVideoWallpaper()

                        else -> wallpaperViewModel.loadLiveWallpapers()
                    }

                }
            }
        })
    }

    companion object {
        var settingOption = 0
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        super.onDestroy()
        PlayerManager.release()
        CacheUtil.release(this)
    }

    override fun onStop() {
        super.onStop()
        PlayerManager.release()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("current_index", currentIndex)
        Log.d("PreviewLive", "Saving currentIndex: $currentIndex")
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

