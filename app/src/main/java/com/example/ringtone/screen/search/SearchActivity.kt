package com.example.ringtone.screen.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.ringtone.R
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.viewmodel.RingtoneViewModel
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate){
//    private var ignoreTextChange = false
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val ringtoneTrendingAdapter : TrendingAdapter by lazy {
        TrendingAdapter()
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
                }

                override fun afterTextChanged(s: Editable?) {
                    // After text has changed
//                    if (ignoreTextChange) return
                    val input = s.toString()
                    if(input.isEmpty()) {
                        trendingTitle.visible()
                        trendingIcon.visible()
                        trendingRecyclerView.visible()
                    } else {
                        trendingTitle.gone()
                        trendingIcon.gone()
                        trendingRecyclerView.gone()
                    }
                }
            })
        }
        ringtoneViewModel.trending.observe(this) { items ->
            ringtoneTrendingAdapter.submitList(items)
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
    }

    override fun getItemCount() = items.size
}