package com.ezt.ringify.ringtonewallpaper.screen.ringtone

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFilteredCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.RingtoneCategoryActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilteredRingtonesActivity : BaseActivity<ActivityFilteredCategoryBinding>(
    ActivityFilteredCategoryBinding::inflate
) {
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val favourite: FavouriteRingtoneViewModel by viewModels()
    private val ringtoneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter { ringTone ->
            RingtonePlayerRemote.setCurrentRingtone(ringTone)
            startActivity(Intent(this, RingtoneActivity::class.java).apply {
                putExtra("categoryId", categoryId)
            })
        }
    }

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -100)
    }

    private val categoryName by lazy {
        intent.getStringExtra("categoryName")
    }

    private lateinit var sortOrder: String

    private val connectionViewModel: InternetConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortOrder = Common.getSortOrder(this)
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            connectionViewModel.isConnectedLiveData.observe(this@FilteredRingtonesActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }


            println("category: $categoryId")
            allCategories.adapter = ringtoneAdapter

            sortIcon.setOnClickListener {
                val dialog = SortBottomSheet(this@FilteredRingtonesActivity) { string ->
                    Common.setSortOrder(this@FilteredRingtonesActivity, string)
                    sortOrder = Common.getSortOrder(this@FilteredRingtonesActivity)
                    displayItems()
                }
                dialog.show()
            }

            ringtoneViewModel.total.observe(this@FilteredRingtonesActivity) { number ->
                binding.nameScreen.text = number.toString()
            }
        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            displayItems()
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }

    private fun loadMoreData() {
        binding.allCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                val isAtBottom = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 5
                if (isAtBottom) {
                    when(categoryId) {
                        -100 -> {
                            ringtoneViewModel.loadPopular(sortOrder)
                            binding.nameScreen.text = getString(R.string.popular)
                        }
                        -99 -> {
                            favourite.loadAllRingtones()
                            binding.nameScreen.text = getString(R.string.favourite)
                        }
                        else ->{
                            binding.nameScreen.text = categoryName ?: getString(R.string.unknown_cat)
                            ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                        }
                    }
                }
            }
        })

    }

    private fun displayItems() {
        binding.apply {
            when(categoryId) {
                -100 -> {
                    ringtoneViewModel.loadPopular(sortOrder)
                    nameScreen.text = getString(R.string.popular)
                    ringtoneViewModel.popular.observe(this@FilteredRingtonesActivity) { items ->
                        if(items.isEmpty()) {
                            noDataLayout.gone()
                            allCategories.visible()
                            return@observe
                        }
                        binding.allCategories.visible()
                        binding.noDataLayout.gone()
                        ringtoneAdapter.submitList(items)
                    }
                }

                -99 -> {
                    favourite.loadAllRingtones()
                    nameScreen.text = getString(R.string.favourite)
                    favourite.allRingtones.observe(this@FilteredRingtonesActivity) { items ->
                        if(items.isEmpty()) {
                            noDataLayout.gone()
                            allCategories.visible()
                            return@observe
                        }
                        binding.allCategories.visible()
                        binding.noDataLayout.gone()
                        ringtoneAdapter.submitList(items)
                    }
                }

                else -> {
                    nameScreen.text = categoryName ?: getString(R.string.unknown_cat)
                    ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                    ringtoneViewModel.selectedRingtone.observe(this@FilteredRingtonesActivity) { items ->
                        if(items.isEmpty()) {
                            binding.noDataLayout.visible()
                            binding.allCategories.gone()
                            return@observe
                        }
                        binding.allCategories.visible()
                        binding.noDataLayout.gone()
                        ringtoneAdapter.submitList(items)
                    }
                }
            }
        }
    }
}