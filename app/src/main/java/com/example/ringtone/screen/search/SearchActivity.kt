package com.example.ringtone.screen.search

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
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.ringtone.R
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.viewmodel.RingtoneViewModel
import com.example.ringtone.screen.home.subscreen.first_screen.adapter.RingtoneAdapter
import com.example.ringtone.screen.player.PlayerActivity
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import com.example.ringtone.utils.RingtonePlayerRemote
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlin.toString
import com.example.ringtone.utils.Utils.hideKeyBoard

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate){
//    private var ignoreTextChange = false
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val ringtoneTrendingAdapter : TrendingAdapter by lazy {
        TrendingAdapter()
    }

    private val ringToneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ringtoneViewModel.loadTrending()
        binding.apply{
            backBtn.setOnClickListener {
                finish()
            }

            trendingRecyclerView.adapter = ringtoneTrendingAdapter
            val layoutManager = FlexboxLayoutManager(this@SearchActivity).apply {
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
                    ringtoneViewModel.searchRingtonesByName(searchText)
                    ringtoneViewModel.search.observe(this@SearchActivity) { result ->
                        if(result.isEmpty()) {
                            noDataLayout.visible()
                            allResults.gone()
                        } else {
                            noDataLayout.gone()
                            allResults.visible()
                            ringToneAdapter.submitList(result)
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
                    val query = searchText.text.toString()
                    this@SearchActivity.hideKeyBoard(binding.searchText)
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
                        this@SearchActivity.hideKeyBoard(binding.searchText)
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
}

class TrendingAdapter() : RecyclerView.Adapter<TrendingAdapter.TagViewHolder>() {
    private lateinit var context: Context
    private val items : MutableList<Ringtone> = mutableListOf()
    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tagText: TextView = itemView.findViewById(R.id.tagText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trending, parent, false)
        return TagViewHolder(view)
    }

    fun submitList(list: List<Ringtone>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val ringTone = items[position]
        holder.tagText.text = ringTone.name
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, PlayerActivity::class.java).apply {
                RingtonePlayerRemote.setPlayingQueue(listOf(ringTone))
                RingtonePlayerRemote.setCurrentRingtone(ringTone)
            })
        }
    }

    override fun getItemCount() = items.size
}