package com.example.ringtone.screen.home.subscreen.first_screen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ringtone.databinding.ItemCategoriesBinding
import com.example.ringtone.remote.model.Category
import com.example.ringtone.R

class CategoryAdapter: RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
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
           val ringTone = allCategories[position]
            binding.apply {
                categoryName.text = ringTone.name
                println("ringTone: ${ringTone.thumbnail}")

                ringTone.thumbnail?.url?.full.let {
                    Glide.with(context).load(it).placeholder(R.drawable.icon_default_category).error(
                        R.drawable.icon_default_category).into(binding.defaultBg)
                }

            }
        }
    }
}