package com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ezt.ringify.ringtonewallpaper.base.BaseFragment
import com.ezt.ringify.ringtonewallpaper.databinding.FragmentRingtoneBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.CategoryViewModel
import com.ezt.ringify.ringtonewallpaper.remote.viewmodel.RingtoneViewModel
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.RingtoneCategoryActivity
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.CategoryAdapter
import com.ezt.ringify.ringtonewallpaper.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.FilteredRingtonesActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import com.ezt.ringify.ringtonewallpaper.utils.RingtonePlayerRemote
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenItem
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.RingtoneActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingtoneFragment: BaseFragment<FragmentRingtoneBinding>(FragmentRingtoneBinding::inflate) {
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private val connectionViewModel: InternetConnectionViewModel by activityViewModels()

    private val categoryAdapter : CategoryAdapter by lazy {
        CategoryAdapter { category ->
            val ctx = context ?: return@CategoryAdapter
            ctx.startActivity(Intent(ctx, FilteredRingtonesActivity::class.java).apply {
                putExtra("categoryId", category.id)
                putExtra("categoryName", category.name)
            })
        }
    }

    private val ringToneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter { ringTone ->
            RingtonePlayerRemote.setCurrentRingtone(ringTone)
            val ctx = context ?: return@RingtoneAdapter
            ctx.startActivity(Intent(ctx, RingtoneActivity::class.java))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }


            allCategories.adapter = categoryAdapter
            allPopular.adapter = ringToneAdapter

            withSafeContext { ctx ->
                allCategories.layoutManager = GridLayoutManager(ctx, 2)
            }

            openAll1.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, RingtoneCategoryActivity::class.java))
                }
            }

            openAll2.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, FilteredRingtonesActivity::class.java))
                }
            }

            categoryViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    val loadingItems1 = List(6) {
                        Category.EMPTY_CATEGORY
                    }
                    categoryAdapter.submitList(loadingItems1)

                    requireActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                } else {
                    displayNormalData()
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }

            }

            noInternet.tryAgain.setOnClickListener {
                withSafeContext { ctx ->
                    val connected = connectionViewModel.isConnectedLiveData.value ?: false
                    if (connected) {
                        origin.visible()
                        noInternet.root.visibility = View.VISIBLE
                        // Maybe reload your data
                    } else {
                        Toast.makeText(
                            ctx,
                            R.string.no_connection,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            newRingtones.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, FilteredRingtonesActivity::class.java).apply {
                        putExtra("categoryId", -101)
                    }
                    )
                }
            }

            weeklyTrending.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, FilteredRingtonesActivity::class.java).apply {
                        putExtra("categoryId", -102)
                    }
                    )
                }
            }

            editorChoice.setOnClickListener {
                withSafeContext { ctx ->
                    startActivity(Intent(ctx, FilteredRingtonesActivity::class.java).apply {
                        putExtra("categoryId", -103)
                    }
                    )
                }


            }
        }
    }

    private fun displayNormalData() {
        binding.apply {
            categoryViewModel.ringtoneCategory.observe(viewLifecycleOwner) { items ->
                withSafeContext { ctx ->
                    val initialFav = Common.getAllFavouriteGenres(ctx)  // List<Int>
                    val favRingtones = initialFav.mapNotNull { id ->
                        items.find { it.id == id }
                    }.toMutableList()
                    val number = 6 - favRingtones.size
                    println("ringtoneCategory: $favRingtones")
                    favRingtones.addAll(
                        items.filterNot { favRingtones.contains(it) }.take(number)
                    )
                    categoryAdapter.submitList(favRingtones)
                }
            }

            ringtoneViewModel.popular.observe(viewLifecycleOwner) { items ->
                RingtonePlayerRemote.setRingtoneQueue(items)
                ringToneAdapter.submitList(items.take(5))
            }
        }
    }


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            categoryViewModel.loadRingtoneCategories()
            ringtoneViewModel.loadPopular()
            binding.noInternet.root.gone()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = RingtoneFragment().apply { }

    }

}