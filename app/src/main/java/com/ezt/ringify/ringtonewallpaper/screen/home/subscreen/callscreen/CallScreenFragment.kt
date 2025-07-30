package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.base.BaseFragment
import com.ezt.ringify.ringtonewallpaper.databinding.FragmentCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemCallscreenBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenItem
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CallScreenViewModel
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.ContentViewModel
import dagger.hilt.android.AndroidEntryPoint

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
            displayCallScreen()
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
                Toast.makeText(requireContext(), "Setup complete. You can now use call screen.", Toast.LENGTH_SHORT).show()
            }

        binding.apply {
            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                checkInternetConnected(isConnected)
            }

            allQuickThemes.adapter = callScreenAdapter
            allQuickThemes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            callScreenViewModel.callScreens.observe(viewLifecycleOwner) { items ->
                callScreenAdapter.submitList(items.take(10))
            }

            callScreenViewModel.loading.observe(viewLifecycleOwner) {
                progressBar.isVisible = it
            }

            contentViewModel.content.observe(viewLifecycleOwner) { items ->
                if (items.size > 2) {
                    saveCallScreenPreference("CANCEL", items.first().url.full)
                    saveCallScreenPreference("ANSWER", items.last().url.full)
                }
            }

            noInternet.tryAgain.setOnClickListener {
                val connected = connectionViewModel.isConnectedLiveData.value ?: false
                if (connected) {
                    origin.visible()
                    noInternet.root.gone()
                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT).show()
                }
            }

            setupCallScreen.setOnClickListener {
                checkAndRequestPermissions()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            openDefaultPhoneAppSettings()
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
            Toast.makeText(requireContext(), "Please grant all permissions to continue.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDefaultPhoneAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            defaultAppSettingsLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Can't open default apps settings.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOverlayPermissionSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            overlayPermissionLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Can't open overlay permission settings.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayCallScreen() {
        val ctx = context ?: return
        val url = currentCallScreen.thumbnail.url.medium
        saveCallScreenPreference("BACKGROUND", url)
        Glide.with(ctx)
            .load(url)
            .placeholder(R.drawable.default_callscreen)
            .error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)
    }

    private fun saveCallScreenPreference(tag: String, value: String) {
        val prefs = requireContext().getSharedPreferences("callscreen_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(tag, value).apply()
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