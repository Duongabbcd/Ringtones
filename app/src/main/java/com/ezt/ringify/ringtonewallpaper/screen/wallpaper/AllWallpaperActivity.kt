package com.ezt.ringify.ringtonewallpaper.screen.wallpaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager
import com.ezt.ringify.ringtonewallpaper.ads.AdsManager.BANNER_HOME
import com.ezt.ringify.ringtonewallpaper.ads.RemoteConfig
import com.ezt.ringify.ringtonewallpaper.ads.new.InterAds
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityAllWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ItemCategoryWallpaperBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.screen.home.MainActivity
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.search.SearchRingtoneActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.PreviewWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.adapter.WallpaperAdapter
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.premium.PremiumWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.Utils.formatWithComma

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllWallpaperActivity: BaseActivity<ActivityAllWallpaperBinding>(ActivityAllWallpaperBinding::inflate) {
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val categoryWallpaperAdapter: CategoryWallpaperAdapter by lazy {
        CategoryWallpaperAdapter { category->
            startActivity(Intent(this, PreviewWallpaperActivity::class.java).apply {
                println("AllWallpaperActivity: $category")
                putExtra("wallpaperCategoryId", category.id)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.apply {
            backBtn.setOnClickListener {
                SearchRingtoneActivity.backToScreen(this@AllWallpaperActivity, "INTER_WALLPAPER")
            }

            connectionViewModel.isConnectedLiveData.observe(this@AllWallpaperActivity) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            allCategories.adapter = categoryWallpaperAdapter
            categoryViewModel.wallpaperCategory.observe(this@AllWallpaperActivity) { categories ->
                categoryWallpaperAdapter.submitList(categories)
                // trigger wallpaper loading for each category
                categories.filter { it.id != 75 }.forEach {
                    categoryViewModel.loadWallpapersByCategory(it.id, categoryWallpaperAdapter)
                }
            }

            categoryViewModel.loading.observe(this@AllWallpaperActivity) { isLoading ->
                binding.loadingCategories.isVisible = isLoading
            }


            categoryViewModel.wallpapersMap.observe(this@AllWallpaperActivity) { map ->
                categoryWallpaperAdapter.submitWallpapersMap(map)
            }
        }

    }

    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            categoryViewModel.loadWallpaperCategories()
            loadMoreData()
            binding.noInternet.root.gone()
        }
    }

    private fun loadMoreData() {
        binding.apply {
            allCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    val isAtBottom =
                        firstVisibleItemPosition + visibleItemCount >= totalItemCount - 5
                    if (isAtBottom) {
                        categoryViewModel.loadWallpaperCategories()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        InterAds.preloadInterAds(this, InterAds.ALIAS_INTER_WALLPAPER, InterAds.INTER_WALLPAPER)
        MainActivity.loadBanner(this, BANNER_HOME)
    }

    override fun onBackPressed() {
        SearchRingtoneActivity.backToScreen(this@AllWallpaperActivity, "INTER_WALLPAPER")

    }
}

class CategoryWallpaperAdapter(
    private val onClickListener: (Category) -> Unit
) : RecyclerView.Adapter<CategoryWallpaperAdapter.CategoryWallpaperViewHolder>() {

    private val allCategories: MutableList<Category> = mutableListOf()
    private val wallpapersMap = mutableMapOf<Int, List<Wallpaper>>()
    private val loadingCategoryIds = mutableSetOf<Int>()
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryWallpaperViewHolder {
        context = parent.context
        val binding = ItemCategoryWallpaperBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return CategoryWallpaperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryWallpaperViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = allCategories.size

    fun submitList(categories: List<Category>) {
        allCategories.clear()
        allCategories.addAll(categories.filter { it.id != 75 })
        notifyDataSetChanged()
    }

    fun submitWallpapersMap(map: Map<Int, List<Wallpaper>>) {
        wallpapersMap.clear()
        wallpapersMap.putAll(map)

        map.keys.forEach { item ->
            println("submitWallpapersMap: $item and ${map[item]?.firstOrNull()}")
        }
        // Notify only updated categories to redraw their wallpaper lists
        map.keys.forEach { categoryId ->
            val pos = allCategories.indexOfFirst { it.id == categoryId }
            if (pos != -1) notifyItemChanged(pos)
        }
    }

    fun setCategoryLoading(categoryId: Int, isLoading: Boolean) {
        if (isLoading) loadingCategoryIds.add(categoryId)
        else loadingCategoryIds.remove(categoryId)

        val pos = allCategories.indexOfFirst { it.id == categoryId }
        if (pos != -1) notifyItemChanged(pos)
    }

    inner class CategoryWallpaperViewHolder(
        private val binding: ItemCategoryWallpaperBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Create wallpaper adapter once per ViewHolder to prevent recycling issues
        private val wallpaperAdapter = WallpaperAdapter {
            val category = allCategories.getOrNull(adapterPosition) ?: return@WallpaperAdapter
            onClickListener(category)
        }

        init {
            binding.allTrending.apply {
                adapter = wallpaperAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
            }
        }

        fun bind(position: Int) {
            val category = allCategories[position]
            val data = wallpapersMap[category.id]?.take(5)
            println("CategoryWallpaperViewHolder: ${category.name} and ${data?.first()}")

            val wallpapers = data ?: emptyList()
            val isLoading = loadingCategoryIds.contains(category.id)

            binding.apply {
                trending.text = category.name
                trendingCount.text = category.contentCount.formatWithComma()
                trendingCount.gone()

                if (isLoading) {
                    allTrending.isEnabled = false
                    progressBar.gone()
                    wallpaperAdapter.submitList(List(5) { Wallpaper.EMPTY_WALLPAPER })
                } else {
                    allTrending.isEnabled = true
                    progressBar.gone()
                    wallpaperAdapter.submitList(wallpapers)
                }

                seeAll.setOnClickListener {
                    onClickListener(category)
                }
            }
        }
    }
}
