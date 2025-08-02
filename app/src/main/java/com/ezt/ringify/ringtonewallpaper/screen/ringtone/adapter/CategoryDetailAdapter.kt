package com.ezt.ringify.ringtonewallpaper.screen.ringtone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemBigCategoryBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote

class CategoryDetailAdapter(private val onClickListener: (Category) -> Unit): RecyclerView.Adapter<CategoryDetailAdapter.CategoryViewHolder>() {
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
        println("list: $list")
        val start = allCategories.size
        allCategories.addAll(list)

        notifyItemRangeInserted(start, list.size)
    }

    override fun getItemCount(): Int =allCategories.size

    inner class CategoryViewHolder(private val binding: ItemBigCategoryBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val category = allCategories[position]
            binding.apply {

                val subtitle = if(category.contentCount <= 1)context.getString(R.string.ringTone) else context.getString(R.string.ringTones)
                categoryCount.text = "${category.contentCount}".plus(" $subtitle")
                categoryCount.gone()
                println("ringTone: ${category.thumbnail}")

                if(category.id == -99) {
                    categoryName.text = context.getString(R.string.favourite)
                    Glide.with(context).load(R.drawable.icon_fav_category).into(binding.categoryAvatar)
                } else {
                    categoryName.text = category.name
                    category.thumbnail?.url?.full.let {
                        Glide.with(context).load(it).placeholder(R.drawable.icon_default_category).error(
                            R.drawable.icon_default_category).into(binding.categoryAvatar)
                    }
                }

                root.setOnClickListener {
                    onClickListener(category)
                }
            }
        }
    }
}