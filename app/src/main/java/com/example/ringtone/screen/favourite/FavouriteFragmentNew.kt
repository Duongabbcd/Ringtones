package com.example.ringtone.screen.favourite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ringtone.R
import com.example.ringtone.databinding.ItemFavouriteBinding
import com.example.ringtone.databinding.ViewpagerFavouriteItempageBinding
import com.example.ringtone.remote.model.Category
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.screen.intro.IntroFragmentNew.CallbackIntro
import com.example.ringtone.screen.intro.IntroFragmentNew.Companion.setSpannableString
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class FavouriteFragmentNew : Fragment() {
    private val binding by lazy { ViewpagerFavouriteItempageBinding.inflate(layoutInflater) }
    private lateinit var callbackIntro: CallbackIntro
    private var position = 0

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val favouriteAdapter: SelectingFavouriteAdapter by lazy {
        SelectingFavouriteAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity is CallbackIntro) callbackIntro = activity as CallbackIntro
        position = arguments?.getInt(ARG_POSITION) ?: 0

        val ctx = context ?: return
        val layoutManager = GridLayoutManager(ctx, 3)
        binding.allFavourite.layoutManager = layoutManager
        binding.allFavourite.adapter = favouriteAdapter

        if (arguments != null) {
            fragmentPosition2()
        }

        binding.nextBtn.setOnClickListener {
            val nextPage = position++
            callbackIntro.onNext(position,nextPage)

        }
    }

    private fun fragmentPosition2() {
        when (position) {
            0 -> {
                categoryViewModel.loadRingtoneCategories()
                setUiIntro1()
            }

            1 -> {
                categoryViewModel.loadWallpaperCategories()
                setUiIntro2()
            }

        }
    }

    private fun setUiIntro1() {
        val first = getString(R.string.fav_1)
        val highlight1 = getString(R.string.fav_light_1)
        setSpannableString(first,highlight1,  binding.description)
        binding.slideDot.setImageResource(R.drawable.first_favourite)


        categoryViewModel.ringtoneCategory.observe(viewLifecycleOwner) { items ->
            favouriteAdapter.submitList(items)
        }

    }

    private fun setUiIntro2() {
        val first = getString(R.string.fav_2)
        val highlight1 = getString(R.string.fav_light_2)
        setSpannableString(first,highlight1,  binding.description)
        binding.slideDot.setImageResource(R.drawable.second_favourite)

        categoryViewModel.wallpaperCategory.observe(viewLifecycleOwner) { items ->
            favouriteAdapter.submitList(items)
        }

    }

    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int): FavouriteFragmentNew {
            val fragment = FavouriteFragmentNew()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
}

class SelectingFavouriteAdapter(): RecyclerView.Adapter<SelectingFavouriteAdapter.SelectingFavouriteViewHolder>() {
    private val alLCategories : MutableList<Category> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectingFavouriteViewHolder {
        context = parent.context
        return SelectingFavouriteViewHolder(
            ItemFavouriteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    private lateinit var context: Context

    override fun onBindViewHolder(
        holder: SelectingFavouriteViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    fun submitList(list: List<Category>) {
        alLCategories.clear()
        alLCategories.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = alLCategories.size

    inner class SelectingFavouriteViewHolder(private val binding: ItemFavouriteBinding )  : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            val category = alLCategories[position]
            binding.apply {
                category.thumbnail?.url?.full.let {
                    Glide.with(context).load(it).placeholder(R.drawable.icon_default_category).error(
                        R.drawable.icon_default_category).into(binding.categoryAvatar)
                }

                categoryName.text = category.name

            }
        }
    }

}

