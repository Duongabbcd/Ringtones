package com.ezt.ringify.ringtonewallpaper.screen.wallpaper

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityPreviewWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.GridWallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.FavouriteWallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.PreviewLiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium.PremiumWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.search.SearchWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PreviewWallpaperActivity : BaseActivity<ActivityPreviewWallpaperBinding>(ActivityPreviewWallpaperBinding::inflate){
    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private val favourite: FavouriteWallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val wallpaperAdapter: GridWallpaperAdapter by lazy {
        GridWallpaperAdapter({
            println("Wallpaper: $it")
            startActivity(Intent(this@PreviewWallpaperActivity, SlideWallpaperActivity::class.java))
        }).apply {
            onAllImagesLoaded = {
                // Safely post notifyDataSetChanged on RecyclerView's message queue
                binding.allCategories.post {
                    notifyDataSetChanged()
                }
            }
        }
    }

    private val categoryId by lazy {
        intent.getIntExtra("categoryId", -1)
    }
    private val type by lazy {
        intent.getIntExtra("type", 1)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("PreviewWallpaperActivity: $categoryId")
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            connectionViewModel.isConnectedLiveData.observe(this@PreviewWallpaperActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            val layoutManager = GridLayoutManager(this@PreviewWallpaperActivity, 3)

            allCategories.layoutManager = layoutManager
            allCategories.adapter = wallpaperAdapter

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
       binding.apply {
           allCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
               override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                   super.onScrolled(recyclerView, dx, dy)
                   val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                   val visibleItemCount = layoutManager.childCount
                   val totalItemCount = layoutManager.itemCount
                   val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                   val isAtBottom = firstVisibleItemPosition + visibleItemCount >= totalItemCount - 5

                   if(isAtBottom) {
                       when(categoryId) {
                           -3 -> {
                               nameScreen.text = resources.getString(R.string.favourite)
                               favourite.loadAllWallpapers()
                           }

                           -2 -> {
                               nameScreen.text = resources.getString(R.string.trending)
                               wallPaperViewModel.loadTrendingWallpapers()
                           }

                           -1 -> {
                               nameScreen.text = resources.getString(R.string.new_wallpaper)
                               wallPaperViewModel.loadNewWallpapers()
                           }

                           else -> {
                               if (categoryId == 75) {
                                   when (type) {
                                       2 -> {
                                           wallPaperViewModel.loadSlideWallpaper()
                                       }

                                       3 -> {
                                           wallPaperViewModel.loadSingleWallpaper()

                                       }

                                       else -> {
                                           wallPaperViewModel.loadPremiumVideoWallpaper()
                                       }
                                   }

                               }

                               println("category: $categoryId")
                               categoryViewModel.getCategoryByName(categoryId = categoryId)
                               wallPaperViewModel.loadSubWallpapers1(categoryId)

                           }
                       }
                   }
               }
           })
       }
    }


    private fun displayItems() {
        binding.apply {
            allCategories.visible()
            when(categoryId) {
                -3 -> {
                    nameScreen.text = resources.getString(R.string.favourite)
                    favourite.loadAllWallpapers()
                    favourite.allWallpapers.observe(this@PreviewWallpaperActivity){ items ->
                        if(items.isEmpty()) {
                            allCategories.gone()
                            noDataLayout.visible()
                            return@observe
                        }
                        wallpaperAdapter.submitList(items)
                    }
                }

                -2 -> {
                    nameScreen.text = resources.getString(R.string.trending)
                    wallPaperViewModel.loadTrendingWallpapers()
                    wallPaperViewModel.trendingWallpaper.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items)
                    }
                }

                -1 -> {
                    nameScreen.text = resources.getString(R.string.new_wallpaper)
                    wallPaperViewModel.loadNewWallpapers()
                    wallPaperViewModel.newWallpaper.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items)
                    }
                }


                else -> {
                    if (categoryId == 75) {
                        when (type) {
                            2 -> {
                                wallPaperViewModel.loadSlideWallpaper()
                                wallPaperViewModel.slideWallpaper.observe(this@PreviewWallpaperActivity) { items ->
                                    wallpaperAdapter.submitList(items, premium = categoryId == 75)
                                }
                            }

                            3 -> {
                                wallPaperViewModel.loadSingleWallpaper()
                                wallPaperViewModel.singleWallpapers.observe(this@PreviewWallpaperActivity) { items ->
                                    wallpaperAdapter.submitList(items, premium = categoryId == 75)
                                }
                            }

                            else -> {
                                wallPaperViewModel.loadPremiumVideoWallpaper()
                                wallPaperViewModel.premiumWallpapers.observe(this@PreviewWallpaperActivity) { items ->
                                    wallpaperAdapter.submitList(items, premium = categoryId == 75)
                                }
                                return@apply
                            }
                        }

                    }

                    println("category: $categoryId")
                    categoryViewModel.getCategoryByName(categoryId = categoryId)
                    categoryViewModel.category.observe(this@PreviewWallpaperActivity){ category ->
                        nameScreen.text = category.name
                    }



                    wallPaperViewModel.loadSubWallpapers1(categoryId)
                    wallPaperViewModel.subWallpaper1.observe(this@PreviewWallpaperActivity){ items ->
                        wallpaperAdapter.submitList(items, premium = categoryId == 75)
                    }
                }
            }
        }
    }
}