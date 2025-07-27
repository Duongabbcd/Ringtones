package com.ezt.ringify.ringtonewallpaper.screen.ringtone

import android.os.Bundle
import androidx.activity.viewModels
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityFilteredCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteRingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.RingtoneCategoryActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilteredRingtonesActivity : BaseActivity<ActivityFilteredCategoryBinding>(
    ActivityFilteredCategoryBinding::inflate
) {
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val favourite: FavouriteRingtoneViewModel by viewModels()
    private val ringtoneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter()
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
        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            displayItems()
            binding.noInternet.root.gone()
        }
    }

    private fun displayItems() {
        binding.apply {
            noDataLayout.visible()
            allCategories.gone()
            when(categoryId) {
                -100 -> {
                    ringtoneViewModel.loadPopular(sortOrder)
                    nameScreen.text = getString(R.string.popular)
                    ringtoneViewModel.popular.observe(this@FilteredRingtonesActivity) { items ->
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
                        ringtoneAdapter.submitList(items)
                    }
                }


                else -> {
                    nameScreen.text = categoryName ?: getString(R.string.unknown_cat)
                    ringtoneViewModel.loadSelectedRingtones(categoryId, sortOrder)
                    ringtoneViewModel.selectedRingtone.observe(this@FilteredRingtonesActivity) { items ->
                        ringtoneAdapter.submitList(items)
                    }
                }
            }
        }
    }
}