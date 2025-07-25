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
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.adapter.PlayRingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityRingtoneBinding
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.bottomsheet.DownloadRingtoneBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog.FeedbackDialog
import com.ezt.ringify.ringtonewallpaper.utils.Common
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
    private lateinit var handler: Handler
    private lateinit var carousel: Carousel
    private val playRingtoneAdapter: PlayRingtoneAdapter by lazy {
        PlayRingtoneAdapter(onRequestScrollToPosition = { newPosition ->
            carousel.scrollSpeed(200f)
            setUpNewPlayer(newPosition)
            handler.postDelayed(   {playRingtoneAdapter.setCurrentPlayingPosition(newPosition, false)}, 300)
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
                    handler.postDelayed(  {playRingtoneAdapter.onSongEnded()} , 300)

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

        binding.apply {
            currentRingtoneName.isSelected = true
            backBtn.setOnClickListener {
                finish()
            }
            index = allRingtones.indexOf(currentRingtone)
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
        Common.showDialogGoToSetting(this@RingtoneActivity) { result ->
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

    private fun setUpNewPlayer(position: Int) {
        binding.horizontalRingtones.smoothScrollToPosition(position)
        currentRingtone = allRingtones[position]
        playRingtoneAdapter.setCurrentPlayingPosition(position)
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



