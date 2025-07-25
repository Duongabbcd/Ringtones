package com.example.ringtone.screen.home.subscreen.wallpaper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentWallpaperBinding
import com.example.ringtone.remote.connection.InternetConnectionViewModel
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.wallpaper.AllWallpaperActivity
import com.example.ringtone.screen.wallpaper.PreviewWallpaperActivity
import com.example.ringtone.screen.wallpaper.adapter.WallpaperAdapter
import com.example.ringtone.screen.wallpaper.live.LiveWallpaperActivity
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import com.example.ringtone.utils.Utils.formatWithComma
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class WallpaperFragment :
    BaseFragment<FragmentWallpaperBinding>(FragmentWallpaperBinding::inflate) {

    private val wallPaperViewModel: WallpaperViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by activityViewModels()

    private val wallPaperAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", -2)
                })
            }
        }
    }
    private val newWallpaperAdapter: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", -1)
                })
            }
        }
    }
    private val subWallpaperAdapter1: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 30)
                })
            }
        }
    }
    private val subWallpaperAdapter2: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 31)
                })
            }
        }
    }

    private val subWallpaperAdapter3: WallpaperAdapter by lazy {
        WallpaperAdapter {
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 32)
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
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
                wallPaperAdapter.submitList(items.take(10))
            }

            wallPaperViewModel.total1.observe(viewLifecycleOwner) { number ->
                trendingCount.text = number.formatWithComma()
            }

            wallPaperViewModel.newWallpaper.observe(viewLifecycleOwner) { items ->
                newWallpaperAdapter.submitList(items.take(10))
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
                        putExtra("categoryId", -2)
                    })
                }
            }
            openAll2.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("categoryId", -1)
                    })
                }
            }

            openAll3.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("categoryId", 30)
                    })
                }
            }
            openAll4.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("categoryId", 31)
                    })
                }
            }

            openAll5.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("categoryId", 32)
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

            premium.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("categoryId", 75)
                    })
                }
            }

            categories.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, AllWallpaperActivity::class.java))
                }
            }

            favourite.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                        putExtra("categoryId", -3)
                    })
                }
            }

            live.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, LiveWallpaperActivity::class.java))
                }
            }

            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            binding.noInternet.tryAgain.setOnClickListener {
                // Optionally trigger a manual refresh of data or recheck
                val connected = connectionViewModel.isConnectedLiveData.value ?: false
                if (connected) {
                    // Do something if reconnected
                    binding.origin.visible()
                    binding.noInternet.root.visibility = View.VISIBLE
                    // Maybe reload your data
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Still no internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
            wallPaperViewModel.loadSubWallpapers1(30)
            wallPaperViewModel.loadSubWallpapers2(31)
            wallPaperViewModel.loadSubWallpapers3(32)
            binding.noInternet.root.gone()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = WallpaperFragment().apply { }

    }

}