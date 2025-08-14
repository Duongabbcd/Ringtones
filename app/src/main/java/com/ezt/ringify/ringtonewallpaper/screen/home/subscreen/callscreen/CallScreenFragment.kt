package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Context.TELECOM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseFragment
import com.ezt.ringify.ringtonewallpaper.databinding.FragmentCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenItem
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CallScreenViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.ContentViewModel
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.apply
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.airbnb.lottie.LottieAnimationView
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.CacheUtil
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PlayerManager
import com.ezt.ringify.ringtonewallpaper.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.content.edit

@AndroidEntryPoint
class CallScreenFragment :
    BaseFragment<FragmentCallscreenBinding>(FragmentCallscreenBinding::inflate) {

    private val callScreenViewModel: CallScreenViewModel by viewModels()
    private val contentViewModel: ContentViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by activityViewModels()

    private lateinit var defaultAppSettingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private var callScreenSetupInProgress = false

    private val callScreenAdapter: CallScreenAdapter by lazy {
        CallScreenAdapter { result ->
            val ctx = context ?: return@CallScreenAdapter
            videoBackgroundUrl = ""
            binding.playerContainer.gone()
            hasRenderedFirstFrame = false
            currentListener?.let {
                PlayerManager.getPlayer(ctx).removeListener(it)
                currentListener = null
            }
            binding.currentCallScreen.visible()
            contentViewModel.getCallScreenContent(result.id)
            contentViewModel.getBackgroundContent(result.id)
        }
    }
    private var onRequestDialerCallBack: ((granted: Boolean) -> Unit)? = null
    private var currentListener: Player.Listener? = null
    private var hasRenderedFirstFrame = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (callScreenSetupInProgress) {
            val ctx = context ?: return
            triggerCallScreenPermission(ctx)
        }

        defaultAppSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                openOverlayPermissionSettings()
            }

        overlayPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.setup_complete),
                    Toast.LENGTH_SHORT
                ).show()
            }

        binding.apply {
            val ctx = context ?: return@apply
            val prefs = ctx.getSharedPreferences("callscreen_prefs",MODE_PRIVATE)

            val backupBackground = prefs.getString("BACKGROUND", "") ?: ""
            var video = ""
            var photo = ""
            if (backupBackground.endsWith(".mp4", true)) {
                video = backupBackground
            } else {
                photo = backupBackground
            }

            println("currentEnd and currentStart: $backupBackground $video ------ $photo")


            videoBackgroundUrl =
                if (videoBackgroundUrl.isEmpty() && photoBackgroundUrl.isEmpty()) video else videoBackgroundUrl
            photoBackgroundUrl =
                if (photoBackgroundUrl.isEmpty() && videoBackgroundUrl.isEmpty()) photo else photoBackgroundUrl

            val backupAvatar = prefs.getString("AVATAR", "") ?: ""
            val currentAvatar = if (avatarUrl != "") avatarUrl else backupAvatar

            val backupEnd = prefs.getString("CANCEL", "") ?: ""
            val currentEnd = if (endCall != "") endCall else backupEnd

            val backupStart = prefs.getString("ANSWER", "") ?: ""
            val currentStart = if (startCall != "") startCall else backupStart
            println("currentEnd and currentStart 123: $currentEnd ------ $currentStart")
            startCall = if (startCall.isEmpty()) currentStart else startCall
            endCall = if (endCall.isEmpty()) currentEnd else endCall


            displayIcon(endCall, startCall)

            if (videoBackgroundUrl.isNotEmpty()) {
                currentCallScreen.gone()
                playerContainer.visible()
                displayVideoBackground(videoBackgroundUrl)
            } else {
                currentCallScreen.visible()
                playerContainer.gone()
                displayCallScreen(photoBackgroundUrl)
            }


            Glide.with(ctx)
                .load(currentAvatar)
                .placeholder(R.drawable.default_cs_avt)
                .error(R.drawable.default_cs_avt)
                .into(binding.defaultAvatar)

            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                checkInternetConnected(isConnected)
            }

            allQuickThemes.adapter = callScreenAdapter
            allQuickThemes.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            callScreenViewModel.callScreens.observe(viewLifecycleOwner) { items ->
                callScreenAdapter.submitList(items.take(10))
            }

            callScreenViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    val loadingItems = List(5) {
                        CallScreenItem.CALLSCREEN_EMPTY
                    }
                    callScreenAdapter.submitList(loadingItems)

                    // Disable scrolling
                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    callScreenViewModel.callScreens.value?.let { realItems ->
                        callScreenAdapter.submitList(realItems.take(10))
                    }

                    // Re-enable touch
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }

            contentViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar2.isVisible = isLoading
            }

            contentViewModel.callScreenContent.observe(viewLifecycleOwner) { items ->
                if (items.size >= 2) {
                    endCall = items.first().url.full
                    startCall = items.last().url.full
                    displayIcon(endCallIcon = endCall, startCallIcon = startCall)
                }
            }
            contentViewModel.backgroundContent.observe(viewLifecycleOwner) { items ->
                if (items.isEmpty()) {
                    return@observe
                }
                val url = items.first().url.full
                displayCallScreen(url)
            }

            noInternet.tryAgain.setOnClickListener {
                val connected = connectionViewModel.isConnectedLiveData.value ?: false
                if (connected) {
                    origin.visible()
                    noInternet.root.gone()
                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                        .show()
                }
            }

            setupCallScreen.setOnClickListener {
                triggerCallScreenPermission(requireActivity())
            }

            backgroundCs.setOnClickListener {
                withSafeContext {  ctx ->
                    startActivity(Intent(ctx, CallScreenEditorActivity::class.java).apply {
                        putExtra("editorType", 1)
                    })
                }
            }
            avatarCs.setOnClickListener {
                withSafeContext {  ctx ->
                    startActivity(Intent(ctx, CallScreenEditorActivity::class.java).apply {
                        putExtra("editorType", 2)
                    })
                }
            }

            iconCs.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, CallScreenEditorActivity::class.java).apply {
                        putExtra("editorType", 3)
                    })
                }
            }

            alertCs.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, CallScreenAlertActivity::class.java))
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun displayVideoBackground(videoBackgroundUrl: String) {
        androidx.media3.common.util.Log.d(
            "PlayerViewHolder",
            "attachPlayer() called with url: $videoBackgroundUrl"
        )
        val ctx = context ?: return
        val player = PlayerManager.getPlayer(ctx)
        val simpleCache = CacheUtil.getSimpleCache(ctx)

        val dataSourceFactory = DefaultDataSource.Factory(ctx)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val mediaItem = MediaItem.fromUri(Uri.parse(videoBackgroundUrl))
        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player.apply {
            stop()
            clearMediaItems()
            setMediaSource(mediaSource)
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true

            // 👇 Delay prepare() to ensure playerView is ready
            binding.playerView.player = this
            binding.playerView.post {
                androidx.media3.common.util.Log.d(
                    "PlayerViewHolder",
                    "Calling prepare() after post"
                )
                prepare()
            }

            currentListener?.let { removeListener(it) }
            currentListener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    androidx.media3.common.util.Log.d("ExoPlayer", "player state = $state")
                    if (state == Player.STATE_READY && !hasRenderedFirstFrame) {
                        binding.progressBar2.visibility = View.VISIBLE
                        binding.playerView.alpha = 0f
                        binding.playerView.visibility = View.VISIBLE
                    }
                }

                override fun onRenderedFirstFrame() {
                    if (!hasRenderedFirstFrame) {
                        hasRenderedFirstFrame = true
                        binding.progressBar2.visibility = View.GONE
                        binding.playerView.visibility = View.VISIBLE
                        binding.playerView.alpha = 1f
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        binding.progressBar2.visibility = View.GONE
                    }
                }
            }

            addListener(currentListener!!)
        }
    }

    private fun triggerCallScreenPermission(ctx: Context) {
        if (!ctx.isAlreadyDefaultDialer()) {
            callScreenSetupInProgress = true
            launchSetDefaultDialerIntent(ctx) { granted ->
                if (granted) openOverlayPermissionSettings()
            }
        } else {
            openOverlayPermissionSettings()
        }
    }

    private fun runDownloadStep(ctx: Context) {
        val input = if (videoBackgroundUrl.isEmpty()) photoBackgroundUrl else videoBackgroundUrl
        println("saveCallScreenPreference: $photoBackgroundUrl and $videoBackgroundUrl")
        println("saveCallScreenPreference 123: $endCall and $startCall")

        lifecycleScope.launch {
            Toast.makeText(ctx, getString(R.string.processing), Toast.LENGTH_SHORT).show()
            withContext(Dispatchers.IO) {
                val result = Utils.downloadCallScreenFile(ctx, fileUrl = input)
                saveCallScreenPreference("BACKGROUND", result?.backgroundPath ?: "")

                val result2 =
                    Utils.downloadCallScreenFile(ctx, fileUrl = endCall, folderName = "endCall")
                saveCallScreenPreference("CANCEL", result2?.backgroundPath ?: "")

                val result3 =
                    Utils.downloadCallScreenFile(ctx, fileUrl = startCall, folderName = "startCall")
                saveCallScreenPreference("ANSWER", result3?.backgroundPath ?: "")

                saveCallScreenPreference("AVATAR", avatarUrl)
            }

            Toast.makeText(ctx, getString(R.string.successful_setup), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (callScreenSetupInProgress && requireContext().isAlreadyDefaultDialer()) {
            callScreenSetupInProgress = false
            runDownloadStep(requireContext().applicationContext)
        }
    }

    @SuppressLint("InlinedApi")
    fun launchSetDefaultDialerIntent(context: Context, callback: (granted: Boolean) -> Unit) {
        val telecomManager = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        val isAlreadyDefaultDialer = context.packageName == telecomManager.defaultDialerPackage
        Log.d(TAG, "launchSetDefaultDialerIntent is here: $isAlreadyDefaultDialer")
        if (isAlreadyDefaultDialer) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                    RoleManager.ROLE_DIALER
                )
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                requireActivity().packageName
            ).apply {
                runCatching {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                }
            }
        }

        onRequestDialerCallBack = {
            callback.invoke(it)
        }
    }

    fun Context.isAlreadyDefaultDialer(): Boolean {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        return this.packageName == telecomManager.defaultDialerPackage
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (resultCode == Activity.RESULT_OK) {
                onRequestDialerCallBack?.invoke(true)

            } else {
                onRequestDialerCallBack?.invoke(false)
            }
        }
    }

    //consider later
    private fun checkAndRequestPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            openDefaultPhoneAppSettings()
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.grant_all_permissions),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openDefaultPhoneAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            defaultAppSettingsLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.can_not_open_default),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openOverlayPermissionSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            overlayPermissionLauncher.launch(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.can_not_overlay),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun displayCallScreen(background: String) {
        val ctx = context ?: return
        Log.d(TAG, "displayCallScreen: $background")
        photoBackgroundUrl = background
        Glide.with(ctx)
            .load(photoBackgroundUrl)
            .placeholder(R.drawable.default_callscreen)
            .error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)
    }

    private fun saveCallScreenPreference(tag: String, value: String) {
        Log.d(TAG, "saveCallScreenPreference: $tag and $value")
        val appCtx = requireContext().applicationContext
        val prefs = appCtx.getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        prefs.edit { putString(tag, value) }
    }

    private fun checkInternetConnected(isConnected: Boolean) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            callScreenViewModel.loadCallScreens()
            binding.noInternet.root.gone()
        }
    }


    private fun displayIcon(endCallIcon: String, startCallIcon: String) {
        binding.apply {
            setIcon(endCallIcon, endImage, endCallLottie, R.drawable.icon_end_call)
            setIcon(startCallIcon, startImage, startCallLottie, R.drawable.icon_start_call)
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("callScreenSetupInProgress", callScreenSetupInProgress)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        callScreenSetupInProgress =
            savedInstanceState?.getBoolean("callScreenSetupInProgress", false) ?: false

        if (callScreenSetupInProgress) {
            triggerCallScreenPermission(requireContext())
        }
    }

    override fun onDestroyView() {
        currentListener?.let {
            PlayerManager.getPlayer(requireContext()).removeListener(it)
        }
        currentListener = null
        super.onDestroyView()
    }


    companion object {
        @JvmStatic
        fun newInstance() = CallScreenFragment()
        var photoBackgroundUrl: String = ""
        var videoBackgroundUrl: String = ""
        var avatarUrl: String = ""

        var endCall: String = ""
        var startCall: String = ""
        private val TAG = CallScreenFragment.javaClass.name

        private const val REQUEST_CODE_PERMISSIONS = 101

        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.MANAGE_OWN_CALLS
        )

        const val REQUEST_CODE_SET_DEFAULT_DIALER = 1007


        fun setIcon(
            url: String,
            imageView: ImageView,
            lottieView: LottieAnimationView,
            placeholder: Int
        ) {
            if (url.endsWith(".json", true)) {
                imageView.gone()
                lottieView.visible()
                val file = File(url)
                if (file.exists()) {
                    val json = file.readText()
                    lottieView.setAnimationFromJson(json, file.name)
                } else {
                    lottieView.setAnimationFromUrl(url)
                }
                lottieView.progress = 0f
                lottieView.playAnimation()
            } else {
                imageView.visible()
                lottieView.gone()
                Glide.with(imageView.context)
                    .load(url)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(imageView)
            }
        }

    }
}


class CallScreenAdapter(private val onClickListener: (CallScreenItem) -> Unit) :
    RecyclerView.Adapter<CallScreenAdapter.CallScreenViewHolder>() {
    private val allCallScreens: MutableList<CallScreenItem> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CallScreenViewHolder {
        context = parent.context
        return CallScreenViewHolder(
            ItemCallscreenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private var premium = false
    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: CallScreenViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<CallScreenItem>, isPremium: Boolean = false) {
        allCallScreens.clear()
        allCallScreens.addAll(list)

        premium = isPremium
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = allCallScreens.size

    inner class CallScreenViewHolder(private val binding: ItemCallscreenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val callScreen = allCallScreens[position]
            binding.apply {
                if (callScreen == CallScreenItem.CALLSCREEN_EMPTY) {
                    binding.callScreenImage.setImageResource(R.drawable.default_callscreen)
                } else {
                    binding.callScreenImage.load(callScreen.thumbnail.url.medium) {
                        crossfade(true) // Optional fade animation
                        placeholder(R.drawable.default_callscreen)
                        error(R.drawable.default_callscreen)
                        listener(
                            onSuccess = { _, _ ->
                                progressBar.visibility = View.GONE
                            },
                            onError = { _, _ ->
                                progressBar.visibility = View.GONE
                            }
                        )
                    }

                    root.setOnClickListener {
                        onClickListener(callScreen)
                    }
                }
            }
        }
    }

}