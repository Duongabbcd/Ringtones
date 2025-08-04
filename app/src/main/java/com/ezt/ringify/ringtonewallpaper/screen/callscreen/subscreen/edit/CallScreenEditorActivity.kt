package com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.edit

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCallscreenBackgroundBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.ContentViewModel
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.preview.PreviewCallScreenActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue
import androidx.core.content.edit
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter.AllAvatarAdapter
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter.AllBackgroundAdapter
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.adapter.AllIConAdapter
import com.ezt.ringify.ringtonewallpaper.screen.callscreen.subscreen.alert.CallScreenAlertActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity

@AndroidEntryPoint
class CallScreenEditorActivity :
    BaseActivity<ActivityCallscreenBackgroundBinding>(ActivityCallscreenBackgroundBinding::inflate) {
    private val contentViewModel: ContentViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val allBackgroundAdapter: AllBackgroundAdapter by lazy {
        AllBackgroundAdapter { input ->
            println("AllBackgroundAdapter: $input")
            if (input.isNullOrEmpty()) {
                return@AllBackgroundAdapter
            }
            backgroundUrl = input
            displayBackground(input)
        }
    }

    private val allAvatarAdapter: AllAvatarAdapter by lazy {
        AllAvatarAdapter { input ->
            println("AllBackgroundAdapter: $input")
            if (input.isNullOrEmpty()) {
                return@AllAvatarAdapter
            }
            avatarUrl = input
            displayAvatar(input)
        }
    }

    private val allIconAdapter: AllIConAdapter by lazy {
        AllIConAdapter { end, start ->
            println("AllBackgroundAdapter: $end and $start")
            if (end.isEmpty() || start.isEmpty()) {
                return@AllIConAdapter
            }
            endCall = end
            startCall = start
            displayIcon(end, start)
        }
    }

    private val editorType by lazy {
        intent.getIntExtra("editorType", 0)
    }


    private fun displayBackground(input: String) {
        Glide.with(this@CallScreenEditorActivity).load(input)
            .placeholder(R.drawable.default_callscreen).error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)
    }

    private fun displayAvatar(input: String) {
        Glide.with(this@CallScreenEditorActivity).load(input)
            .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
            .into(binding.defaultAvatar)
    }

    private fun displayIcon(end: String, start: String) {
        Glide.with(this@CallScreenEditorActivity).load(end)
            .placeholder(R.drawable.icon_end_call).error(R.drawable.icon_end_call)
            .into(binding.end)
        Glide.with(this@CallScreenEditorActivity).load(start)
            .placeholder(R.drawable.icon_start_call).error(R.drawable.icon_start_call)
            .into(binding.start)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("callscreen_prefs", MODE_PRIVATE)
        val background = prefs.getString("BACKGROUND", "") ?: ""
        if (background.isNotEmpty()) {
            backgroundUrl = background
        }

        val start = prefs.getString("ANSWER", "") ?: ""
        if (start.isNotEmpty()) {
            startCall = start
        }
        val end = prefs.getString("CANCEL", "") ?: ""
        if (end.isNotEmpty()) {
            endCall = end
        }
        val avatar = prefs.getString("AVATAR", "") ?: ""
        if (avatar.isNotEmpty()) {
            avatarUrl = avatar
        }

        Glide.with(this@CallScreenEditorActivity).load(backgroundUrl)
            .placeholder(R.drawable.default_callscreen).error(R.drawable.default_callscreen)
            .into(binding.currentCallScreen)

        Glide.with(this@CallScreenEditorActivity).load(avatarUrl)
            .placeholder(R.drawable.default_cs_avt).error(R.drawable.default_cs_avt)
            .into(binding.defaultAvatar)

        Glide.with(this@CallScreenEditorActivity).load(startCall)
            .placeholder(R.drawable.icon_end_call).error(R.drawable.icon_end_call)
            .into(binding.start)

        Glide.with(this@CallScreenEditorActivity).load(endCall)
            .placeholder(R.drawable.icon_start_call).error(R.drawable.icon_start_call)
            .into(binding.end)

        connectionViewModel.isConnectedLiveData.observe(this) { isConnected ->
            checkInternetConnected(isConnected)
        }
        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(
                    this@CallScreenEditorActivity,
                    "INTER_CALLSCREEN"
                )
            }

            when (editorType) {
                1 -> {
                    allBackground.adapter = allBackgroundAdapter
                    editorScreenName.text = getString(R.string.background)
                }

                2 -> {
                    allBackground.adapter = allAvatarAdapter
                    editorScreenName.text = getString(R.string.avatar)
                }

                3 -> {
                    allBackground.adapter = allIconAdapter
                    editorScreenName.text = getString(R.string.icon)
                }

                else -> {
                    allBackground.adapter = allBackgroundAdapter
                    editorScreenName.text = getString(R.string.background)
                }
            }



            allBackground.layoutManager =
                LinearLayoutManager(
                    this@CallScreenEditorActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            displayBackground(backgroundUrl)

            contentViewModel.backgroundContent.observe(this@CallScreenEditorActivity) { items ->
                println("allBackgroundAdapter: $items")
                when (editorType) {
                    1 -> allBackgroundAdapter.submitList(items)
                    2 -> allAvatarAdapter.submitList(items)
                    else -> allBackgroundAdapter.submitList(items)
                }

            }

            contentViewModel.iconContent.observe(this@CallScreenEditorActivity) { items ->
                allIconAdapter.submitList(items)
            }


            previewBtn.setOnClickListener {
                startActivity(
                    Intent(
                        this@CallScreenEditorActivity,
                        PreviewCallScreenActivity::class.java
                    )
                )
            }

            applyBtn.setOnClickListener {
                saveCallScreenPreference("BACKGROUND", backgroundUrl)
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
            when (editorType) {
                1 -> contentViewModel.getAllCallScreenBackgrounds()
                2 -> contentViewModel.getAllCallScreenAvatars()
                3 -> contentViewModel.getAllCallScreenIcons()
            }

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
                    when (editorType) {
                        1 -> contentViewModel.getAllCallScreenBackgrounds()
                        2 -> contentViewModel.getAllCallScreenAvatars()
                        3 -> contentViewModel.getAllCallScreenIcons()
                    }
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if (RemoteConfig.BANNER_COLLAP_ALL_070625 != "0") {
            AdsManager.showAdBanner(
                this,
                BANNER_HOME,
                binding.frBanner,
                binding.view,
                isCheckTestDevice = false
            ) {}
        }
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@CallScreenEditorActivity, "INTER_CALLSCREEN")
    }

    companion object {
        var backgroundUrl: String = ""
        var avatarUrl: String = ""

        var endCall: String = ""
        var startCall: String = ""
    }


}