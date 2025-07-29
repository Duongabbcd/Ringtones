package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySlideWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.OneItemSnapHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper.setWallpaperFromUrl
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.WallpaperTarget
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.DownloadWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.crop.CropActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog.SetWallpaperDialog
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium.PremiumWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service.SlideshowWallpaperService
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
import java.net.URL
import kotlin.math.abs

@AndroidEntryPoint
class SlideWallpaperActivity :
    BaseActivity<ActivitySlideWallpaperBinding>(ActivitySlideWallpaperBinding::inflate) {

    private val viewModel: FavouriteWallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedWallpapers
    }

    private var index = 0
    private var currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper

    private lateinit var handler: Handler
    private lateinit var carousel: Carousel

    private var lastDx: Int = 0
    private var duration = 0L
    private var downloadedUri: Uri? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var isFavorite: Boolean = false

    private val playSlideWallpaperAdapter: PlaySlideWallpaperAdapter by lazy {
        PlaySlideWallpaperAdapter(this,
            onRequestScrollToPosition = { newPosition ->
                carousel.scrollSpeed(200f)
                setUpNewPlayer(newPosition)
                handler.postDelayed({
                    playSlideWallpaperAdapter.setCurrentPlayingPosition(newPosition, false)
                }, 300)
            }
        ) { _, _ -> }
    }

    private val snapHelper: OneItemSnapHelper by lazy { OneItemSnapHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkDownloadPermissions()

        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }

        index = savedInstanceState?.getInt("wallpaper_index", 0)
            ?: allRingtones.indexOf(currentWallpaper).takeIf { it >= 0 } ?: 0
    }

    private fun checkInternetConnected(isConnected: Boolean) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            binding.noInternet.root.gone()

            handler = Handler(Looper.getMainLooper())
            observeRingtoneFromDb()

            with(binding) {
                backBtn.setOnClickListener { finish() }
                favourite.setOnClickListener { displayFavouriteIcon(true) }
                viewModel.loadWallpaperById(currentWallpaper.id)

                initViewPager()
                setUpNewPlayer(index)
                setupButtons()
            }
        }
    }

    private fun checkDownloadPermissions() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                actuallyDownloadWallpaper()
            } else {
                val permanentlyDenied = permissions.keys.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, it)
                }

                if (permanentlyDenied) showGoToSettingsDialog()
                else Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this) { goToSettings ->
            if (goToSettings) {
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
                val url = currentWallpaper.contents.first().url.full
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Check this out")
                    putExtra(Intent.EXTRA_TEXT, url)
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }

            download.setOnClickListener {
                downloadWallpaper()
            }

            wallpaper.setOnClickListener {
                if (isVideoWallpaper(currentWallpaper)) {
                    setUpVideoByCondition(currentWallpaper.contents.first().url.full)
                } else {
                    setUpPhotoByCondition(currentWallpaper.contents)
                }
            }
        }
    }

    private fun isVideoWallpaper(wallpaper: Wallpaper): Boolean {
        return wallpaper.type == 2 // Or another flag you use
    }

    private fun setUpVideoByCondition(videoUrl: String) {
        val dialog = SetWallpaperDialog(this) { result ->
            settingOption = result
            launchLiveWallpaper(this, videoUrl)
        }
        dialog.show()
    }

    fun launchLiveWallpaper(context: Context, videoUrl: String) {
        val prefs = context.getSharedPreferences("video_wallpaper", Context.MODE_PRIVATE)
        prefs.edit().putString("video_url", videoUrl).apply()

        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, VideoWallpaperService::class.java)
            )
        }
        startActivity(intent)
    }

    private fun downloadWallpaper() {
        val missing = RingtoneHelper.getMissingPhotoPermissions(this)
        if (missing.isEmpty()) {
            actuallyDownloadWallpaper()
        } else {
            requestPermissionLauncher.launch(missing.toTypedArray())
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

        val url = currentWallpaper.contents.first().url.full

        lifecycleScope.launch {
            val uri = if (isVideoWallpaper(currentWallpaper)) {
                RingtoneHelper.downloadVideo(this@SlideWallpaperActivity, url)
            } else {
                RingtoneHelper.downloadImage(this@SlideWallpaperActivity, url)
            }

            withContext(Dispatchers.Main) {
                if (uri != null) {
                    downloadedUri = uri
                    delay(5000L)
                    bottomSheet.showSuccess()
                    enableDismiss(bottomSheet)
                    viewModel.increaseDownload(currentWallpaper)
                } else {
                    bottomSheet.showFailure()
                    enableDismiss(bottomSheet)
                }
            }
        }
    }

    private fun enableDismiss(bottomSheet: DownloadWallpaperBottomSheet) {
        bottomSheet.apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        (bottomSheet as? BottomSheetDialog)?.behavior?.apply {
            isDraggable = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun observeRingtoneFromDb() {
        viewModel.wallpaper.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentWallpaper.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite else R.drawable.icon_unfavourite
            )
        }
    }

    private fun displayFavouriteIcon(manual: Boolean = false) {
        if (isFavorite && manual) {
            viewModel.deleteWallpaper(currentWallpaper)
            binding.favourite.setImageResource(R.drawable.icon_unfavourite)
            isFavorite = false
        } else if (manual) {
            viewModel.insertWallpaper(currentWallpaper)
            binding.favourite.setImageResource(R.drawable.icon_favourite)
            isFavorite = true
        }
    }

    private fun initViewPager() {
        playSlideWallpaperAdapter.submitList(allRingtones)
        carousel = Carousel(this, binding.horizontalWallpapers, playSlideWallpaperAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        snapHelper.attachToRecyclerView(binding.horizontalWallpapers)

        binding.horizontalWallpapers.apply {
            adapter = playSlideWallpaperAdapter
            initialPosition = index

            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    updateIndex(position, "onPositionChange")
                    setUpNewPlayer(position)
                }

                override fun onScroll(dx: Int, dy: Int) {
                    lastDx = dx
                }
            })

            setOnTouchListener { _, event ->
                duration = event.eventTime - event.downTime
                if (event.action == MotionEvent.ACTION_UP && duration <= 100) {
                    val newIndex = when {
                        lastDx > 0 && index < allRingtones.size - 1 -> index + 1
                        lastDx < 0 && index > 0 -> index - 1
                        else -> index
                    }
                    if (newIndex != index) {
                        updateIndex(newIndex, "touch")
                        handler.postDelayed({ setUpNewPlayer(index) }, 300)
                    }
                }
                false
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val lm = recyclerView.layoutManager as LinearLayoutManager
                    val view = snapHelper.findSnapView(lm) ?: return
                    val newIndex = lm.getPosition(view)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        index = newIndex
                        setUpNewPlayer(index)
                    }
                }
            })
        }
    }

    private fun updateIndex(newIndex: Int, caller: String) {
        Log.d("SlideWallpaper", "Index changed from $index to $newIndex by $caller")
        index = newIndex
    }

    private fun setUpNewPlayer(position: Int) {
        binding.horizontalWallpapers.smoothScrollToPosition(position)
        currentWallpaper = allRingtones[position]
        viewModel.loadWallpaperById(currentWallpaper.id)
        playSlideWallpaperAdapter.setCurrentPlayingPosition(position)
    }

    private fun setUpPhotoByCondition(contents: List<ImageContent>) {
        val dialog = SetWallpaperDialog(this) { result ->
            settingOption = result
            if (contents.size > 1) {
                lifecycleScope.launch {
                    setUpLiveWallpaperByCondition(result, contents)
                }
            } else {
                val intent = Intent(this, CropActivity::class.java).apply {
                    putExtra("imageUrl", contents.first().url.full)
                }
                cropLauncher.launch(intent)
            }
        }
        dialog.show()
    }

    private suspend fun setUpLiveWallpaperByCondition(result: Int, contents: List<ImageContent>) {
        val bitmap = urlToBitmap(contents.first().url.full) ?: return

        when (result) {
            1 -> {
                startLiveWallpaper(contents)
                setWallpaperFromUrl(this, bitmap, WallpaperTarget.LOCK)
            }

            2 -> startLiveWallpaper(contents)
            else -> {
                startLiveWallpaper(contents)
                setWallpaperFromUrl(this, bitmap, WallpaperTarget.BOTH)
            }
        }
    }

    private fun startLiveWallpaper(contents: List<ImageContent>) {
        SlideshowWallpaperService.imageUrls = contents.map { it.url.full }
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(packageName, SlideshowWallpaperService::class.java.name)
            )
        }
        startActivity(intent)
    }

    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>("croppedImageUri")
            uri?.let { loadBitmapFromUri(this, it)?.let { bmp -> continueAfterCrop(bmp) } }
        }
    }

    private fun continueAfterCrop(bitmap: Bitmap) {
        lifecycleScope.launch {
            val bottomSheet = DownloadWallpaperBottomSheet(this@SlideWallpaperActivity).apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setOnShowListener {
                    (it as? BottomSheetDialog)?.behavior?.apply {
                        isDraggable = false
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
                when (settingOption) {
                    1 -> setType("lock")
                    2 -> setType("home")
                    else -> setType("both")
                }
            }

            val success = when (settingOption) {
                1 -> setWallpaperFromUrl(this@SlideWallpaperActivity, bitmap, WallpaperTarget.LOCK)
                2 -> setWallpaperFromUrl(this@SlideWallpaperActivity, bitmap, WallpaperTarget.HOME)
                else -> setWallpaperFromUrl(
                    this@SlideWallpaperActivity,
                    bitmap,
                    WallpaperTarget.BOTH
                )
            }

            if (success) {
                bottomSheet.showSuccess()
                enableDismiss(bottomSheet)
                viewModel.increaseSet(currentWallpaper)
            } else {
                bottomSheet.showFailure()
                enableDismiss(bottomSheet)
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    }

    suspend fun urlToBitmap(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            URL(url).openStream().use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("wallpaper_index", index)
    }

    companion object {
        var imageBitmap: Bitmap? = null
        var settingOption = 0
    }
}
