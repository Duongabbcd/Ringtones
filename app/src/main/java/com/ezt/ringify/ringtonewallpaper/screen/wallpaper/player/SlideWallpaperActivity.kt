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
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.ads.new.RewardAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySlideWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.reward.RewardBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.OneItemSnapHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper.setWallpaperFromUrl
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.WallpaperTarget
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.DownloadWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.crop.CropActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog.SetWallpaperDialog
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service.SlideshowWallpaperService
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val playSlideWallpaperAdapter: PlaySlideWallpaperAdapter by lazy {
        PlaySlideWallpaperAdapter(this, onRequestScrollToPosition = { newPosition ->
            carousel.scrollSpeed(200f)
            setUpNewPlayer(newPosition)
            handler.postDelayed({
                playSlideWallpaperAdapter.setCurrentPlayingPosition(
                    newPosition,
                    false
                )
            }, 300)
        }
        ) { result, id -> }
    }
    private lateinit var handler: Handler
    private lateinit var carousel: Carousel

    private var index = 0

    private var currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedWallpapers
    }

    private var lastDx: Int = 0
    private var duration = 0L
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var downloadedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkDownloadPermissions()
        connectionViewModel.isConnectedLiveData.observe(this@SlideWallpaperActivity) { isConnected ->
            println("isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }

        if (savedInstanceState != null) {

            index = savedInstanceState.getInt("wallpaper_index", 0)
            println("savedInstanceState 0: $index")
        } else {
            currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper
            index = allRingtones.indexOf(currentWallpaper)
            binding.horizontalWallpapers.post {
                val layoutManager = binding.horizontalWallpapers.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    layoutManager.scrollToPosition(index)
                }
            }
            println("savedInstanceState 1: $index")
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
                // Loop through each permission
                val permanentlyDenied = permissions.keys.any { permission ->
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }

                if (permanentlyDenied) {
                    // User denied and selected "Don't ask again"
                    showGoToSettingsDialog()
                } else {
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }


    private var returnedFromSettings = false
    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this@SlideWallpaperActivity) { result ->
            if (result) {
                returnedFromSettings = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun setupButtons() {

        binding.apply {
            share.setOnClickListener {
                val total = currentWallpaper.contents.size
                if (total == 1) {
                    checkPayBeforeNormalWallpaper {
                        val imageUrl = currentWallpaper.contents.first().url.full
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, imageUrl)
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share image via"))
                    }
                } else {
                    checkPayBeforeSpecialWallpaper {
                        val imageUrls = currentWallpaper.contents.map { it.url.full }
                        val shareText = imageUrls.joinToString(separator = "\n") // one URL per line

                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }

                        startActivity(Intent.createChooser(shareIntent, "Share images via"))
                    }
                }
            }

            download.setOnClickListener {
                val imageUrl = currentWallpaper.contents
                if (imageUrl.size == 1) {
                    checkPayBeforeNormalWallpaper {
                        downloadWallpaper()
                    }
                } else {
                    checkPayBeforeSpecialWallpaper {
                        downloadWallpaper()
                    }
                }
            }

            wallpaper.setOnClickListener {
                val imageUrl = currentWallpaper.contents
                setUpPhotoByCondition(imageUrl)
            }

        }
    }

    private fun checkPayBeforeSpecialWallpaper(onClickListener: () -> Unit) {
        val listName = mutableListOf<Int>()
        val origin = Common.getAllFreeWallpapers(this@SlideWallpaperActivity)
        listName.addAll(origin)
        if (!listName.contains(currentWallpaper.id)) {
            val rewardBottomSheet = RewardBottomSheet(this@SlideWallpaperActivity) {
                RewardAds.showAds(this@SlideWallpaperActivity, object : RewardAds.RewardCallback {
                    override fun onAdShowed() {
                        Log.d(TAG, "onAdShowed")
                    }

                    override fun onAdDismiss() {
                        Log.d(TAG, "onAdDismiss")
                        if (listName.size > RemoteConfig.totalFreeRingtones.toInt()) {
                            listName.drop(0)
                        }
                        listName.add(currentWallpaper.id)
                        Common.setAllFreeWallpapers(this@SlideWallpaperActivity, listName)
                        onClickListener()
                    }

                    override fun onAdFailedToShow() {
                        Log.d(TAG, "onAdFailedToShow")
                        onClickListener()
                    }

                    override fun onEarnedReward() {
                        Log.d(TAG, "onEarnedReward")

                    }

                    override fun onPremium() {
                        Log.d(TAG, "onPremium")
                        onClickListener()
                    }

                })

            }
            rewardBottomSheet.show()

        } else {
            onClickListener()
        }
    }

    private fun checkPayBeforeNormalWallpaper(onClickListener: () -> Unit) {
        val listName = mutableListOf<Int>()
        val origin = Common.getAllFreeWallpapers(this@SlideWallpaperActivity)
        listName.addAll(origin)
        if (!listName.contains(currentWallpaper.id)) {
            InterAds.showPreloadInter(
                this@SlideWallpaperActivity,
                InterAds.ALIAS_INTER_DOWNLOAD,
                onLoadDone = {
                    if (listName.size > RemoteConfig.totalFreeRingtones.toInt()) {
                        listName.drop(0)
                    }
                    listName.add(currentWallpaper.id)
                    Common.setAllFreeWallpapers(this@SlideWallpaperActivity, listName)
                    onClickListener()
                },
                onLoadFailed = {
                    onClickListener()
                })

        } else {
            onClickListener()
        }
    }


    private val cropLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>("croppedImageUri")
            println("cropLauncher: $uri")
            if (uri != null) {
                val bitmap = loadBitmapFromUri(this, uri)
                if (bitmap != null) {
                    continueAfterCrop(bitmap)
                }
            }
        }
    }

    private fun continueAfterCrop(bitmap: Bitmap) {
        println("continueAfterCrop: $bitmap and $settingOption")
        lifecycleScope.launch {
            val bottomSheet = DownloadWallpaperBottomSheet(this@SlideWallpaperActivity)
            bottomSheet.apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setOnShowListener { dialog ->
                    val b = (dialog as BottomSheetDialog).behavior
                    b.isDraggable = false
                    b.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }


            val isSuccess: Boolean = when (settingOption) {
                1 -> {
                    bottomSheet.setType("lock")
                    bottomSheet.dismiss()
                    setWallpaperFromUrl(
                        context = this@SlideWallpaperActivity,
                        bitmap = bitmap,
                        target = WallpaperTarget.LOCK
                    )
                }

                2 -> {
                    bottomSheet.setType("home")
                    bottomSheet.dismiss()
                    setWallpaperFromUrl(
                        context = this@SlideWallpaperActivity,
                        bitmap = bitmap,
                        target = WallpaperTarget.HOME
                    )

                }

                else -> {
                    bottomSheet.setType("both")
                    bottomSheet.dismiss()

                    setWallpaperFromUrl(
                        context = this@SlideWallpaperActivity,
                        bitmap = bitmap,
                        target = WallpaperTarget.BOTH
                    )
                }

            }


            if (isSuccess) {
                bottomSheet.showSuccess().also {
                    enableDismiss(bottomSheet)
                }
                viewModel.increaseSet(currentWallpaper)
            } else {
                bottomSheet.showFailure().also {
                    enableDismiss(bottomSheet)
                }
            }
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    }


    private fun setUpPhotoByCondition(imageUrl: List<ImageContent>) {
        println("setUpPhotoByCondition: ${imageUrl.size}")
        if (imageUrl.size == 1) {
            InterAds.preloadInterAds(
                this,
                alias = InterAds.ALIAS_INTER_DOWNLOAD,
                adUnit = InterAds.INTER_DOWNLOAD
            )
            val dialog = SetWallpaperDialog(this@SlideWallpaperActivity) { result ->
                settingOption = result
                checkPayBeforeNormalWallpaper {
                    val intent =
                        Intent(this@SlideWallpaperActivity, CropActivity::class.java).apply {
                            putExtra("imageUrl", imageUrl.first().url.full)
                        }
                    cropLauncher.launch(intent)
                }
            }
            dialog.show()
        } else {
            checkPayBeforeSpecialWallpaper {
                lifecycleScope.launch {
                    setUpLiveWallpaperByCondition(imageUrl)
                }
            }
        }
    }

    private suspend fun setUpLiveWallpaperByCondition(imageUrls: List<ImageContent>) {
        val bitmap = urlToBitmap(imageUrls.first().url.full) ?: return
        println("setUpLiveWallpaperByCondition: $bitmap")
        lifecycleScope.launch {
            startLiveWallpaper(imageUrls)
        }
    }

    suspend fun urlToBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val input = URL(imageUrl).openStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

    private fun downloadWallpaper() {

        val missingPermissions = RingtoneHelper.getMissingPhotoPermissions(this)

        if (missingPermissions.isEmpty()) {
            // All permissions granted
            actuallyDownloadWallpaper()
        } else {
            // Request the missing permissions using launcher
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }

        RingtoneHelper.getMissingPhotoPermissions(this@SlideWallpaperActivity)

    }

    private fun actuallyDownloadWallpaper(isBackground: Boolean = false) {
        val bottomSheet = DownloadWallpaperBottomSheet(this).apply {
            setType("download")
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnShowListener { dialog ->
                val b = (dialog as BottomSheetDialog).behavior
                b.isDraggable = false
                b.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        if (!isBackground) {
            bottomSheet.show()
        }

        val urls = currentWallpaper.contents.map { it.url.full }

        lifecycleScope.launch {
            val uris: List<Uri?> = withContext(Dispatchers.IO) {
                coroutineScope {
                    urls.map { url ->
                        async {
                            RingtoneHelper.downloadImage(this@SlideWallpaperActivity, url)
                        }
                    }.awaitAll()
                }
            }

            withContext(Dispatchers.Main) {
                if (uris.all { it != null }) {
                    downloadedUri = uris.first() // or handle multiple URIs as needed
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        playSlideWallpaperAdapter.submitList(allRingtones)

        carousel = Carousel(this, binding.horizontalWallpapers, playSlideWallpaperAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        snapHelper.attachToRecyclerView(binding.horizontalWallpapers)

        binding.apply {
            horizontalWallpapers.adapter = playSlideWallpaperAdapter
            horizontalWallpapers.initialPosition = index

            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    updateIndex(position, "onPositionChange")
                    // 🔁 force rebind to update playingHolder
                }

                override fun onScroll(dx: Int, dy: Int) {
                    lastDx = dx // ⬅️ Save dx for later use
                    Log.d("PlayerActivity", "Scrolling... dx = $dx")
                }
            })

            horizontalWallpapers.setOnTouchListener { _, event ->
                carousel.scrollSpeed(300f)
                duration = event.eventTime - event.downTime
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Log.d("PlayerActivity", "Touch DOWN")
                    }

                    MotionEvent.ACTION_MOVE -> {
                        Log.d("PlayerActivity", "Touch MOVE")
                    }

                    MotionEvent.ACTION_UP -> {
                        if (duration > 100) {
                            println("horizontalRingtones: $duration and $index")
                            binding.horizontalWallpapers.stopScroll()
                            setUpNewPlayer(index)
                            return@setOnTouchListener false
                        }

                        // 👇 Decide scroll direction (and clamp to ±1)
                        val newIndex = when {
                            lastDx > 0 && index < allRingtones.size - 1 -> index + 1
                            lastDx < 0 && index > 0 -> index - 1
                            else -> index
                        }

                        // 👇 Update only if actual index changes
                        if (newIndex != index) {
                            updateIndex(newIndex, "onPositionChange")
                            handler.postDelayed({
                                setUpNewPlayer(index)
                            }, 300)
                        } else {
                            // stay on current
                            setUpNewPlayer(index)
                        }

                        Log.d("PlayerActivity", "Touch UP - Scrolled to index=$index (dx=$lastDx)")
                    }
                }
                false
            }

            var previousIndex = index

            horizontalWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val view = snapHelper.findSnapView(layoutManager) ?: return
                    val newIndex = layoutManager.getPosition(view)

                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            previousIndex = index
                            println("🎯 Drag started at index $previousIndex")
                        }

                        RecyclerView.SCROLL_STATE_IDLE -> {

                            val distanceJumped = abs(newIndex - previousIndex)
                            println("🟨 Scroll ended. Jumped: $distanceJumped (from $previousIndex to $newIndex)")

                            if (distanceJumped >= 2) {
                                Log.d("PlayerActivity", "🛑 Too fast! Jumped $distanceJumped items")
                                recyclerView.stopScroll()
                            }

                            index = newIndex
                            setUpNewPlayer(index)
                        }
                    }
                }
            })
        }

    }

    private fun setUpNewPlayer(position: Int) {
        binding.horizontalWallpapers.smoothScrollToPosition(position)
        currentWallpaper = allRingtones[position]
        println("setUpNewPlayer: $position and $currentWallpaper")

        playSlideWallpaperAdapter.setCurrentPlayingPosition(position)
        viewModel.loadWallpaperById(currentWallpaper.id)
        viewModel.loadSlideWallpaperById(currentWallpaper.id)
    }

    private val snapHelper: OneItemSnapHelper by lazy {
        OneItemSnapHelper()
    }

    private var isFavorite: Boolean = false  // ← track this in activity

    private fun observeRingtoneFromDb() {
        viewModel.wallpaper.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentWallpaper.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite
                else R.drawable.icon_unfavourite
            )
        }

        viewModel.slideWallpaper.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentWallpaper.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite
                else R.drawable.icon_unfavourite
            )
        }
    }

    private fun displayFavouriteIcon(isManualChange: Boolean = false) {
        if (isFavorite) {
            if (isManualChange) {
                if (currentWallpaper.contents.size > 1) {
                    viewModel.deleteSlideWallpaper(currentWallpaper)
                } else {
                    viewModel.deleteWallpaper(currentWallpaper)
                }
                binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                isFavorite = false
            }
        } else {
            if (isManualChange) {
                if (currentWallpaper.contents.size > 1) {
                    viewModel.insertSlideWallpaper(currentWallpaper)
                } else {
                    viewModel.insertWallpaper(currentWallpaper)
                }
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

            handler = Handler(Looper.getMainLooper())
            binding.apply {
                index = allRingtones.indexOf(currentWallpaper)
                observeRingtoneFromDb()
                backBtn.setOnClickListener {
                    SearchRingtoneActivity.backToScreen(
                        this@SlideWallpaperActivity,
                        "INTER_WALLPAPER"
                    )
                }
                println("onCreate: $index")
                viewModel.loadWallpaperById(currentWallpaper.id)
                viewModel.loadSlideWallpaperById(currentWallpaper.id)
                observeRingtoneFromDb()

                favourite.setOnClickListener {
                    displayFavouriteIcon(true)
                }

                initViewPager()
                setUpNewPlayer(index)
                setupButtons()
            }
            binding.noInternet.root.gone()
        }
    }

    companion object {
        var settingOption = 0
        var currentIndex = 0

        private val TAG = SlideWallpaperActivity::class.java.name
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("wallpaper_index", index)
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
        loadBanner(this, BANNER_HOME)
        RewardAds.initRewardAds(this)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@SlideWallpaperActivity, "INTER_WALLPAPER")

    }
}