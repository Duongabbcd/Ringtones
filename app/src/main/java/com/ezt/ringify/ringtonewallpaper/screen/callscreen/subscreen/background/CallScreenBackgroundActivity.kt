package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallscreenBackgroundBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentBackgroundBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.ContentViewModel
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview.PreviewCallScreenActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import androidx.core.content.edit

@AndroidEntryPoint
class CallScreenBackgroundActivity: BaseActivity<ActivityCallscreenBackgroundBinding>(ActivityCallscreenBackgroundBinding::inflate) {
    private val contentViewModel: ContentViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val allBackgroundAdapter: AllBackgroundAdapter by lazy {
        AllBackgroundAdapter { input->
            println("AllBackgroundAdapter: $input")
            if(input.isNullOrEmpty()) {
                return@AllBackgroundAdapter
            }
            videoUrl = input
            displayBackground(input)
        }
    }

    private fun displayBackground(input: String) {
        Glide.with(this@CallScreenBackgroundActivity).load(input)
            .placeholder(R.drawable.default_callscreen).error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        videoUrl = prefs.getString("BACKGROUND", "") ?: ""
        println("videoUrl: $videoUrl")

        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
            allBackground.adapter = allBackgroundAdapter
            allBackground.layoutManager =
                LinearLayoutManager(
                    this@CallScreenBackgroundActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            displayBackground(videoUrl)

            contentViewModel.backgroundContent.observe(this@CallScreenBackgroundActivity) {items ->
                allBackgroundAdapter.submitList(items)
            }


            previewBtn.setOnClickListener {
                startActivity(Intent(this@CallScreenBackgroundActivity, PreviewCallScreenActivity::class.java))
            }

            applyBtn.setOnClickListener {
                saveCallScreenPreference("BACKGROUND", videoUrl)
            }
        }
    }

    private fun saveCallScreenPreference(tag: String, value: String) {
        println("saveCallScreenPreference: $tag and $value")
        val prefs = this.getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        prefs.edit { putString(tag, value) }
    }

    private fun checkInternetConnected(isConnected: Boolean) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            contentViewModel.getAllCallScreenBackgrounds()
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }


    private var isLoadingMore = false
    private fun loadMoreData() {
        binding.allBackground.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx <= 0) return  // Only when scrolling right
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isAtBottom = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 2
                if (isAtBottom && !isLoadingMore) {
                    isLoadingMore = true
                    contentViewModel.getAllCallScreenBackgrounds()
                }
            }

        })
    }

    companion object {
        var videoUrl: String = ""
        var avatarUrl: String = ""

        var endCall: String = ""
        var startCall: String = ""
    }


}


class AllBackgroundAdapter(private val onClickListener: (String) -> Unit) : RecyclerView.Adapter<AllBackgroundAdapter.AllBackgroundViewHolder>() {
    private val allBackgrounds : MutableList<ImageContent> = mutableListOf()
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllBackgroundViewHolder {
        context = parent.context
        val binding = ItemContentBackgroundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllBackgroundViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AllBackgroundViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<ImageContent>) {
        allBackgrounds.clear()
        allBackgrounds.addAll(list)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int =allBackgrounds.size

    inner class AllBackgroundViewHolder(private val binding: ItemContentBackgroundBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val imageContent = allBackgrounds[position]

            binding.apply {
                val input = imageContent.url.medium

                Glide.with(context).load(input).placeholder(R.drawable.default_callscreen)
                    .error(R.drawable.default_callscreen).into(callScreenBackground)

                root.setOnClickListener {
                    println("AllBackgroundViewHolder: $input")
                    onClickListener(input)
                }
            }
        }
    }

}