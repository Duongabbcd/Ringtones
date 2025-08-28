package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live

import alirezat775.lib.carouselview.Carousel
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
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.RewardAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewLiveWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.reward.RewardBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.OneItemSnapHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneHelper
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.bottomsheet.DownloadWallpaperBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.service.LiveVideoWallpaperService
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

    private val selectedType by lazy {
        intent.getIntExtra("type", -1)
    }
    private val tagId by lazy {
        intent.getIntExtra("tagId", -1)
    }

    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val playSlideWallpaperAdapter: PlayLiveWallpaperAdapter by lazy {
        PlayLiveWallpaperAdapter(this)
    }

    private lateinit var carousel: Carousel
    private var currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper

    private val allWallpapers by lazy {
        RingtonePlayerRemote.allSelectedWallpapers
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var downloadedUri: Uri? = null

    private var duration = 0L
    private var isOpenActivity = false
    private lateinit var handler: Handler
    private var isFromLiveService = false

    private val snapHelper: OneItemSnapHelper by lazy {
        OneItemSnapHelper()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this, BANNER_HOME)
        handler = Handler(Looper.getMainLooper())
        checkDownloadPermissions()
        Log.d(TAG, "savedInstanceState: ${savedInstanceState == null} and $selectedType and $tagId")
        if (savedInstanceState != null) {
            liveWallpaperIndex = savedInstanceState.getInt("current_index", 0)
            Log.d(TAG, "savedInstanceState 0: $liveWallpaperIndex")
        } else {
            Log.d(TAG, "savedInstanceState 1: ${RingtonePlayerRemote.currentPlayingWallpaper}")
            isOpenActivity = true
            currentWallpaper = RingtonePlayerRemote.currentPlayingWallpaper
            liveWallpaperIndex = allWallpapers.indexOf(currentWallpaper).takeIf { it >= 0 } ?: 0
            favouriteViewModel.loadLiveAllWallpapers()
            setUpNewPlayer(liveWallpaperIndex)
        }

        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@PreviewLiveWallpaperActivity,
                    "INTER_WALLPAPER"
                )
            }
            favourite.setOnClickListener { displayFavouriteIcon(true) }
            loadMoreData()
            initViewPager()
            setupButtons()
        }

        addedWallpaperIds.addAll(allWallpapers.map { it.id })
        connectionViewModel.isConnectedLiveData.observe(this@PreviewLiveWallpaperActivity) { isConnected ->
            Log.d(TAG, "isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }
        when (selectedType) {
            -10 -> wallpaperViewModel.searchWallpapers3.observe(this@PreviewLiveWallpaperActivity) { items ->
                appendNewRingtones(items)
            }

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

        // initial centering (only once at startup)
        binding.horizontalWallpapers.post {
            centerItem(liveWallpaperIndex)
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
                actuallyDownloadVideoWallpaper()
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
                if (currentWallpaper.contents.isEmpty()) {
                    return@setOnClickListener
                }
                checkPayBeforeUsingVideoWallpaper {
                    val videoUrl = currentWallpaper.contents.first().url.full
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Check out this video")
                        putExtra(Intent.EXTRA_TEXT, videoUrl)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Share video via"))
                }
            }

            download.setOnClickListener {
                if (currentWallpaper.contents.isEmpty()) {
                    return@setOnClickListener
                }
                checkPayBeforeUsingVideoWallpaper {
                    downloadVideoWallpaper()
                }
            }

            wallpaper.setOnClickListener {
                if (currentWallpaper.contents.isEmpty()) {
                    return@setOnClickListener
                }
                checkPayBeforeUsingVideoWallpaper {
                    val videoUrl = currentWallpaper.contents.first().url.full
                    setUpVideoByCondition(videoUrl)
                }
            }
        }
    }

    private fun checkPayBeforeUsingVideoWallpaper(onClickListener: () -> Unit) {
        val listName = mutableListOf<Int>()
        val origin = Common.getAllFreeWallpapers(this@PreviewLiveWallpaperActivity)
        listName.addAll(origin)
        if (!listName.contains(currentWallpaper.id) && RemoteConfig.INTER_WALLPAPER != "0") {
            val rewardBottomSheet = RewardBottomSheet(this@PreviewLiveWallpaperActivity) {
                RewardAds.showAds(
                    this@PreviewLiveWallpaperActivity,
                    object : RewardAds.RewardCallback {
                        override fun onAdShowed() {
                            Log.d(TAG, "onAdShowed")
                        }

                        override fun onAdDismiss() {
                            Log.d(TAG, "onAdDismiss")
                            if (listName.size > RemoteConfig.totalFreeRingtones.toInt()) {
                                listName.drop(0)
                            }
                            listName.add(currentWallpaper.id)
                            Common.setAllFreeWallpapers(this@PreviewLiveWallpaperActivity, listName)
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

    private fun setUpVideoByCondition(videoUrl: String) {
        launchLiveWallpaper(this, videoUrl)
    }

    fun launchLiveWallpaper(context: Context, videoUrl: String) {
        // Save the video URL to SharedPreferences
        val prefs = context.getSharedPreferences("video_wallpaper", MODE_PRIVATE)
        prefs.edit().putString("video_url", videoUrl).apply()
        Log.d("LivePreview", "Saved wallpaper URL: $videoUrl")

        // Force user to re-apply the same live wallpaper
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, LiveVideoWallpaperService::class.java)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        isFromLiveService = false
        context.startActivity(intent)
    }

    private fun downloadVideoWallpaper() {
        val missingPermissions = RingtoneHelper.getMissingPhotoPermissions(this)
        if (missingPermissions.isEmpty()) {
            actuallyDownloadVideoWallpaper()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun actuallyDownloadVideoWallpaper(isBackground: Boolean = false) {
        val bottomSheet = DownloadWallpaperBottomSheet(this).apply {
            setType()
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
        // setup adapter
        playSlideWallpaperAdapter.submitList(allWallpapers)
        playSlideWallpaperAdapter.setCurrentPlayingPosition(liveWallpaperIndex)
        // create carousel
        carousel = Carousel(this, binding.horizontalWallpapers, playSlideWallpaperAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        binding.horizontalWallpapers.adapter = playSlideWallpaperAdapter


        binding.horizontalWallpapers.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //do nothing
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d(TAG, "setUpNewPlayer 4")
                    val touchedChild =
                        binding.horizontalWallpapers.findChildViewUnder(event.x, event.y)
                    if (touchedChild != null) {
                        val position =
                            binding.horizontalWallpapers.getChildAdapterPosition(touchedChild)
                        if (position != RecyclerView.NO_POSITION) {
                            Log.d(TAG, "Touch UP - Tapped on position=$position")
                            updateIndex(position, "onTouch")
                        }
                    } else {
                        Log.d(TAG, "🚫 Touch UP - No valid item under touch")
                    }
                }
            }
            false
        }

        snapHelper.attachToRecyclerView(binding.horizontalWallpapers)
        // 🔑 Listen for swipe + snapping
        binding.horizontalWallpapers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val snapView = snapHelper.findSnapView(lm)
                    snapView?.let {
                        val newPos = lm.getPosition(it)
                        if (newPos != RecyclerView.NO_POSITION && newPos != liveWallpaperIndex) {
                            liveWallpaperIndex = newPos
                            setUpNewPlayer(newPos, "onScrollStateChanged")
                            playSlideWallpaperAdapter.setCurrentPlayingPosition(newPos)
                        }
                    }
                }
            }

        })

    }


    // Helper function to center item (used at startup or programmatic jumps)
    private fun centerItem(position: Int) {
        val rv = binding.horizontalWallpapers
        val lm = rv.layoutManager as? LinearLayoutManager
        println("centerItem 0: $lm")
        if (lm == null) {
            return
        }

        val rvCenter = rv.paddingLeft + (rv.width - rv.paddingLeft - rv.paddingRight) / 2

        val child = lm.findViewByPosition(position)

        if (child == null) {
            // Step 1: Bring it into view without offset
            lm.scrollToPositionWithOffset(position, 0)

            // Step 2: Re-center *after* layout has happened
            rv.post { centerItem(position) }
            return
        }

        // Step 3: Calculate decorated width and center properly
        val params = child.layoutParams as RecyclerView.LayoutParams
        val childWidth =
            lm.getDecoratedMeasuredWidth(child) + params.leftMargin + params.rightMargin
        val childCenter = lm.getDecoratedLeft(child) + childWidth / 2

        val offset = rvCenter - childCenter
        println("centerItem 1: $child and $offset")
        if (position in listOf(0, 1)) {
            binding.horizontalWallpapers.smoothScrollToPosition(position)
        } else {
            lm.scrollToPositionWithOffset(position, offset)
        }

    }


    private fun setUpNewPlayer(position: Int, tag: String = "") {
        binding.horizontalWallpapers.smoothScrollToPosition(position)
            currentWallpaper = allWallpapers[position]
            RingtonePlayerRemote.currentPlayingWallpaper = currentWallpaper
            Log.d(
                TAG,
                "setUpNewPlayer: position= $position wallpaper= ${currentWallpaper.id} and $tag"
            )
            favouriteViewModel.loadLiveWallpaperById(currentWallpaper.id)
            observeRingtoneFromDb()
        liveWallpaperIndex = position

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
        Log.d(TAG, "Index changed from $liveWallpaperIndex to $newIndex by $caller")
        liveWallpaperIndex = newIndex
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
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

                if (isAtEnd && !isLoadingMore && selectedType != 1) {
                    isLoadingMore = true
                    when (selectedType) {
                        -10 -> wallpaperViewModel.searchVideoWallpaperByTag(tagId = tagId)
                        2 -> wallpaperViewModel.loadLiveWallpapers()

                        4 -> wallpaperViewModel.loadPremiumVideoWallpaper()

                        else -> wallpaperViewModel.loadLiveWallpapers()
                    }

                }
            }
        })
    }

    @OptIn(UnstableApi::class)
    override fun onStop() {
        super.onStop()
        PlayerManager.release()
        CacheUtil.release(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_index", liveWallpaperIndex)
        Log.d(TAG, "Saving currentIndex: $liveWallpaperIndex")
    }

    override fun onResume() {
        super.onResume()
        RewardAds.initRewardAds(this)
        binding.horizontalWallpapers.post {
            playSlideWallpaperAdapter.setCurrentPlayingPosition(liveWallpaperIndex)
            setUpNewPlayer(liveWallpaperIndex)
        }
        binding.horizontalWallpapers.isEnabled = true
        binding.horizontalWallpapers.suppressLayout(false)

        if (isFromLiveService) {
            isFromLiveService = false
            if (isMyLiveWallpaperRunning()) {
                Log.d(TAG, "My live wallpaper is currently active.")
                try {
                } catch (e: Exception) {
                    Log.e(TAG, "Toast failed: ${e.message}")
                }
            } else {
                Log.d(TAG, "My live wallpaper is NOT active.")
            }
        }
    }

    private fun isMyLiveWallpaperRunning(): Boolean {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val info = wallpaperManager.wallpaperInfo

        return info?.packageName == packageName &&
                info.serviceName == LiveVideoWallpaperService::class.java.name
    }

    override fun onPause() {
        super.onPause()
        binding.horizontalWallpapers.suppressLayout(true)
        binding.horizontalWallpapers.isEnabled = false
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@PreviewLiveWallpaperActivity, "INTER_WALLPAPER")

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        binding.horizontalWallpapers.isEnabled = hasFocus
    }

    companion object {
        private val TAG = PreviewLiveWallpaperActivity::class.java.simpleName
        private var liveWallpaperIndex = 0

    }
}

