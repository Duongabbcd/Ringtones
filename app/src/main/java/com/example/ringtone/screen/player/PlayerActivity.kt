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
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.ringtone.remote.viewmodel.FavouriteRingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.ringtone.R
import com.example.ringtone.screen.player.bottomsheet.DownloadBottomSheet
import com.example.ringtone.screen.player.dialog.FeedbackDialog
import com.example.ringtone.utils.Common
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private val viewModel: FavouriteRingtoneViewModel by viewModels()
    private var downloadedUri: Uri? = null

    private lateinit var handler: Handler
    private val playerAdapter: PlayerAdapter by lazy {
        PlayerAdapter(onRequestScrollToPosition = { newPosition ->
            binding.horizontalRingtones.smoothScrollToPosition(newPosition)
            setUpNewPlayer(newPosition)
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
                    playerAdapter.onSongEnded()
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
        displayFavouriteIcon()
        // Initialize ExoPlayer
        playRingtone(false)
        initViewPager()

        binding.apply {
            currentRingtoneName.isSelected = true
            backBtn.setOnClickListener {
                finish()
            }
            val index = allRingtones.indexOf(currentRingtone)
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
                bottomSheet.updateProgress(progress)
            }
            withContext(Dispatchers.Main) {
                if (uri != null) {
                    downloadedUri = uri
                    bottomSheet.showSuccess()
                    viewModel.increaseDownload(currentRingtone)
                    Toast.makeText(this@PlayerActivity, "Downloaded!", Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheet.showFailure()
                    Toast.makeText(this@PlayerActivity, "Download failed.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupRingtone() {
        if (!RingtoneHelper.hasWriteSettingsPermission(this)) {
            // Ask the user to grant WRITE_SETTINGS
            returnedFromSettings = true
            RingtoneHelper.requestWriteSettingsPermission(this)
            return
        }

        // Now permission is granted—set the ringtone
        setRingtoneAfterPermission()


    }

    private fun setRingtoneAfterPermission() {
        val ringtoneTitle = currentRingtone.name
        val ringtoneUrl = currentRingtone.contents.url
        Toast.makeText(this@PlayerActivity, "Preparing...", Toast.LENGTH_SHORT).show()
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
                saveRingtone()
            }
        }


    }

    private fun saveRingtone() {
        if (Settings.System.canWrite(this@PlayerActivity)) {
            println("System can write: $downloadedUri")
            val success = downloadedUri?.let {
                RingtoneHelper.setAsSystemRingtone(this@PlayerActivity, it)
            } == true
            if (success) {
                Toast.makeText(this@PlayerActivity, "Ringtone set!", Toast.LENGTH_SHORT).show()
                    .also {
                        viewModel.increaseSet(currentRingtone)
                    }
            } else {
                Toast.makeText(this@PlayerActivity, "Failed to set ringtone.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun displayFavouriteIcon(isManualChange: Boolean = false) {
        viewModel.ringtone.observe(this) { ringtone ->
            if (ringtone.id == currentRingtone.id) {

                if (isManualChange) {
                    viewModel.deleteRingtone(currentRingtone)
                    binding.favourite.setImageResource(R.drawable.icon_unfavourite)
                } else {
                    binding.favourite.setImageResource(R.drawable.icon_favourite)
                }
            } else {
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
        currentRingtone = allRingtones[position]
        RingtonePlayerRemote.currentPlayingRingtone = allRingtones[position]
        displayFavouriteIcon()
        playerAdapter.setCurrentPlayingPosition(position)
        Log.d("PlayerActivity", "onPositionChange $currentRingtone")
        binding.currentRingtoneName.text = currentRingtone.name
        binding.currentRingtoneAuthor.text = currentRingtone.author.name
        exoPlayer.release()
        exoPlayer = ExoPlayer.Builder(this).build()
        handler.removeCallbacks(progressUpdater)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        playerAdapter.submitList(allRingtones)
        val carousel = Carousel(this, binding.horizontalRingtones, playerAdapter)
        carousel.setOrientation(CarouselView.HORIZONTAL, false)
        carousel.scrollSpeed(100f)
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
                    setUpNewPlayer(position)
                    // 🔁 force rebind to update playingHolder
                }

                override fun onScroll(dx: Int, dy: Int) {
//                    Log.d("PlayerActivity",  "onScroll dx : $dx -- dy : $dx")
                }
            })

            horizontalRingtones.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            // User started scrolling
                            Log.d("WheelPicker", "User is actively scrolling")
                        }

                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // User started scrolling

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

