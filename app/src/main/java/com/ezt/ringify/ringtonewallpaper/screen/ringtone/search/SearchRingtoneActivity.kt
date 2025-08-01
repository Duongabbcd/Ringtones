package com.ezt.ringify.ringtonewallpaper.screen.ringtone.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applovin.impl.sdk.AppLovinBroadcastManager
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ActivitySearchRingtoneBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlin.toString
import com.ezt.ringify.ringtonewallpaper.utils.Utils.hideKeyBoard
import kotlin.getValue

@AndroidEntryPoint
class SearchRingtoneActivity : BaseActivity<ActivitySearchRingtoneBinding>(ActivitySearchRingtoneBinding::inflate){
//    private var ignoreTextChange = false
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val ringtoneTrendingAdapter : TrendingAdapter by lazy {
        TrendingAdapter()
    }

    private val ringToneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter { ringTone ->
            RingtonePlayerRemote.setCurrentRingtone(ringTone)
            startActivity(Intent(this, RingtoneActivity::class.java))
        }
    }
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private var inputText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply{
            backBtn.setOnClickListener {
                finish()
            }
            connectionViewModel.isConnectedLiveData.observe(this@SearchRingtoneActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            trendingRecyclerView.adapter = ringtoneTrendingAdapter
            val layoutManager = FlexboxLayoutManager(this@SearchRingtoneActivity).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            trendingRecyclerView.layoutManager = layoutManager

            allResults.adapter = ringToneAdapter


            searchText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                    // Before text is changed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Text is changing
                    val searchText = s.toString()
                    inputText = searchText
                    ringtoneViewModel.searchRingtonesByName(inputText)
                    ringtoneViewModel.search.observe(this@SearchRingtoneActivity) { result ->
                        if(result.isEmpty()) {
                            noDataLayout.visible()
                            allResults.gone()
                        } else {

                            noDataLayout.gone()
                            allResults.visible()
                            ringToneAdapter.submitList1(result)
                        }

                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // After text has changed
//                    if (ignoreTextChange) return
                    val input = s.toString()
                    displayByCondition(input)
                }
            })

            searchText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    this@SearchRingtoneActivity.hideKeyBoard(binding.searchText)
                    // Do something with the search query
                    // For example: performSearch(query)
                    true // consume the action
                } else {
                    false
                }
            }

            allResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        this@SearchRingtoneActivity.hideKeyBoard(binding.searchText)
                    }
                }
            })

            closeButton.setOnClickListener {
                searchText.setText("")
                displayByCondition("")
            }

        }
        
        ringtoneViewModel.trending.observe(this) { items ->
            ringtoneTrendingAdapter.submitList(items)
        }


    }

    private fun displayByCondition(input: String) {
        binding.apply {
            if(input.isEmpty()) {
                trendingTitle.visible()
                trendingIcon.visible()
                trendingRecyclerView.visible()

                allResults.gone()
                closeButton.gone()
            } else {
                trendingTitle.gone()
                trendingIcon.gone()
                trendingRecyclerView.gone()

                allResults.visible()
                closeButton.visible()
            }
        }

    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            ringtoneViewModel.loadTrending()
            binding.noInternet.root.gone()
        }
    }
}

class TrendingAdapter() : RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder>() {
    private lateinit var context: Context
    private val items : MutableList<Ringtone> = mutableListOf()
    inner class TrendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagText: TextView = itemView.findViewById(R.id.tagText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trending, parent, false)
        return TrendingViewHolder(view)
    }

    fun submitList(list: List<Ringtone>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        val ringTone = items[position]
        holder.tagText.text = ringTone.name
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, RingtoneActivity::class.java).apply {
                RingtonePlayerRemote.setRingtoneQueue(listOf(ringTone))
                RingtonePlayerRemote.setCurrentRingtone(ringTone)
            })
        }
    }



    override fun getItemCount() = items.size
}