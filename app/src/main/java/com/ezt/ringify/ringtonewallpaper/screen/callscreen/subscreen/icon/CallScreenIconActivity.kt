package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.icon

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallscreenIconBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentBackgroundBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemContentIconBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.ContentViewModel
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity.Companion.avatarUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.background.CallScreenBackgroundActivity.Companion.videoUrl
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview.PreviewCallScreenActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallScreenIconActivity : BaseActivity<ActivityCallscreenIconBinding>(ActivityCallscreenIconBinding::inflate){

    private val contentViewModel: ContentViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    private val allBackgroundAdapter: AllIconAdapter by lazy {
        AllIconAdapter { input->
            println("AllBackgroundAdapter: $input")
            if(input.isNullOrEmpty()) {
                return@AllIconAdapter
            }
            avatarUrl = input
            displayBackground(input)
        }
    }


    private fun displayBackground(input: String) {
        Glide.with(this@CallScreenIconActivity).load(input)
            .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
            .into(binding.avatar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }

        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        videoUrl = prefs.getString("BACKGROUND", "") ?: ""
        println("videoUrl: $videoUrl")
        avatarUrl = prefs.getString("AVATAR", "") ?: ""
        println("videoUrl: $avatarUrl")

        binding.apply {
            Glide.with(this@CallScreenIconActivity).load(videoUrl)
                .placeholder(R.drawable.default_callscreen).error(R.drawable.default_callscreen)
                .into(binding.currentCallScreen)

            backBtn.setOnClickListener {
                finish()
            }
            allBackground.adapter = allBackgroundAdapter
            allBackground.layoutManager =
                LinearLayoutManager(
                    this@CallScreenIconActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            displayBackground(avatarUrl)

            contentViewModel.backgroundContent.observe(this@CallScreenIconActivity) {items ->
                allBackgroundAdapter.submitList(items)
            }

            previewBtn.setOnClickListener {
                startActivity(Intent(this@CallScreenIconActivity, PreviewCallScreenActivity::class.java))
            }

            applyBtn.setOnClickListener {
                saveCallScreenPreference("AVATAR", avatarUrl)
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
            contentViewModel.getAllCallScreenIcons()
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
                    contentViewModel.getAllCallScreenIcons()
                }
            }

        })
    }
}

class AllIconAdapter(private val onClickListener: (String) -> Unit) : RecyclerView.Adapter<AllIconAdapter.AllIconViewHolder>() {
    private val allBackgrounds : MutableList<ImageContent> = mutableListOf()
    private lateinit var context: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllIconViewHolder {
        context = parent.context
        val binding = ItemContentIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllIconViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AllIconViewHolder,
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

    inner class AllIconViewHolder(private val binding: ItemContentIconBinding) : RecyclerView.ViewHolder(binding.root) {
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