package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.databinding.ItemCategoriesBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.R

class CategoryAdapter(private val onClickListener: (Category) -> Unit): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private val allCategories : MutableList<Category> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        context = parent.context
        return CategoryViewHolder(
            ItemCategoriesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<Category>) {
        allCategories.clear()
        allCategories.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =allCategories.size

    inner class CategoryViewHolder(private val binding: ItemCategoriesBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
           val category = allCategories[position]
            binding.apply {
                categoryName.text = category.name
                println("ringTone: ${category.thumbnail}")

                category.thumbnail?.url?.full.let {
                    Glide.with(context).load(it).placeholder(R.drawable.icon_default_category).error(
                        R.drawable.icon_default_category).into(binding.defaultBg)
                }

                root.setOnClickListener {
                    onClickListener(category)
                }

            }
        }
    }
}