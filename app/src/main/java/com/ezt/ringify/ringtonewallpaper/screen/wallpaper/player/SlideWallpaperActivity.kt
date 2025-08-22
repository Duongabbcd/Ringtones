package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.R.attr.type
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
import androidx.core.view.isVisible
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
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.reward.RewardBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.OneItemSnapHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper.setWallpaperFromUrl
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.WallpaperTarget
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.DownloadWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.crop.CropActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog.AlarmDialog
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

@AndroidEntryPoint
class SlideWallpaperActivity :
    BaseActivity<ActivitySlideWallpaperBinding>(ActivitySlideWallpaperBinding::inflate) {
    private val favouriteViewModel: FavouriteWallpaperViewModel by viewModels()
    private val wallpaperViewModel: WallpaperViewModel by viewModels()

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

    private var imageWallpaperIndex = 0

    private var currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper

    private val allWallpapers by lazy {
        RingtonePlayerRemote.allSelectedWallpapers
    }

    private var lastDx: Int = 0
    private var duration = 0L
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var downloadedUri: Uri? = null

    private var isUserTouch = false

    private val selectedType by lazy {
        intent.getIntExtra("type", -1)
    }

    private val wallpaperCategoryId by lazy {
        intent.getIntExtra("wallpaperCategoryId", -1)
    }
    private var returnedFromSettings = false

    private var isLoadingMore = false
    private val addedWallpaperIds = mutableSetOf<Int>() // Track already added

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this, BANNER_HOME)
        
        checkDownloadPermissions()
        connectionViewModel.isConnectedLiveData.observe(this@SlideWallpaperActivity) { isConnected ->
            Log.d(TAG, "isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }

        Log.d("PreviewLive", "savedInstanceState: $type and $wallpaperCategoryId")
        if (savedInstanceState != null) {
            imageWallpaperIndex = savedInstanceState.getInt("wallpaper_index", 0)
        } else {
            currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper
            imageWallpaperIndex = allWallpapers.indexOf(currentWallpaper).takeIf { it >= 0 } ?: 0

            binding.horizontalWallpapers.post {
                val layoutManager = binding.horizontalWallpapers.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    binding.horizontalWallpapers.scrollToPosition(imageWallpaperIndex)
                }
            }
        }

        addedWallpaperIds.addAll(allWallpapers.map { it.id })

        wallpaperViewModel.trendingWallpaper.observe(this) { items ->
            appendNewRingtones(items)
        }
        wallpaperViewModel.newWallpaper.observe(this) { items ->
            appendNewRingtones(items)
        }
        wallpaperViewModel.subWallpaper1.observe(this) { items ->
            appendNewRingtones(items)
        }

    }

    private fun appendNewRingtones(newItems: List<Wallpaper>) {
        val oldSize = allWallpapers.size
        Log.d(TAG, "appendNewRingtones 0: ${newItems.size}")
        val distinctItems = newItems.filter { it.id !in addedWallpaperIds }

        if (distinctItems.isNotEmpty()) {
            allWallpapers.addAll(distinctItems)
            distinctItems.forEach { addedWallpaperIds.add(it.id) }
            Log.d(TAG, "appendNewRingtones 1: ${allWallpapers.size}")
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
            println("selectedType is: $type")
            alarm.isVisible = currentWallpaper.contents.size > 1
            alarm.setOnClickListener {
                val dialog = AlarmDialog(this@SlideWallpaperActivity) { totalTime ->
                    SlideshowWallpaperService.setupSlideShowInterval = totalTime
                    playSlideWallpaperAdapter.setSlideshowInterval(totalTime)
                }
                dialog.show()
            }

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
                if (imageUrl.size == 1) {
                    checkPayBeforeNormalWallpaper {
                        val dialog = SetWallpaperDialog(this@SlideWallpaperActivity) { result ->
                            settingOption = result
                            checkPayBeforeNormalWallpaper {
                                val intent =
                                    Intent(
                                        this@SlideWallpaperActivity,
                                        CropActivity::class.java
                                    ).apply {
                                        putExtra("imageUrl", imageUrl.first().url.full)
                                    }
                                cropLauncher.launch(intent)
                            }
                        }
                        dialog.show()
                    }

                } else {
                    checkPayBeforeSpecialWallpaper {
                        println("checkPayBeforeSpecialWallpaper 0")
                        lifecycleScope.launch {
                            setUpLiveWallpaperByCondition(imageUrl)
                        }
                    }
                }
            }

        }
    }

    private fun checkPayBeforeSpecialWallpaper(onClickListener: () -> Unit) {
        val listName = mutableListOf<Int>()
        val origin = Common.getAllFreeWallpapers(this@SlideWallpaperActivity)
        listName.addAll(origin)
        if (RemoteConfig.INTER_WALLPAPER == "0") {
            println("checkPayBeforeSpecialWallpaper 1")
            onClickListener()
            return
        }
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
            Log.d(TAG, "cropLauncher: $uri")
            if (uri != null) {
                val bitmap = loadBitmapFromUri(this, uri)
                if (bitmap != null) {
                    continueAfterCrop(bitmap)
                }
            }
        }
    }

    private fun continueAfterCrop(bitmap: Bitmap) {
        Log.d(TAG, "continueAfterCrop: $bitmap and $settingOption")
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
                favouriteViewModel.increaseSet(currentWallpaper)
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


    private suspend fun setUpLiveWallpaperByCondition(imageUrls: List<ImageContent>) {
        val bitmap = urlToBitmap(imageUrls.first().url.full) ?: return
        Log.d(TAG, "setUpLiveWallpaperByCondition: $bitmap")
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
        println("startLiveWallpaper is here")
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@SlideWallpaperActivity, SlideshowWallpaperService::class.java)
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
                    favouriteViewModel.increaseDownload(currentWallpaper)
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
        playSlideWallpaperAdapter.submitList(allWallpapers)
        playSlideWallpaperAdapter.setCurrentPlayingPosition(imageWallpaperIndex)

        carousel = Carousel(this, binding.horizontalWallpapers, playSlideWallpaperAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        binding.horizontalWallpapers.adapter = playSlideWallpaperAdapter
//        snapHelper.attachToRecyclerView(binding.horizontalWallpapers)

        // Scroll to current index after layout
        binding.horizontalWallpapers.post {
            centerItem(imageWallpaperIndex)
            println("setUpNewPlayer 3")
            setUpNewPlayer(imageWallpaperIndex)
        }

        carousel.addCarouselListener(object : CarouselListener {
            override fun onPositionChange(position: Int) {
                updateIndex(imageWallpaperIndex, "onPositionChange")
                setUpNewPlayer(position)
                playSlideWallpaperAdapter.setCurrentPlayingPosition(position, false)
                // 🔁 force rebind to update playingHolder
            }

            override fun onScroll(dx: Int, dy: Int) {
                lastDx = dx // ⬅️ Save dx for later use
                Log.d("PlayerActivity", "Scrolling... dx = $dx")
            }
        })

        binding.horizontalWallpapers.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //do nothing
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    println("setUpNewPlayer 4")
                    val newIndex = when {
                        lastDx > 0 && imageWallpaperIndex < allWallpapers.size - 1 -> imageWallpaperIndex + 1
                        lastDx < 0 && imageWallpaperIndex > 0 -> imageWallpaperIndex - 1
                        else -> imageWallpaperIndex
                    }
                    updateIndexBySnap(newIndex)
                }
            }
            false
        }

        // Listen for scroll idle to update index
        binding.horizontalWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    println("setUpNewPlayer 5")
                    updateIndexBySnap(imageWallpaperIndex)
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    //do nothing
                }
            }
        })
    }

    // Helper function to center a given item
    private fun centerItem(position: Int) {
        val layoutManager =
            binding.horizontalWallpapers.layoutManager as? LinearLayoutManager ?: return
        val child =
            binding.horizontalWallpapers.findViewHolderForAdapterPosition(position)?.itemView

        if (child == null) {
            layoutManager.scrollToPositionWithOffset(position, 0) // first bring it into view
            binding.horizontalWallpapers.post { centerItem(position) } // re-center after layout
            return
        }

        val itemWidth = child.width
        val recyclerWidth = binding.horizontalWallpapers.width
        val offset = (recyclerWidth - itemWidth) / 2

        layoutManager.scrollToPositionWithOffset(position, offset)
    }

    private fun updateIndexBySnap(snapIndex: Int) {
        val layoutManager =
            binding.horizontalWallpapers.layoutManager as? LinearLayoutManager ?: return
        val snapView = snapHelper.findSnapView(layoutManager) ?: return
        val snappedPosition = layoutManager.getPosition(snapView)
        updateIndex(snapIndex, "SNAP")
        println("setUpNewPlayer 6")
        setUpNewPlayer(snappedPosition)
        playSlideWallpaperAdapter.setCurrentPlayingPosition(snappedPosition)
    }

    private fun setUpNewPlayer(position: Int) {
        binding.horizontalWallpapers.smoothScrollToPosition(position)

        currentWallpaper = allWallpapers[position]
        Log.d(TAG, "setUpNewPlayer: position= $position wallpaper= $currentWallpaper")
        favouriteViewModel.loadLiveWallpaperById(currentWallpaper.id)
        imageWallpaperIndex = position
    }

    private val snapHelper: OneItemSnapHelper by lazy {
        OneItemSnapHelper()
    }

    private var isFavorite: Boolean = false  // ← track this in activity

    private fun observeRingtoneFromDb() {
        favouriteViewModel.wallpaper.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentWallpaper.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite
                else R.drawable.icon_unfavourite
            )
        }

        favouriteViewModel.slideWallpaper.observe(this) { dbRingtone ->
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
                    favouriteViewModel.deleteSlideWallpaper(currentWallpaper)
                } else {
                    favouriteViewModel.deleteWallpaper(currentWallpaper)
                }
                binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                isFavorite = false
            }
        } else {
            if (isManualChange) {
                if (currentWallpaper.contents.size > 1) {
                    favouriteViewModel.insertSlideWallpaper(currentWallpaper)
                } else {
                    favouriteViewModel.insertWallpaper(currentWallpaper)
                }
                binding.favourite.setImageResource(R.drawable.icon_favourite)
                isFavorite = true
            }
        }
    }

    private fun updateIndex(newIndex: Int, caller: String) {
        Log.d(
            TAG,
            "Index changed from $imageWallpaperIndex to $newIndex by $caller"
        )
        imageWallpaperIndex = newIndex
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()

            handler = Handler(Looper.getMainLooper())
            binding.apply {
                imageWallpaperIndex = allWallpapers.indexOf(currentWallpaper)
                observeRingtoneFromDb()
                backBtn.setOnClickListener {
                    SearchRingtoneActivity.backToScreen(
                        this@SlideWallpaperActivity,
                        "INTER_WALLPAPER"
                    )
                }
                Log.d(TAG, "onCreate: $imageWallpaperIndex and $selectedType")
                favouriteViewModel.loadWallpaperById(currentWallpaper.id)
                favouriteViewModel.loadSlideWallpaperById(currentWallpaper.id)
                observeRingtoneFromDb()

                favourite.setOnClickListener {
                    displayFavouriteIcon(true)
                }
                displayItems()
                loadMoreData()

                initViewPager()
                setUpNewPlayer(imageWallpaperIndex)
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

                if (isAtEnd && !isLoadingMore) {
                    isLoadingMore = true
                    when (wallpaperCategoryId) {
                        -2 -> {
                            wallpaperViewModel.loadTrendingWallpapers()
                        }

                        -1 -> {
                            wallpaperViewModel.loadNewWallpapers()
                        }

                        else -> {
                            wallpaperViewModel.loadSubWallpapers1(wallpaperCategoryId)
                        }
                    }
                }
            }
        })
    }

    private fun displayItems() {
        when (wallpaperCategoryId) {
            -2 -> {
                wallpaperViewModel.loadTrendingWallpapers()
            }

            -1 -> {
                wallpaperViewModel.loadNewWallpapers()
            }

            else -> {
                wallpaperViewModel.loadSubWallpapers1(wallpaperCategoryId)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("wallpaper_index", imageWallpaperIndex)
    }

    override fun onResume() {
        super.onResume()
        RewardAds.initRewardAds(this)
        binding.horizontalWallpapers.post {
            centerItem(imageWallpaperIndex)
            playSlideWallpaperAdapter.setCurrentPlayingPosition(imageWallpaperIndex)
            setUpNewPlayer(imageWallpaperIndex)
        }
        binding.horizontalWallpapers.isEnabled = true
        binding.horizontalWallpapers.suppressLayout(false)
    }

    override fun onPause() {
        super.onPause()
        binding.horizontalWallpapers.suppressLayout(true)
        binding.horizontalWallpapers.isEnabled = false
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@SlideWallpaperActivity, "INTER_WALLPAPER")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        binding.horizontalWallpapers.isEnabled = hasFocus
    }


    companion object {
        var settingOption = 0
        private const val TAG = "SlideWallpaperActivity"
    }
}