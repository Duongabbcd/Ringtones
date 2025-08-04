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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.adapter.PlayRingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityRingtoneBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.bottomsheet.DownloadRingtoneBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.FeedbackDialog
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.service.RingtonePlayerService
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private lateinit var handler: Handler
    private lateinit var carousel: Carousel
    // Modify your adapter initialization callback:
    private val playRingtoneAdapter: PlayRingtoneAdapter by lazy {
        PlayRingtoneAdapter(
            onRequestScrollToPosition = { newPosition ->
                carousel.scrollSpeed(200f)
                // Stop player via service if bound
                if (serviceBound) {
                    playerService?.stop()
                }

                setUpNewPlayer(newPosition)
                handler.postDelayed({ playRingtoneAdapter.setCurrentPlayingPosition(newPosition, false) }, 300)
            },
            onClickListener = { isPlaying, id ->
                println("Activity onClickListener: isPlaying=$isPlaying, id=$id")
                playRingtone(isPlaying, id)
            }
        )
    }

    private var currentId = -10
    private var isPlaying = false

    private var index = 0

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -100)
    }

    lateinit var exoPlayer: ExoPlayer
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private var returnedFromSettings = false

    // Update play/pause click to use service instead of ExoPlayer directly
    private fun playRingtone(isPlaying: Boolean = false, ringtoneId: Int? = null) {
        println("playRingtone called, serviceBound=$serviceBound, isPlaying=$isPlaying, id=$ringtoneId and ${playerService?.currentPlayingId}")
        if (!serviceBound) return

        if (isPlaying) {
            if (ringtoneId != null && ringtoneId != playerService?.currentPlayingId) {
                val ringtone = allRingtones.find { it.id == ringtoneId } ?: return
                currentRingtone = ringtone
                println("Calling service.playUri(${currentRingtone.contents.url})")
                playerService?.playUri(currentRingtone.contents.url, ringtoneId)
                currentId = ringtoneId
            } else {
                // SAME ringtone, check if it needs restarting
                if (playerService?.isAtEnd() == true) {
                    println("Player at end — restarting same ringtone")
                    playerService?.playUri(currentRingtone.contents.url, ringtoneId!!)
                } else{
                    println("Calling service.resume()")
                    playerService?.resume()
                }
            }
        } else  {
            println("Calling service.pause()")
            playerService?.pause()
        }
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


    private var currentRingtone = RingtonePlayerRemote.currentPlayingRingtone

    private val allRingtones by lazy {
        RingtonePlayerRemote.allSelectedRingtones
    }

    private var playerService: RingtonePlayerService? = null
    private var serviceBound = false

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName?, service: android.os.IBinder?) {
            val binder = service as RingtonePlayerService.LocalBinder
            playerService = binder.getService()
            serviceBound = true
            syncUiWithService()
        }

        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            serviceBound = false
            playerService = null
        }
    }

    private val progressReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            val progress = intent?.getLongExtra("progress", 0L) ?: 0L
            playRingtoneAdapter.updateProgress(progress.toFloat())
        }
    }

    private val endedReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            playRingtoneAdapter.onSongEnded()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(progressReceiver)
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(endedReceiver)
    }

    private fun syncUiWithService() {
        // Update adapter to reflect current play status and progress when bound
        playerService?.let {
            val isPlaying = it.isPlaying()
            val currentPosition = it.getCurrentPosition()
            // Update UI in adapter
            val playingPos = allRingtones.indexOf(currentRingtone)
            if (playingPos != -1) {
                playRingtoneAdapter.setCurrentPlayingPosition(playingPos, isPlaying)
                playRingtoneAdapter.updateProgress(currentPosition.toFloat())
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("currentId: ${savedInstanceState?.getInt("currentId")}")
        currentId = savedInstanceState?.getInt("currentId") ?: -1
        isPlaying = savedInstanceState?.getBoolean("isPlaying", false) ?: false
        // Bind service
        val intent = Intent(this, RingtonePlayerService::class.java)
        bindService(intent, serviceConnection, android.content.Context.BIND_AUTO_CREATE)

        // Register progress and ended receivers
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
            .registerReceiver(progressReceiver, android.content.IntentFilter("ringtone_progress"))
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
            .registerReceiver(endedReceiver, android.content.IntentFilter("ringtone_ended"))

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
                SearchRingtoneActivity.backToScreen(this@RingtoneActivity)
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

            // 🔥 NEW: query service state before acting
            val isPlaying = playerService?.isPlaying() ?: false
            val currentServiceId = playerService?.currentPlayingId

            if (currentServiceId == currentRingtone.id) {
                // Resume if it was playing before
                playRingtone(isPlaying, currentServiceId)
            } else {
                // Otherwise stop it (or no action)
                playRingtone(false)
            }

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
                    Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    private fun showGoToSettingsDialog() {
        Common.showDialogGoToSetting(this@RingtoneActivity) { result ->
            if (result) {
                returnedFromSettings = true
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupButtons() {
        binding.apply {
            download.setOnClickListener {
                downloadRingtone()
            }

            ringTone.setOnClickListener {
                setupRingtone()
            }

            notification.setOnClickListener {
                setupRingtone(true)
            }
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

    // When user scrolls or selects ringtone, also notify service
    private fun setUpNewPlayer(position: Int) {
        binding.horizontalRingtones.smoothScrollToPosition(position)
        currentRingtone = allRingtones[position]
        RingtonePlayerRemote.setCurrentRingtone(currentRingtone)
        playRingtoneAdapter.setCurrentPlayingPosition(position)
        currentId = currentRingtone.id
        viewModel.loadRingtoneById(currentRingtone.id)
        binding.currentRingtoneName.text = currentRingtone.name
        binding.currentRingtoneAuthor.text = currentRingtone.author.name
        syncUiWithService()
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
                    playerService?.stop()
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
        // Don't release player here! Service manages it.
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

        if (!serviceBound) return

        val servicePlaying = playerService?.isPlaying() ?: false
        val serviceId = playerService?.currentPlayingId ?: -1

        // Convert ringtone ID to adapter position
        val position = allRingtones.indexOfFirst { it.id == serviceId }.takeIf { it != -1 } ?: 0

        currentId = serviceId
        isPlaying = servicePlaying

        println("onResume - servicePlaying: $servicePlaying, serviceId: $serviceId, position: $position")

        // If player is NOT playing, try to resume
        if (!servicePlaying) {
            playerService?.resume()
            isPlaying = true
            println("Activity onResume: called resume()")
        }

        // Update adapter and UI based on final state
        playRingtoneAdapter.setCurrentPlayingPosition(position, isPlaying)
        syncUiWithService()
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this)
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
                Toast.makeText(this, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateIndex(newIndex: Int, caller: String) {
        Log.d("WallpaperActivity", "Index changed from $index to $newIndex by $caller")
        index = newIndex
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentId", currentId)
        outState.putBoolean("isPlaying", isPlaying)
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            playerService?.pause()
            playRingtoneAdapter.setCurrentPlayingPosition(index, false)
            println("Activity onStop: pause playback")
        }
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



