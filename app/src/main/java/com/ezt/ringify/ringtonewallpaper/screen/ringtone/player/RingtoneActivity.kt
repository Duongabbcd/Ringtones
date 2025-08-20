package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player

import alirezat775.lib.carouselview.Carousel
import alirezat775.lib.carouselview.CarouselListener
import alirezat775.lib.carouselview.CarouselView
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@AndroidEntryPoint
class RingtoneActivity : BaseActivity<ActivityRingtoneBinding>(ActivityRingtoneBinding::inflate) {
    private val viewModel: FavouriteRingtoneViewModel by viewModels()
    private var downloadedUri: Uri? = null

    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private var isPlayerReleased = false
    private lateinit var handler: Handler
    private lateinit var carousel: Carousel
    private val playRingtoneAdapter: PlayRingtoneAdapter by lazy {
        PlayRingtoneAdapter(onRequestScrollToPosition = { newPosition ->
            carousel.scrollSpeed(200f)
            setUpNewPlayer(newPosition)
            handler.postDelayed({
                playRingtoneAdapter.setCurrentPlayingPosition(
                    newPosition,
                    false
                )
            }, 300)
        }
        ) { result, id ->
            if (result) {
                if (id != currentId) {
                    playRingtone(true)
                    currentId = id
                } else {
                    if (exoPlayer.currentPosition >= exoPlayer.duration) {
                        exoPlayer.seekTo(0)
                    }
                    exoPlayer.play()
                }
            } else {
                exoPlayer.pause()
            }
        }
    }

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

    private fun playRingtone(isPlaying: Boolean = false) {
        exoPlayer = ExoPlayer.Builder(this).build()
        // Prepare the media item
        if (!isPlaying) return
        val mediaItem = MediaItem.fromUri(currentRingtone.contents.url)
        exoPlayer.setMediaItem(mediaItem)
        // Prepare and start playback
        exoPlayer.prepare()
        exoPlayer.play()
        println("▶️ Starting playback & posting progressUpdater")
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                println("🎧 onIsPlayingChanged: $isPlaying")
                if (isPlaying) {
                    handler.post(progressUpdater)
                } else {
                    handler.removeCallbacks(progressUpdater)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    println("🎵 Song ended")
                    handler.postDelayed({ playRingtoneAdapter.onSongEnded() }, 300)

                }
            }
        })
    }

    private val progressUpdater = object : Runnable {
        override fun run() {
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

    private fun releasePlayer() {
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
            isPlayerReleased = true
        }
    }

    private fun releasePlayerAndResetUI() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        releasePlayer()
        playRingtoneAdapter.updateProgress(0f)
        playRingtoneAdapter.setCurrentPlayingPosition(index, false) // -1 means no item is playing
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
            isPlayerReleased = true
        }
    }



    private var currentRingtone = RingtonePlayerRemote.currentPlayingRingtone

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedRingtones
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (RemoteConfig.BANNER_ALL == "0") {
            binding.frBanner.root.gone()
        }

        loadBanner(this)
        RewardAds.initRewardAds(this)
        handler = Handler(Looper.getMainLooper())
        println("onCreate: $categoryId")
        sortOrder = Common.getSortOrder(this)
        checkDownloadPermissions()

        connectionViewModel.isConnectedLiveData.observe(this@RingtoneActivity) { isConnected ->
            println("isConnected: $isConnected")
            checkInternetConnected(isConnected)
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
                setUpNewPlayer(index)
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
            when (categoryId) {
                -100 -> {
                    favouriteViewModel.allRingtones.observe(this@RingtoneActivity) { items ->
                        appendNewRingtones(items)
                    }

                }

                -99 -> {
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

        }
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            viewModel.loadRingtoneById(currentRingtone.id)
            observeRingtoneFromDb()
            displayItems()
            playRingtone(false)
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }

    private fun displayItems() {
        when (categoryId) {
            -100 -> {
                ringtoneViewModel.loadPopular(sortOrder)

            }

            -99 -> {
                favouriteViewModel.loadAllRingtones()
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

    private lateinit var sortOrder: String
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val favouriteViewModel: FavouriteRingtoneViewModel by viewModels()

    private var isLoadingMore = false
    private val addedRingtoneIds = mutableSetOf<Int>() // Track already added

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
                    when (categoryId) {
                        -100 -> {
                            ringtoneViewModel.loadPopular(sortOrder)

                        }

                        -99 -> {
                            favouriteViewModel.loadAllRingtones()
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

                        setUpNewPlayer(index)
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
                    viewModel.increaseDownload(currentRingtone)
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

                viewModel.increaseSet(currentRingtone)
            } else {
                dialog.showFailure().also {
                    enableDismiss(dialog)
                }
            }
        }
    }

    private var isFavorite: Boolean = false  // ← track this in activity

    private fun observeRingtoneFromDb() {
        viewModel.ringtone.observe(this) { dbRingtone ->
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
                viewModel.deleteRingtone(currentRingtone)
                binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                isFavorite = false
            }
        } else {
            if (isManualChange) {
                viewModel.insertRingtone(currentRingtone)
                binding.favourite.setImageResource(R.drawable.icon_favourite)
                isFavorite = true
            }
        }
    }

    private fun setUpNewPlayer(position: Int) {
        binding.horizontalRingtones.smoothScrollToPosition(position)
        currentRingtone = allRingtones[position]
        playRingtoneAdapter.setCurrentPlayingPosition(position)
        viewModel.loadRingtoneById(currentRingtone.id)
        binding.currentRingtoneName.text = currentRingtone.name
        binding.currentRingtoneAuthor.text =
            currentRingtone.author?.name ?: resources.getString(R.string.unknwon_author)
        exoPlayer.release()
        exoPlayer = ExoPlayer.Builder(this).build()
        handler.removeCallbacks(progressUpdater)
    }

    private var lastDx: Int = 0
    private var duration = 0L
    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        playRingtoneAdapter.submitList(allRingtones)

        carousel = Carousel(this, binding.horizontalRingtones, playRingtoneAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scaleView(true)

        snapHelper.attachToRecyclerView(binding.horizontalRingtones)

        binding.apply {
            horizontalRingtones.initialPosition = index
            horizontalRingtones.adapter = playRingtoneAdapter

            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    currentId = -10
                    updateIndex(index, "onPositionChange")
                    setUpNewPlayer(position)
                    playRingtoneAdapter.setCurrentPlayingPosition(position, false)
                    // 🔁 force rebind to update playingHolder
                }

                override fun onScroll(dx: Int, dy: Int) {
                    lastDx = dx // ⬅️ Save dx for later use
                    Log.d("PlayerActivity", "Scrolling... dx = $dx")
                }
            })

            horizontalRingtones.setOnTouchListener { _, event ->
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
                        if(duration > 100) {
                            println("horizontalRingtones: $duration and $index")
                            binding.horizontalRingtones.stopScroll()
                            setUpNewPlayer(index)
                            playRingtoneAdapter.setCurrentPlayingPosition(index, false)
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
                            setUpNewPlayer(index)
                            handler.postDelayed({
                                playRingtoneAdapter.setCurrentPlayingPosition(index, false)
                            }, 300)
                        } else {
                            // stay on current
                            setUpNewPlayer(index)
                            playRingtoneAdapter.setCurrentPlayingPosition(index, false)
                        }

                        Log.d("PlayerActivity", "Touch UP - Scrolled to index=$index (dx=$lastDx)")
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
                            println("🟨 Scroll ended. Jumped: $distanceJumped (from $previousIndex to $newIndex)")

                            if (distanceJumped >= 2) {
                                Log.d("PlayerActivity", "🛑 Too fast! Jumped $distanceJumped items")
                                recyclerView.stopScroll()
                            }

                            updateIndex(newIndex, "onPositionChange")
                            setUpNewPlayer(index)
                            playRingtoneAdapter.setCurrentPlayingPosition(index, false)
                        }
                    }
                }
            })
        }


    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(progressUpdater)
        exoPlayer.release()
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
        Log.d("WallpaperActivity", "Index changed from $index to $newIndex by $caller")
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
        private val TAG = RingtoneActivity.javaClass.simpleName
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
