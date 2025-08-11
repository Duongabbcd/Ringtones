package com.ezt.ringify.ringtonewallpaper.screen.favourite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ItemFavouriteBinding
import com.ezt.ringify.ringtonewallpaper.databinding.ViewpagerFavouriteItempageBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew.CallbackIntro
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew.Companion.setSpannableString
import com.ezt.ringify.ringtonewallpaper.utils.Common
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class FavouriteRingtoneFragment : Fragment() {
    private val binding by lazy { ViewpagerFavouriteItempageBinding.inflate(layoutInflater) }
    private lateinit var callbackIntro: CallbackIntro
    private var position = 0

    private val categoryViewModel: CategoryViewModel by viewModels()
    private var allFavRingtones: MutableList<Int> = mutableListOf()
    private val ringtoneAdapter: SelectingFavouriteAdapter by lazy {
        SelectingFavouriteAdapter { list ->
            allFavRingtones.clear()
            allFavRingtones.addAll(list)

            println("ringtoneAdapter: $allFavRingtones")
            binding.currentItem.text = "(${allFavRingtones.size}/3)"
        }
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


        if (arguments != null) {
            setUiIntro1()
        }

        binding.nextBtn.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            Common.setAllFavouriteGenres(ctx, allFavRingtones)
            val nextPage = position++
            callbackIntro.onNext(position, nextPage)

        }

        binding.skipBtn.setOnClickListener {
            callbackIntro.onNext(1, 2)
        }
    }


    private fun setUiIntro1() {
        val first = getString(R.string.fav_1)
        val highlight1 = getString(R.string.fav_light_1)
        setSpannableString(first, listOf(highlight1), binding.description)
        binding.slideDot.setImageResource(R.drawable.first_favourite)
        binding.allFavourite.adapter = ringtoneAdapter
        categoryViewModel.loadRingtoneCategories()
        categoryViewModel.ringtoneCategory.observe(viewLifecycleOwner) { items ->
            ringtoneAdapter.submitList(items)
        }

    }


    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int): FavouriteRingtoneFragment {
            val fragment = FavouriteRingtoneFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
}

class SelectingFavouriteAdapter(private val onSelectionChanged: (List<Int>) -> Unit) :
    RecyclerView.Adapter<SelectingFavouriteAdapter.SelectingFavouriteViewHolder>() {
    private val allCategories: MutableList<Category> = mutableListOf()
    private val selectedCategories: MutableList<Int> = mutableListOf()
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
        allCategories.clear()
        allCategories.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = allCategories.size

    inner class SelectingFavouriteViewHolder(private val binding: ItemFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val category = allCategories[position]
            binding.apply {
                category.thumbnail?.url?.medium.let {
                    Glide.with(context).load(it).placeholder(R.drawable.icon_default_category)
                        .error(
                            R.drawable.icon_default_category
                        ).into(binding.categoryAvatar)
                }

                categoryName.text = category.name
                updateUI(category.id)

                root.setOnClickListener {
                    val clickedId = category.id

                    if (selectedCategories.contains(clickedId)) {
                        // ✅ Already selected → remove
                        selectedCategories.remove(clickedId)
                        notifyItemChanged(position)
                        onSelectionChanged(selectedCategories)
                        return@setOnClickListener
                    }

                    // ✅ Not selected → only add if size < 3
                    if (selectedCategories.size < 3) {
                        selectedCategories.add(clickedId)
                        notifyItemChanged(position)
                        onSelectionChanged(selectedCategories)
                    } else {
                        // ❌ Do nothing or show a toast
                        Toast.makeText(context, "You can select up to 3 only", Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }

        private fun updateUI(categoryId: Int) {
            val isSelected = selectedCategories.contains(categoryId)
            binding.root.setBackgroundResource(
                if (isSelected)
                    R.drawable.background_radius_12_purple_solid
                else
                    R.drawable.background_radius_12_light_gray
            )

            val textColor =
                context.resources.getColor(if (isSelected) R.color.white else R.color.customBlack)
            binding.categoryName.setTextColor(textColor)
        }
    }

}

