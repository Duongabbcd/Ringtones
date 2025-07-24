package com.example.ringtone.screen.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentWallpaperBinding
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.wallpaper.AllWallpaperActivity
import com.example.ringtone.screen.wallpaper.PreviewWallpaperActivity
import com.example.ringtone.screen.wallpaper.adapter.WallpaperAdapter
import com.example.ringtone.screen.wallpaper.live.LiveWallpaperActivity
import com.example.ringtone.utils.Utils.formatWithComma
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WallpaperFragment: BaseFragment<FragmentWallpaperBinding>(FragmentWallpaperBinding::inflate) {

    private val wallPaperViewModel : WallpaperViewModel by viewModels()

    private val wallPaperAdapter : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", -2)
                })
            }
        }
    }
     private val newWallpaperAdapter : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", -1)
                })
            }
        }
    }
    private val subWallpaperAdapter1 : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 30)
                })
            }
        }
    }
    private val subWallpaperAdapter2 : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
            withSafeContext { ctx ->
                startActivity(Intent(ctx, PreviewWallpaperActivity::class.java).apply {
                    putExtra("categoryId", 31)
                })
            }
        }
    }

    private val subWallpaperAdapter3 : WallpaperAdapter by lazy {
        WallpaperAdapter{
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
        wallPaperViewModel.loadTrendingWallpapers()
        wallPaperViewModel.loadNewWallpapers()
        wallPaperViewModel.loadSubWallpapers1(30)
        wallPaperViewModel.loadSubWallpapers2(31)
        wallPaperViewModel.loadSubWallpapers3(32)

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

            trendingCount.text = wallPaperViewModel.total1.toString()

            wallPaperViewModel.newWallpaper.observe(viewLifecycleOwner) {items ->
                newWallpaperAdapter.submitList(items.take(10))
            }

            wallPaperViewModel.total2.observe(viewLifecycleOwner) { number ->
                newWallpaperCount.text = number.formatWithComma()
            }


            wallPaperViewModel.subWallpaper1.observe(viewLifecycleOwner) {items ->
                subWallpaperAdapter1.submitList(items)
            }


            wallPaperViewModel.total3.observe(viewLifecycleOwner) { number ->
                sub1Count.text = number.formatWithComma()
            }


            wallPaperViewModel.subWallpaper2.observe(viewLifecycleOwner) {items ->
                subWallpaperAdapter2.submitList(items)
            }

            wallPaperViewModel.total4.observe(viewLifecycleOwner) { number ->
                sub2Count.text = number.formatWithComma()
            }



            wallPaperViewModel.subWallpaper3.observe(viewLifecycleOwner) {items ->
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


            wallPaperViewModel.loading.observe(viewLifecycleOwner) {
                loading1.isVisible = it
                loading2.isVisible = it
                loading3.isVisible = it
                loading4.isVisible = it
                loading5.isVisible = it
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

            live.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, LiveWallpaperActivity::class.java))
                }
            }
        }

    }


    companion object {
        @JvmStatic
        fun newInstance() = WallpaperFragment().apply { }

    }

}