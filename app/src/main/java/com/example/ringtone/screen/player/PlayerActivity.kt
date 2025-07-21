package com.example.ringtone.screen.player

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
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityPlayerBinding
import com.example.ringtone.screen.player.adapter.PlayerAdapter
import com.example.ringtone.utils.RingtonePlayerRemote
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.ringtone.remote.viewmodel.FavouriteRingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.ringtone.R
import com.example.ringtone.screen.player.bottomsheet.DownloadBottomSheet
import com.example.ringtone.screen.player.dialog.FeedbackDialog
import com.example.ringtone.utils.Common
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private val viewModel: FavouriteRingtoneViewModel by viewModels()
    private var downloadedUri: Uri? = null

    private lateinit var handler: Handler
    private lateinit var carousel: Carousel
    private val playerAdapter: PlayerAdapter by lazy {
        PlayerAdapter(onRequestScrollToPosition = { newPosition ->
            carousel.scrollSpeed(200f)
            setUpNewPlayer(newPosition)
            handler.postDelayed(   {playerAdapter.setCurrentPlayingPosition(newPosition, false)}, 200)
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

    private var index = 0


    lateinit var exoPlayer: ExoPlayer
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

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
                    handler.postDelayed(  {playerAdapter.onSongEnded()} , 300)

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
                playerAdapter.updateProgress(progress)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())

        checkDownloadPermissions()


        viewModel.loadRingtoneById(currentRingtone.id)
        observeRingtoneFromDb()
        // Initialize ExoPlayer
        playRingtone(false)
        initViewPager()

        binding.apply {
            currentRingtoneName.isSelected = true
            backBtn.setOnClickListener {
                finish()
            }
            index = allRingtones.indexOf(currentRingtone)
            horizontalRingtones.scrollToPosition(index)

            favourite.setOnClickListener {
                displayFavouriteIcon(true)
            }


            feedback.setOnClickListener {
                val dialog = FeedbackDialog(this@PlayerActivity)
                dialog.setRingtoneFeedback(currentRingtone)
                dialog.show()
            }
            setupButtons()
        }
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
        Common.showDialogGoToSetting(this@PlayerActivity) { result ->
            if (result) {
                returnedFromSettings = true
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

        val missingPermissions = RingtoneHelper.getMissingMediaPermissions(this)

        if (missingPermissions.isEmpty()) {
            // All permissions granted
            actuallyDownloadRingtone()
        } else {
            // Request the missing permissions using launcher
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }

        RingtoneHelper.getMissingMediaPermissions(this@PlayerActivity)

    }

    private fun actuallyDownloadRingtone(isBackground: Boolean = false) {

        val bottomSheet = DownloadBottomSheet(this)
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
                this@PlayerActivity,
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

    private fun enableDismiss(bottomSheet: DownloadBottomSheet) {
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
                this@PlayerActivity,
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
        val dialog = DownloadBottomSheet(this, if(isNotification) "notification" else "ringtone")
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
        if (Settings.System.canWrite(this@PlayerActivity)) {
            println("System can write: $downloadedUri")
            val success = downloadedUri?.let {
                RingtoneHelper.setAsSystemRingtone(this@PlayerActivity, it, isNotification)
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

    private fun observeRingtoneFromDb() {
        viewModel.ringtone.observe(this) { dbRingtone ->
            val isFavorite = dbRingtone.id == currentRingtone.id
            binding.favourite.setImageResource(
                if (isFavorite) R.drawable.icon_favourite
                else R.drawable.icon_unfavourite
            )
        }
    }

    private fun displayFavouriteIcon(isManualChange: Boolean = false) {
        viewModel.ringtone.observe(this) { dbRingtone ->
            if (dbRingtone.id == currentRingtone.id) {
                // It's a favorite
                if (isManualChange) {
                    viewModel.deleteRingtone(currentRingtone)
                    binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                } else {
                    binding.favourite.setImageResource(R.drawable.icon_favourite)
                }
            } else {
                // Not a favorite
                if (isManualChange) {
                    viewModel.insertRingtone(currentRingtone)
                    binding.favourite.setImageResource(R.drawable.icon_favourite)
                } else {
                    binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                }
            }
        }
    }


    private fun setUpNewPlayer(position: Int) {
        binding.horizontalRingtones.smoothScrollToPosition(position)
        currentRingtone = allRingtones[position]
        playerAdapter.setCurrentPlayingPosition(position)
        viewModel.loadRingtoneById(currentRingtone.id)
        binding.currentRingtoneName.text = currentRingtone.name
        binding.currentRingtoneAuthor.text = currentRingtone.author.name
        exoPlayer.release()
        exoPlayer = ExoPlayer.Builder(this).build()
        handler.removeCallbacks(progressUpdater)
    }

    private var lastDx: Int = 0

    private var duration = 0L
    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        playerAdapter.submitList(allRingtones)

        carousel = Carousel(this, binding.horizontalRingtones, playerAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)

        carousel.scaleView(true)

        val recyclerView = binding.horizontalRingtones.getChildAt(0) as? RecyclerView

        recyclerView?.let {
            val snapHelper = OneItemAtATimeSnapHelper()
            snapHelper.attachToRecyclerView(recyclerView)

            it.onFlingListener = null
            it.setOnTouchListener { _, event ->
                // Optional: custom touch logic to restrict fast flings
                false
            }

            // Disable flickering during item change animations
            it.itemAnimator?.changeDuration = 0
            (it.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }



        binding.apply {
            horizontalRingtones.adapter = playerAdapter


            carousel.addCarouselListener(object : CarouselListener {
                override fun onPositionChange(position: Int) {
                    currentId = -10
                    index = position
                    setUpNewPlayer(position)
                    playerAdapter.setCurrentPlayingPosition(position, false)
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
                            playerAdapter.setCurrentPlayingPosition(index, false)
                            return@setOnTouchListener false
                        }

                        // 👇 Get current visible position
                        val layoutManager = horizontalRingtones.layoutManager as LinearLayoutManager
                        val visiblePos = layoutManager.findFirstCompletelyVisibleItemPosition()

                        // 👇 Decide scroll direction (and clamp to ±1)
                        val newIndex = when {
                            lastDx > 0 && index < allRingtones.size - 1 -> index + 1
                            lastDx < 0 && index > 0 -> index - 1
                            else -> index
                        }

                        // 👇 Update only if actual index changes
                        if (newIndex != index) {
                            index = newIndex
                            setUpNewPlayer(index)
                            handler.postDelayed({
                                playerAdapter.setCurrentPlayingPosition(index, false)
                            }, 200)
                        } else {
                            // stay on current
                            setUpNewPlayer(index)
                            playerAdapter.setCurrentPlayingPosition(index, false)
                        }

                        Log.d("PlayerActivity", "Touch UP - Scrolled to index=$index (dx=$lastDx)")
                    }
                }
                false
            }

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(horizontalRingtones)

            horizontalRingtones.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val view = snapHelper.findSnapView(layoutManager)
                        val position = layoutManager.getPosition(view!!)

                        if (position != index) {
                            index = position
                            setUpNewPlayer(index)
                            playerAdapter.setCurrentPlayingPosition(index, false)
                        } else {
                            binding.horizontalRingtones.stopScroll()
                            setUpNewPlayer(index)
                            playerAdapter.setCurrentPlayingPosition(index, false)
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

}

class OneItemAtATimeSnapHelper : LinearSnapHelper() {
    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val currentPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        val visibleView = findSnapView(layoutManager)
        val currentViewPosition = layoutManager.getPosition(visibleView!!)

        return when {
            velocityX > 0 -> currentViewPosition + 1
            velocityX < 0 -> currentViewPosition - 1
            else -> currentViewPosition
        }.coerceIn(0, layoutManager.itemCount - 1)
    }
}

//class OneItemScrollManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {
//    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
//        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
//            override fun getHorizontalSnapPreference(): Int {
//                return SNAP_TO_START
//            }
//
//            override fun calculateTimeForScrolling(dx: Int): Int {
//                return 150 // control scroll speed
//            }
//        }
//        smoothScroller.targetPosition = position
//        startSmoothScroll(smoothScroller)
//    }
//
//    override fun canScrollHorizontally(): Boolean = true
//
//    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
//        // Clamp scroll amount if needed
//        return super.scrollHorizontallyBy(dx, recycler, state)
//    }
//
//    override fun fling(velocityX: Int, velocityY: Int): Boolean {
//        // Optional: fully block flings
//        return false
//    }
//}

