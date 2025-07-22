package com.example.ringtone.screen.wallpaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityAllWallpaperBinding
import com.example.ringtone.databinding.ItemCategoryWallpaperBinding
import com.example.ringtone.remote.model.Category
import com.example.ringtone.remote.model.Wallpaper
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.screen.wallpaper.adapter.WallpaperAdapter
import com.example.ringtone.utils.Utils.formatWithComma

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllWallpaperActivity: BaseActivity<ActivityAllWallpaperBinding>(ActivityAllWallpaperBinding::inflate) {

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val categoryWallpaperAdapter: CategoryWallpaperAdapter by lazy {
        CategoryWallpaperAdapter { category->
            startActivity(Intent(this, PreviewWallpaperActivity::class.java).apply {
                println("AllWallpaperActivity: $category")
                putExtra("categoryId", category.id)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryViewModel.loadWallpaperCategories()
        binding.apply {
            allCategories.adapter = categoryWallpaperAdapter
            categoryViewModel.wallpaperCategory.observe(this@AllWallpaperActivity) { categories ->
                categoryWallpaperAdapter.submitList(categories)
                // trigger wallpaper loading for each category
                categories.forEach {
                    categoryViewModel.loadWallpapersByCategory(it.id, categoryWallpaperAdapter)
                }
            }

            categoryViewModel.wallpapersMap.observe(this@AllWallpaperActivity) { map ->
                categoryWallpaperAdapter.submitWallpapersMap(map)
            }
        }

    }
}




class CategoryWallpaperAdapter(private val onClickListener: (Category) -> Unit): RecyclerView.Adapter<CategoryWallpaperAdapter.CategoryWallpaperViewHolder>()
{

    private val allCategories : MutableList<Category> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryWallpaperViewHolder {
        context = parent.context
        return CategoryWallpaperViewHolder(
            ItemCategoryWallpaperBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: CategoryWallpaperViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }


    private val loadingCategoryIds = mutableSetOf<Int>()

    fun setCategoryLoading(categoryId: Int, isLoading: Boolean) {
        if (isLoading) loadingCategoryIds.add(categoryId)
        else loadingCategoryIds.remove(categoryId)
        notifyItemChanged(allCategories.indexOfFirst { it.id == categoryId })
    }

    fun submitWallpapersMap(map: Map<Int, List<Wallpaper>>) {
        map.onEach {
            println("submitWallpapersMap : ${it.key} and ${it.value}")
        }
        wallpapersMap.clear()
       wallpapersMap.putAll(map)
        notifyDataSetChanged()
    }

    fun submitList(list:  List<Category>) {
        println("submitList: ${list.size}")
        allCategories.clear()
        allCategories.addAll(list)
        notifyDataSetChanged()
    }


    private val wallpapersMap = mutableMapOf<Int, List<Wallpaper>>()
    override fun getItemCount(): Int =allCategories.size

    inner class CategoryWallpaperViewHolder(
        private val binding: ItemCategoryWallpaperBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val category = allCategories[position]
            val wallpapers = wallpapersMap[category.id]?.take(10) ?: emptyList()
            val isLoading = loadingCategoryIds.contains(category.id)

            binding.apply {
                trending.text = category.name
                trendingCount.text = category.contentCount.formatWithComma()

                if (isLoading) {
                    allTrending.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                } else {
                    allTrending.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    val wallpaperAdapter = WallpaperAdapter {
                        onClickListener(category)
                    }
                    allTrending.adapter = wallpaperAdapter
                    allTrending.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                    wallpaperAdapter.submitList(wallpapers)
                }

                root.setOnClickListener {
                    onClickListener(category)
                }
            }
        }
    }
}