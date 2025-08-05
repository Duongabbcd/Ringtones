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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
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
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.backgroundUrl
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.apply
import androidx.core.content.edit
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.endCall
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit.CallScreenEditorActivity.Companion.startCall

@AndroidEntryPoint
class CallScreenFragment :
    BaseFragment<FragmentCallscreenBinding>(FragmentCallscreenBinding::inflate) {

    private val callScreenViewModel: CallScreenViewModel by viewModels()
    private val contentViewModel: ContentViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by activityViewModels()

    private lateinit var defaultAppSettingsLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    private val callScreenAdapter: CallScreenAdapter by lazy {
        CallScreenAdapter { result ->
            currentCallScreen = result
            contentViewModel.getCallScreenContent(result.id)
            contentViewModel.getBackgroundContent(result.id)

        }
    }

    private var currentCallScreen: CallScreenItem = CallScreenItem.CALLSCREEN_EMPTY

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultAppSettingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                openOverlayPermissionSettings()
            }

        overlayPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Toast.makeText(
                    requireContext(),
                    "Setup complete. You can now use call screen.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        binding.apply {
            val ctx = context ?: return@apply
            val prefs = ctx.getSharedPreferences("callscreen_prefs",MODE_PRIVATE)
            backgroundUrl = prefs.getString("BACKGROUND", "") ?: ""
            avatarUrl = prefs.getString("AVATAR", "") ?: ""

            Glide.with(ctx)
                .load(backgroundUrl)
                .placeholder(R.drawable.default_callscreen)
                .error(R.drawable.default_callscreen)
                .into(binding.currentCallScreen)
            Glide.with(ctx)
                .load(avatarUrl)
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

            contentViewModel.callScreenContent.observe(viewLifecycleOwner) { items ->
                if (items.size >= 2) {
                    endCall = items.first().url.full
                    startCall = items.last().url.full
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

    private fun triggerCallScreenPermission(ctx: Context) {
        if (!ctx.isAlreadyDefaultDialer()) {
            launchSetDefaultDialerIntent(ctx) { _ ->
                if (ctx.isAlreadyDefaultDialer()) {
                    Toast.makeText(ctx, getString(R.string.successful_setup), Toast.LENGTH_SHORT).show()
                    checkAndRequestPermissions()
                } else {
                    Toast.makeText(ctx,getString(R.string.error_setup), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            saveCallScreenPreference("BACKGROUND", backgroundUrl)
            saveCallScreenPreference("CANCEL", endCall)
            saveCallScreenPreference("ANSWER", startCall)
            Toast.makeText(ctx, getString(R.string.successful_setup), Toast.LENGTH_SHORT).show()
        }
    }

    private var isPermission = false
    private var onRequestDialerCallBack: ((granted: Boolean) -> Unit)? = null

    @SuppressLint("InlinedApi")
    fun launchSetDefaultDialerIntent(context: Context, callback: (granted: Boolean) -> Unit) {
        val telecomManager = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        val isAlreadyDefaultDialer = context.packageName == telecomManager.defaultDialerPackage
        println("launchSetDefaultDialerIntent is here: $isAlreadyDefaultDialer")
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
        //       else {
//            openDefaultPhoneAppSettings()
//        }
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
                "Please grant all permissions to continue.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openDefaultPhoneAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            defaultAppSettingsLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Can't open default apps settings.",
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
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Can't open overlay permission settings.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun displayCallScreen(background: String) {
        val ctx = context ?: return
        val url = currentCallScreen.thumbnail.url.medium
        println("displayCallScreen: $background")
        backgroundUrl = url
        Glide.with(ctx)
            .load(url)
            .placeholder(R.drawable.default_callscreen)
            .error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)
    }

    private fun saveCallScreenPreference(tag: String, value: String) {
        println("saveCallScreenPreference: $tag and $value")
        val prefs = requireContext().getSharedPreferences("callscreen_prefs", Context.MODE_PRIVATE)
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

    companion object {
        @JvmStatic
        fun newInstance() = CallScreenFragment()

        private const val REQUEST_CODE_PERMISSIONS = 101

        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.MANAGE_OWN_CALLS
        )

        const val IS_PERMISSION_DUP_KEY = "IS_PERMISSION_DUP_KEY"
        const val REQUEST_CODE_SET_DEFAULT_DIALER = 1007
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