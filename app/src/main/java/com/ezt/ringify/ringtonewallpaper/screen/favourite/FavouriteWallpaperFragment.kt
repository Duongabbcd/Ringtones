package com.ezt.ringify.ringtonewallpaper.screen.favourite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.ViewpagerFavouriteItempageBinding
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew.CallbackIntro
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew.Companion.setSpannableString
import com.ezt.ringify.ringtonewallpaper.utils.Common
import com.iab.omid.library.mmadbridge.walking.async.a
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class FavouriteWallpaperFragment : Fragment() {
    private val binding by lazy { ViewpagerFavouriteItempageBinding.inflate(layoutInflater) }
    private lateinit var callbackIntro: CallbackIntro
    private var position = 0

    private val categoryViewModel: CategoryViewModel by viewModels()

    private var allFavWallpaper: MutableList<Int> = mutableListOf()
    private val wallpaperAdapter: SelectingFavouriteAdapter by lazy {
        SelectingFavouriteAdapter { items ->
            allFavWallpaper.clear()
            allFavWallpaper.addAll(items)

            println("wallpaperAdapter: $allFavWallpaper")
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
            setUiIntro2()
        }

        binding.nextBtn.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            Common.setAllFavouriteWallpaper(ctx, allFavWallpaper)

            val nextPage = position++
            callbackIntro.onNext(position,nextPage)

        }
    }

    private fun setUiIntro2() {
        val first = getString(R.string.fav_2)
        val highlight2 = getString(R.string.fav_light_2)
        setSpannableString(first,listOf(highlight2),  binding.description)
        binding.slideDot.setImageResource(R.drawable.second_favourite)
        binding.allFavourite.adapter = wallpaperAdapter
        categoryViewModel.loadWallpaperCategories()
        categoryViewModel.wallpaperCategory.observe(viewLifecycleOwner) { items ->
            items.onEach {
                println("setUiIntro2: $it")
            }
            wallpaperAdapter.submitList(items)
        }

    }

    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int): FavouriteWallpaperFragment {
            val fragment = FavouriteWallpaperFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
}