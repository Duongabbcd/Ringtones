package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telecom.TelecomManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private val callScreenAdapter: CallScreenAdapter by lazy {
        CallScreenAdapter { result ->
            currentCallScreen = result
            contentViewModel.getCallScreenContent(result.id)
            displayCallScreen()
        }
    }

    private fun displayCallScreen() {
        val ctx = context ?: return
        val url = currentCallScreen.thumbnail.url.medium
        saveCallScreenPreference("BACKGROUND", url)
        Glide.with(ctx).load(url).placeholder(R.drawable.default_callscreen)
            .error(R.drawable.default_callscreen).into(binding.currentCallScreen)

    }

    private var currentCallScreen: CallScreenItem = CallScreenItem.CALLSCREEN_EMPTY

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            allQuickThemes.adapter = callScreenAdapter

            val ctx = context ?: return@apply
            allQuickThemes.layoutManager =
                LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)

            callScreenViewModel.callScreens.observe(viewLifecycleOwner) { items ->
                callScreenAdapter.submitList(items.take(10))
            }

            noInternet.tryAgain.setOnClickListener {
                withSafeContext { ctx ->
                    val connected = connectionViewModel.isConnectedLiveData.value ?: false
                    if (connected) {
                        binding.origin.visible()
                        binding.noInternet.root.visibility = View.VISIBLE
                        // Maybe reload your data
                    } else {
                        Toast.makeText(
                            ctx,
                            R.string.no_connection,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            callScreenViewModel.loading.observe(viewLifecycleOwner) { result ->
                progressBar.isVisible = result
            }

            contentViewModel.content.observe(viewLifecycleOwner) { items ->
                if (items.isNotEmpty() && items.size > 2) {
                    saveCallScreenPreference("CANCEL", items.first().url.full)
                    saveCallScreenPreference("ANSWER", items.last().url.full)
                }
            }

            setupCallScreen.setOnClickListener {
                withSafeContext { ctx ->
                    val telecomManager =
                        ctx.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                    val isDefault = telecomManager.defaultDialerPackage == ctx.packageName

                    if (isDefault) {
                        Toast.makeText(ctx, "App is already default dialer", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        checkAndRequestPermissions(ctx)
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions(ctx: Context) {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            requestPermissions(missingPermissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            // Permissions granted, request default dialer role
            requestDefaultDialer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                requestDefaultDialer()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Required permissions not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun requestDefaultDialer() {
        val ctx = requireContext()
        val telecomManager = ctx.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        println("requestDefaultDialer:${telecomManager.defaultDialerPackage == ctx.packageName}")
        if (telecomManager.defaultDialerPackage == ctx.packageName) {
            Toast.makeText(ctx, "App is already default dialer", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
            putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, ctx.packageName)
        }
        startActivityForResult(intent, REQUEST_CODE_ROLE_DIALER)
    }

    private fun saveCallScreenPreference(tag: String, value: String) {
        val prefs = requireContext().getSharedPreferences("callscreen_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(tag, value).apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("REQUEST_CODE_ROLE_DIALER: $requestCode")
        if (requestCode == REQUEST_CODE_ROLE_DIALER) {
            val telecomManager =
                requireContext().getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val isDefault = telecomManager.defaultDialerPackage == requireContext().packageName
            if (isDefault) {
                Toast.makeText(requireContext(), "App is now default dialer", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "App is NOT default dialer", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
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
        fun newInstance() = CallScreenFragment().apply { }
        const val REQUEST_CODE_PERMISSIONS = 101
        val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.MANAGE_OWN_CALLS
        )
        const val REQUEST_CODE_ROLE_DIALER = 1001
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