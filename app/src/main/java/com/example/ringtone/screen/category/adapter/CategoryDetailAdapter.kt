package com.example.ringtone.screen.category.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemBigCategoryBinding
import com.example.ringtone.remote.model.Category

class CategoryDetailAdapter(private val onClickListener: (Int) -> Unit): RecyclerView.Adapter<CategoryDetailAdapter.CategoryViewHolder>() {
    private val allCategories : MutableList<Category> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        context = parent.context
        return CategoryViewHolder(
            ItemBigCategoryBinding.inflate(
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

    inner class CategoryViewHolder(private val binding: ItemBigCategoryBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val category = allCategories[position]
            binding.apply {
                categoryName.text = category.name
                val subtitle = if(category.contentCount <= 1)context.getString(R.string.ringTone) else context.getString(R.string.ringTones)
                categoryCount.text = "${category.contentCount}".plus(" $subtitle")
                println("ringTone: ${category.thumbnail}")
                category.thumbnail?.url?.full.let {
                    Glide.with(context).load(it).placeholder(R.drawable.icon_default_category).error(
                        R.drawable.icon_default_category).into(binding.categoryAvatar)
                }

                root.setOnClickListener {
                    onClickListener(category.id)
                }
            }
        }
    }
}