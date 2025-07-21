package com.example.ringtone.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentRingtoneBinding
import com.example.ringtone.databinding.FragmentRingtoneBinding.inflate
import com.example.ringtone.databinding.FragmentWallpaperBinding
import com.example.ringtone.remote.viewmodel.WallpaperViewModel
import com.example.ringtone.screen.wallpaper.adapter.WallpaperAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WallpaperFragment: BaseFragment<FragmentWallpaperBinding>(FragmentWallpaperBinding::inflate) {

    private val wallPaperViewModel : WallpaperViewModel by viewModels()

    private val wallPaperAdapter : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
        }
    }
     private val newWallpaperAdapter : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
        }
    }
    private val subWallpaperAdapter1 : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
        }
    }
    private val subWallpaperAdapter2 : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
        }
    }

    private val subWallpaperAdapter3 : WallpaperAdapter by lazy {
        WallpaperAdapter{
            println("Wallpaper: $it")
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
                wallPaperAdapter.submitList(items)
            }

            wallPaperViewModel.total1.observe(viewLifecycleOwner) { number ->
                trendingCount.text = "$number"
            }

            trendingCount.text = wallPaperViewModel.total1.toString()

            wallPaperViewModel.newWallpaper.observe(viewLifecycleOwner) {items ->
                newWallpaperAdapter.submitList(items)
            }

            wallPaperViewModel.total2.observe(viewLifecycleOwner) { number ->
                newWallpaperCount.text = "$number"
            }


            wallPaperViewModel.subWallpaper1.observe(viewLifecycleOwner) {items ->
                subWallpaperAdapter1.submitList(items)
            }


            wallPaperViewModel.total3.observe(viewLifecycleOwner) { number ->
                sub1Count.text = "$number"
            }


            wallPaperViewModel.subWallpaper2.observe(viewLifecycleOwner) {items ->
                subWallpaperAdapter2.submitList(items)
            }

            wallPaperViewModel.total4.observe(viewLifecycleOwner) { number ->
                sub2Count.text = "$number"
            }



            wallPaperViewModel.subWallpaper3.observe(viewLifecycleOwner) {items ->
                subWallpaperAdapter3.submitList(items)
            }

            wallPaperViewModel.total5.observe(viewLifecycleOwner) { number ->
                sub3Count.text = "$number"
            }

        }

    }


    companion object {
        @JvmStatic
        fun newInstance() = WallpaperFragment().apply { }

    }

}