package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.callscreen

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallScreenFragment :
    BaseFragment<FragmentCallscreenBinding>(FragmentCallscreenBinding::inflate) {
    private val callScreenViewModel: CallScreenViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by activityViewModels()

    private val callScreenAdapter: CallScreenAdapter by lazy {
        CallScreenAdapter { result ->
            currentCallScreen = result
            displayCallScreen()
        }
    }

    private fun displayCallScreen() {
        val ctx = context ?: return
        val url = currentCallScreen.thumbnail.url.medium
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
                callScreenAdapter.submitList(items)
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