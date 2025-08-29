package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.RewardAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityRingtoneBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.firebase.AnalyticsLogger
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity.Companion.loadBanner
import com.ezt.ringify.ringtonewallpaper.screen.reward.RewardBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.adapter.PlayRingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.bottomsheet.DownloadRingtoneBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.FeedbackDialog
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote.currentPlayingRingtone
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class RingtoneActivity : BaseActivity<ActivityRingtoneBinding>(ActivityRingtoneBinding::inflate) {
    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private val favouriteRingtoneViewModel: FavouriteRingtoneViewModel by viewModels()
    private var downloadedUri: Uri? = null

    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private var isPlayerReleased = false
    private lateinit var handler: Handler
    private lateinit var carousel: Carousel

    private var shouldAutoPlay = false
    private val playRingtoneAdapter: PlayRingtoneAdapter by lazy {
        PlayRingtoneAdapter(onRequestScrollToPosition = { newPosition ->
            println("PlayRingtoneAdapter 0")
            carousel.scrollSpeed(200f)
            safeSetUpNewPlayer(newPosition, true)
        }, onClickListener = { result, id ->
            println("PlayRingtoneAdapter 1: $result and $id and $currentId")
            println("PlayRingtoneAdapter 2: ${exoPlayer.currentPosition} and ${exoPlayer.duration}")
            shouldAutoPlay = result
            if (result) {
                if (id != currentId) {
                    currentId = id
                    shouldAutoPlay = true // ✅ Now it will auto play once ready
                    safeSetUpNewPlayer(index, true)
                } else {

                    if (exoPlayer.playbackState == Player.STATE_READY) {
                        println("PlayRingtoneAdapter 4: ${exoPlayer.playbackState} and $index and $currentId")
                        tryAutoPlayFromReady()
                    } else {
                        println("PlayRingtoneAdapter 5: ${exoPlayer.playbackState} and $index and $currentId")
                        shouldAutoPlay = true
                    }
                }
            } else {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                }

            }
        }, onCurrentIdChanged = { id ->
            currentId = id  // sync currentId immediately on position change
        })
    }

    private fun tryAutoPlayFromReady() {
        if (shouldAutoPlay) {
            println("▶️ Playing from tryAutoPlayFromReady: ${exoPlayer.currentPosition} and ${exoPlayer.duration}")
            if (exoPlayer.duration != C.TIME_UNSET && exoPlayer.currentPosition >= exoPlayer.duration) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
            shouldAutoPlay = false
        } else {
            println("🚫 Skipped auto-play in tryAutoPlayFromReady: shouldAutoPlay = false")
        }
    }

    private lateinit var sortOrder: String
    private val ringtoneViewModel: RingtoneViewModel by viewModels()

    private var isLoadingMore = false
    private val addedRingtoneIds = mutableSetOf<Int>() // Track already added

    private var currentId = -10
    private var isPlaying = false

    private var index = 0

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -100)
    }

    lateinit var exoPlayer: ExoPlayer
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var isNotification = false

    private var returnedFromSettings = false

    private val progressUpdater = object : Runnable {
        override fun run() {
            val exoPlayer = RingtonePlayerRemote.exoPlayer

            println("⏱ progressUpdater running...")

            if (exoPlayer.isPlaying) {
                val progress = exoPlayer.currentPosition.toFloat()
                println("⏳ Progress: $progress")
                playRingtoneAdapter.updateProgress(progress)
                handler.postDelayed(this, 1000)
            } else {
                println("⏸️ Not playing, stopping progress updates.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::exoPlayer.isInitialized) {
            RingtonePlayerRemote.release()
            isPlayerReleased = true
        }
    }

    private var currentRingtone = currentPlayingRingtone

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedRingtones
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RingtonePlayerRemote.initialize(this)
        exoPlayer = RingtonePlayerRemote.exoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && exoPlayer.duration != C.TIME_UNSET) {
                    println("🟢 Player is ready, duration: $shouldAutoPlay ${exoPlayer.duration}")
                    if (shouldAutoPlay) {
                        tryAutoPlayFromReady()
                        shouldAutoPlay = false
                    }
                } else if (state == Player.STATE_ENDED) {
                    println("🎵 Song ended")
                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = true
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                println("onIsPlayingChanged: isPlaying = $isPlaying and $index")
                playRingtoneAdapter.setCurrentPlayingPosition(index, isPlaying)
                if (isPlaying) {
                    handler.post(progressUpdater)
                } else {
                    handler.removeCallbacks(progressUpdater)
                    playRingtoneAdapter.updateProgress(exoPlayer.currentPosition.toFloat())
                }
            }
        })



        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this)
        RewardAds.initRewardAds(this)
        handler = Handler(Looper.getMainLooper())
        Log.d(TAG, "categoryId: $categoryId")
        sortOrder = Common.getSortOrder(this)
        checkDownloadPermissions()

        connectionViewModel.isConnectedLiveData.observe(this@RingtoneActivity) { isConnected ->
            println("isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }

        when (categoryId) {
            -99 -> {
                favouriteRingtoneViewModel.allRingtones.observe(this@RingtoneActivity) { items ->
                    appendNewRingtones(items)
                }

            }

            -100 -> {
                ringtoneViewModel.popular.observe(this@RingtoneActivity) { items ->
                    appendNewRingtones(items)
                }
            }

            else -> {
                ringtoneViewModel.selectedRingtone.observe(this@RingtoneActivity) { items ->
                    appendNewRingtones(items)
                }
            }
        }

        binding.apply {
            currentRingtoneName.isSelected = true
            backBtn.setOnClickListener {
                finish()
            }
            index = allRingtones.indexOf(currentRingtone)
            addedRingtoneIds.addAll(allRingtones.map { it.id })
            initViewPager()
            binding.horizontalRingtones.post {
                (binding.horizontalRingtones.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(index, 0)
                safeSetUpNewPlayer(index)
            }

            favourite.setOnClickListener {
                displayFavouriteIcon(true)
            }


            feedback.setOnClickListener {
                val dialog = FeedbackDialog(this@RingtoneActivity)
                dialog.setRingtoneFeedback(currentRingtone)
                dialog.show()
            }
            setupButtons()
        }
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            displayItems()
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }

    private fun displayItems() {
        if (RingtonePlayerRemote.allSelectedRingtones.size <= 1) {
            return
        }
        when (categoryId) {
            -100 -> {
                ringtoneViewModel.loadPopular(sortOrder)

            }

            -99 -> {
                favouriteRingtoneViewModel.loadAllRingtones()
            }

            else -> {
                ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
            }
        }
    }

    private fun appendNewRingtones(newItems: List<Ringtone>) {
        val oldSize = allRingtones.size
        println("appendNewRingtones 0: ${newItems.size}")
        val distinctItems = newItems.filter { it.id !in addedRingtoneIds }

        if (distinctItems.isNotEmpty()) {
            allRingtones.addAll(distinctItems)
            distinctItems.forEach { addedRingtoneIds.add(it.id) }
            println("appendNewRingtones 1: ${allRingtones.size}")
            playRingtoneAdapter.submitList(allRingtones.toList())
            playRingtoneAdapter.notifyItemRangeInserted(oldSize, distinctItems.size)
        }

        isLoadingMore = false
    }



    private fun loadMoreData() {
        binding.horizontalRingtones.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dx <= 0) return  // Only when scrolling right

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isAtEnd = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 2

                if (isAtEnd && !isLoadingMore) {
                    isLoadingMore = true
                    println("horizontalRingtones: $categoryId")
                    when (categoryId) {
                        -100 -> {
                            favouriteRingtoneViewModel.loadAllRingtones()
                        }

                        -99 -> {
                            ringtoneViewModel.loadPopular(sortOrder)

                        }

                        else -> {
                            ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                        }
                    }

                }
            }
        })
    }

    private fun checkDownloadPermissions() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                actuallyDownloadRingtone()
            } else {
                // Loop through each permission
                val permanentlyDenied = permissions.keys.any { permission ->
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }

                if (permanentlyDenied) {
                    // User denied and selected "Don't ask again"
                    showGoToSettingsDialog()
                } else {
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this@RingtoneActivity) { result ->
            if (result) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            download.setOnClickListener {
                checkPayBeforeUsingRingtone {
                    downloadRingtone()

                    if(returnedFromSettings) {
                        returnedFromSettings = false
                        RingtonePlayerRemote.setCurrentRingtone(currentRingtone)
                        startActivity(Intent(this@RingtoneActivity, RingtoneActivity::class.java))
                    }
                }
            }

            ringTone.setOnClickListener {
                checkPayBeforeUsingRingtone {
                    isNotification = false
                    setupRingtone(isNotification)
                }
            }

            notification.setOnClickListener {
                checkPayBeforeUsingRingtone {
                    isNotification = true
                    setupRingtone(isNotification)
                }
            }
        }
    }

    private fun checkPayBeforeUsingRingtone(onClickListener: () -> Unit) {
        val listName = mutableListOf<String>()
        val origin = Common.getAllFreeRingtones(this@RingtoneActivity)
        listName.addAll(origin)
        if (RemoteConfig.INTER_RINGTONE == "0") {
            onClickListener()
            return
        }
        if (!listName.contains(currentRingtone.name)) {
            val rewardBottomSheet = RewardBottomSheet(this@RingtoneActivity) {
                RewardAds.showAds(this@RingtoneActivity, object : RewardAds.RewardCallback {
                    override fun onAdShowed() {
                        Log.d(TAG, "onAdShowed")
                    }

                    override fun onAdDismiss() {
                        Log.d(TAG, "onAdDismiss")
                        if (listName.size > RemoteConfig.totalFreeRingtones.toInt()) {
                            listName.drop(0)
                        }
                        listName.add(currentRingtone.name)
                        Common.setAllFreeRingtones(this@RingtoneActivity, listName)

                        safeSetUpNewPlayer(index)
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

    private fun downloadRingtone() {

        val missingPermissions = RingtoneHelper.getMissingAudioPermissions(this)

        if (missingPermissions.isEmpty()) {
            // All permissions granted
            actuallyDownloadRingtone()
        } else {
            // Request the missing permissions using launcher
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }

        RingtoneHelper.getMissingAudioPermissions(this@RingtoneActivity)

    }

    private val snapHelper: OneItemSnapHelper by lazy {
        OneItemSnapHelper()
    }

    private fun actuallyDownloadRingtone(isBackground: Boolean = false) {

        val bottomSheet = DownloadRingtoneBottomSheet(this)
        bottomSheet.apply {
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


        val ringtoneUrl = currentRingtone.contents.url
        val ringtoneTitle = currentRingtone.name
        lifecycleScope.launch {
            val uri = RingtoneHelper.downloadRingtoneFile(
                this@RingtoneActivity,
                ringtoneUrl,
                ringtoneTitle
            ) { progress ->
                println("progress: $progress")
            }
            withContext(Dispatchers.Main) {
                if (uri != null) {
                    downloadedUri = uri
                    delay(5000L)
                    bottomSheet.showSuccess().also {
                        enableDismiss(bottomSheet)
                    }
                    favouriteRingtoneViewModel.increaseDownload(currentRingtone)
                } else {
                    bottomSheet.showFailure().also {
                        enableDismiss(bottomSheet)
                    }
                }
            }
        }
    }

    private fun enableDismiss(bottomSheet: DownloadRingtoneBottomSheet) {
        bottomSheet.apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
        }

        (bottomSheet as? BottomSheetDialog)?.behavior?.apply {
            isDraggable = true
            // Optional: let it collapse instead of staying expanded
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupRingtone(isNotification: Boolean = false) {
        this.isNotification = isNotification
        if (!RingtoneHelper.hasWriteSettingsPermission(this)) {
            // Ask the user to grant WRITE_SETTINGS
            returnedFromSettings = true
            RingtoneHelper.requestWriteSettingsPermission(this)
            return
        }

        // Now permission is granted—set the ringtone
        setRingtoneAfterPermission(isNotification)

    }

    private fun setRingtoneAfterPermission(isNotification: Boolean = false) {
        val ringtoneTitle = currentRingtone.name
        val ringtoneUrl = currentRingtone.contents.url
        CoroutineScope(Dispatchers.IO).launch {
            val uri = RingtoneHelper.downloadRingtoneFile(
                this@RingtoneActivity,
                ringtoneUrl,
                ringtoneTitle
            ) { progress ->
                println("progress: $progress")
            }
            downloadedUri = uri
            withContext(Dispatchers.Main) {
                saveRingtone(isNotification)
            }
        }


    }

    private fun saveRingtone(isNotification: Boolean) {
        val dialog = DownloadRingtoneBottomSheet(this, if(isNotification) "notification" else "ringtone")
        dialog.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnShowListener { dialog ->
                val b = (dialog as BottomSheetDialog).behavior
                b.isDraggable = false
                b.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        dialog.show()
        if (Settings.System.canWrite(this@RingtoneActivity)) {
            println("System can write: $downloadedUri")
            val success = downloadedUri?.let {
                RingtoneHelper.setAsSystemRingtone(this@RingtoneActivity, it, isNotification)
            } == true
            if (success) {
                handler.postDelayed(  {  dialog.showSuccess().also {
                    enableDismiss(dialog)
                }}, 5000L)

                favouriteRingtoneViewModel.increaseSet(currentRingtone)
            } else {
                dialog.showFailure().also {
                    enableDismiss(dialog)
                }
            }
        }
    }

    private var isFavorite: Boolean = false  // ← track this in activity

    private fun observeRingtoneFromDb() {
        println("observeRingtoneFromDb: ${currentRingtone.id}")
        favouriteRingtoneViewModel.ringtone.observe(this) { dbRingtone ->
            isFavorite = dbRingtone.id == currentRingtone.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite
                else R.drawable.icon_unfavourite
            )
        }
    }

    private fun displayFavouriteIcon(isManualChange: Boolean = false) {
        if (isFavorite) {
            if (isManualChange) {
                favouriteRingtoneViewModel.deleteRingtone(currentRingtone)
                binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                isFavorite = false
            }
        } else {
            if (isManualChange) {
                favouriteRingtoneViewModel.insertRingtone(currentRingtone)
                binding.favourite.setImageResource(R.drawable.icon_favourite)
                isFavorite = true
            }
        }
    }

    private var lastSetupTime = 0L
    private var lastSetupPosition = -1

    private fun safeSetUpNewPlayer(position: Int, autoplay: Boolean = false) {
        val now = System.currentTimeMillis()

        if (position == lastSetupPosition && now - lastSetupTime < 300) {
            Log.d(
                "RingtoneActivity",
                "⏳ Skipping duplicate setUpNewPlayer call at position=$position"
            )
            return
        }

        lastSetupTime = now
        lastSetupPosition = position

        setUpNewPlayer(position, autoplay)
    }


    private fun setUpNewPlayer(position: Int, playingSong: Boolean = false) {
        val exoPlayer = RingtonePlayerRemote.exoPlayer

        Log.d(TAG, "🔄 setUpNewPlayer: position=$position, shouldAutoPlay=$shouldAutoPlay")
        shouldAutoPlay = playingSong

        binding.horizontalRingtones.smoothScrollToPosition(position)
        val selectedRingtone = allRingtones[position]
        currentRingtone = selectedRingtone

        RingtonePlayerRemote.setCurrentRingtone(selectedRingtone)
        favouriteRingtoneViewModel.loadRingtoneById(currentRingtone.id)
        observeRingtoneFromDb()
        playRingtoneAdapter.setCurrentPlayingPosition(position, shouldAutoPlay)

        binding.currentRingtoneName.text = selectedRingtone.name
        binding.currentRingtoneAuthor.text =
            selectedRingtone.author?.name ?: getString(R.string.unknwon_author)

        handler.removeCallbacks(progressUpdater)

        val mediaItem = MediaItem.fromUri(selectedRingtone.contents.url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        playRingtoneAdapter.submitList(allRingtones)

        carousel = Carousel(this, binding.horizontalRingtones, playRingtoneAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        binding.horizontalRingtones.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect.set(0, 0, 0, 0)
            }
        })


        snapHelper.attachToRecyclerView(binding.horizontalRingtones)

        binding.apply {
            horizontalRingtones.initialPosition = index
            horizontalRingtones.adapter = playRingtoneAdapter

            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    currentId = -10
                    shouldAutoPlay = false
//                    updateIndex(index, "onPositionChange")
                    // 🔁 force rebind to update playingHolder
                }

                override fun onScroll(dx: Int, dy: Int) {
                    shouldAutoPlay = false
                }
            })

            horizontalRingtones.setOnTouchListener { _, event ->

                val childView = horizontalRingtones.findChildViewUnder(event.x, event.y)
                if (childView == null) {
                    // Handle touch on empty space
                    return@setOnTouchListener true // Consume the event
                }
                shouldAutoPlay = false
                carousel.scrollSpeed(300f)
                when (event.action) {
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {

                        val touchedChild = horizontalRingtones.findChildViewUnder(event.x, event.y)
                        if (touchedChild != null) {
                            val position = horizontalRingtones.getChildAdapterPosition(touchedChild)
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

            var previousIndex = index  // Track before scroll starts

            horizontalRingtones.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            Log.d(
                                TAG,
                                "🟨 Scroll ended. Jumped: $distanceJumped (from $previousIndex to $newIndex)"
                            )

                            if (distanceJumped >= 2) {
                                Log.d("PlayerActivity", "🛑 Too fast! Jumped $distanceJumped items")
                                recyclerView.stopScroll()
                            }

                            updateIndex(newIndex, "onPositionChange")
                            safeSetUpNewPlayer(newIndex, false)
                        }
                    }
                }
            })
        }


    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
    }

    // ✅ Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateIndex(newIndex: Int, caller: String) {
        Log.d(TAG, "Index changed from $index to $newIndex by $caller")
        index = newIndex
    }

    override fun onResume() {
        super.onResume()
        if(returnedFromSettings) {
            returnedFromSettings = false
            if (RingtoneHelper.hasWriteSettingsPermission(this)) {
                setRingtoneAfterPermission(isNotification)
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentId", currentId)
        outState.putBoolean("isPlaying", isPlaying)

        outState.putBoolean("isReturnFromSetting", returnedFromSettings)
        outState.putBoolean("isNotification", isNotification)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        returnedFromSettings =
            savedInstanceState.getBoolean("isReturnFromSetting", false)
        isNotification =
            savedInstanceState.getBoolean("isNotification", false)
    }

    companion object {
        private val TAG = RingtoneActivity::class.java.simpleName
    }
}

class OneItemSnapHelper(private val maxFlingDistance: Int = 1) : PagerSnapHelper() {

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val current = layoutManager.getPosition(snapView)

        return when {
            velocityX > 0 -> (current + 1).coerceAtMost(layoutManager.itemCount - 1)
            velocityX < 0 -> (current - 1).coerceAtLeast(0)
            else -> current
        }
    }
}
