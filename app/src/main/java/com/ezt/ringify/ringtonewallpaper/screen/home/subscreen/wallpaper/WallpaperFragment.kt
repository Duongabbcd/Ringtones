package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.wallpaper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.LiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.base.BaseFragment
import com.ezt.ringify.ringtonewallpaper.databinding.FragmentWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.WallpaperViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.AllWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.player.SlideWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium.PremiumWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.Utils.formatWithComma
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.favourite.FavouriteWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class WallpaperFragment :
    BaseFragment<FragmentWallpaperBinding>(FragmentWallpaperBinding::inflate) {

    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by activityViewModels()

    private val wallPaperAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, SlideWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", -2)
                })
            }
        }
    }
    private val newWallpaperAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, SlideWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", -1)
                })
            }
        }
    }
    private lateinit var subWallpaperAdapter1: WallpaperAdapter
    private lateinit var subWallpaperAdapter2: WallpaperAdapter
    private lateinit var subWallpaperAdapter3: WallpaperAdapter
    private val initialList: MutableList<Int?> = mutableListOf()
    private var resultList: List<Int> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCustomWallpapers()
        binding.apply {

            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            allTrending.adapter = wallPaperAdapter
            allNewWallpaper.adapter = newWallpaperAdapter
            allSub1.adapter = subWallpaperAdapter1
            allSub2.adapter = subWallpaperAdapter2
            allSub3.adapter = subWallpaperAdapter3
            val ctx = context ?: return@apply
            allTrending.layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
            allNewWallpaper.layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
            allSub1.layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
            allSub2.layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
            allSub3.layoutManager = LinearLayoutManager(ctx, RecyclerView.HORIZONTAL, false)
            wallPaperViewModel.trendingWallpaper.observe(viewLifecycleOwner) { items ->
                wallPaperAdapter.submitList(items)
            }

            wallPaperViewModel.total1.observe(viewLifecycleOwner) { number ->
                trendingCount.text = number.formatWithComma()
            }

            wallPaperViewModel.newWallpaper.observe(viewLifecycleOwner) { items ->
                newWallpaperAdapter.submitList(items)
            }

            wallPaperViewModel.total2.observe(viewLifecycleOwner) { number ->
                newWallpaperCount.text = number.formatWithComma()
            }


            wallPaperViewModel.subWallpaper1.observe(viewLifecycleOwner) { items ->
                subWallpaperAdapter1.submitList(items)
            }


            wallPaperViewModel.total3.observe(viewLifecycleOwner) { number ->
                sub1Count.text = number.formatWithComma()
            }


            wallPaperViewModel.subWallpaper2.observe(viewLifecycleOwner) { items ->
                subWallpaperAdapter2.submitList(items)
            }

            wallPaperViewModel.total4.observe(viewLifecycleOwner) { number ->
                sub2Count.text = number.formatWithComma()
            }



            wallPaperViewModel.subWallpaper3.observe(viewLifecycleOwner) { items ->
                subWallpaperAdapter3.submitList(items)
            }

            wallPaperViewModel.total5.observe(viewLifecycleOwner) { number ->
                sub3Count.text = number.formatWithComma()
            }

            openAll1.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("wallpaperCategoryId", -2)
                    })
                }
            }
            openAll2.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("wallpaperCategoryId", -1)
                    })
                }
            }

            openAll3.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("wallpaperCategoryId", resultList[0])
                    })
                }
            }
            openAll4.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("wallpaperCategoryId", resultList[1])
                    })
                }
            }

            openAll5.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("wallpaperCategoryId", resultList[2])
                    })
                }
            }


            wallPaperViewModel.loading1.observe(viewLifecycleOwner) {
                loading1.isVisible = it
                newWallpaperCount.isVisible = !it

            }

            wallPaperViewModel.loading2.observe(viewLifecycleOwner) {
                loading2.isVisible = it
                trendingCount.isVisible = !it
            }
            wallPaperViewModel.loading3.observe(viewLifecycleOwner) {
                loading3.isVisible = it
                sub1Count.isVisible = !it
            }
            wallPaperViewModel.loading4.observe(viewLifecycleOwner) {
                loading4.isVisible = it
                sub2Count.isVisible = !it
            }
            wallPaperViewModel.loading5.observe(viewLifecycleOwner) {
                loading5.isVisible = it
                sub3Count.isVisible = !it
            }
            categoryViewModel.categoryName1.observe(viewLifecycleOwner) {
                sub1.text = it
            }

            categoryViewModel.categoryName2.observe(viewLifecycleOwner) {
                sub2.text = it
            }

            categoryViewModel.categoryName3.observe(viewLifecycleOwner) {
                sub3.text = it
            }

            premium.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PremiumWallpaperActivity::class.java))
                }
            }

            categories.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, AllWallpaperActivity::class.java))
                }
            }

            favourite.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, FavouriteWallpaperActivity::class.java).apply {
                        putExtra("wallpaperCategoryId", -3)
                    })
                }
            }

            live.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, LiveWallpaperActivity::class.java))
                }
            }

            binding.noInternet.tryAgain.setOnClickListener {
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
        }

    }

    private fun setupCustomWallpapers() {

        withSafeContext { ctx ->
            initialList.addAll(Common.getAllFavouriteWallpaper(ctx))
        }

        val backupList = listOf<Int>(30, 31, 32)
        resultList = if (initialList.isEmpty()) {
            backupList
        } else {
            initialList.mapIndexed { index, value ->
                value ?: backupList.getOrNull(index) ?: -1
            }
        }
        println("setupCustomWallpapers: $resultList")

        subWallpaperAdapter1 = WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, SlideWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", resultList[0])
                })
            }
        }

        subWallpaperAdapter2 = WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, SlideWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", resultList[1])
                })
            }
        }

        subWallpaperAdapter3 = WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, SlideWallpaperActivity::class.java).apply {
                    putExtra("wallpaperCategoryId", resultList[2])
                })
            }
        }
    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            wallPaperViewModel.loadTrendingWallpapers()
            wallPaperViewModel.loadNewWallpapers()
            wallPaperViewModel.loadSubWallpapers1(resultList[0])
            wallPaperViewModel.loadSubWallpapers2(resultList[1])
            wallPaperViewModel.loadSubWallpapers3(resultList[2])

            categoryViewModel.getFirstCategory(resultList[0])
            categoryViewModel.getSecondCategory(resultList[1])
            categoryViewModel.getThirdCategory(resultList[2])
            binding.noInternet.root.gone()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = WallpaperFragment().apply { }

    }

}