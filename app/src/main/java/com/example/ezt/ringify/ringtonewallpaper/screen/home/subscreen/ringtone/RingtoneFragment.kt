package com.example.ringtone.screen.home.subscreen.ringtone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentRingtoneBinding
import com.example.ringtone.remote.connection.InternetConnectionViewModel
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.remote.viewmodel.RingtoneViewModel
import com.example.ringtone.screen.ringtone.RingtoneCategoryActivity
import com.example.ringtone.screen.home.subscreen.ringtone.adapter.CategoryAdapter
import com.example.ringtone.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import com.example.ringtone.screen.ringtone.FilteredRingtonesActivity
import com.example.ringtone.utils.Common.gone
import com.example.ringtone.utils.Common.visible
import com.example.ringtone.utils.RingtonePlayerRemote
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
        RingtoneAdapter(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            allCategories.adapter = categoryAdapter
            allPopular.adapter = ringToneAdapter
            withSafeContext { ctx ->
                allCategories.layoutManager = GridLayoutManager(ctx, 2)
            }

            openAll1.setOnClickListener {
                withSafeContext {  ctx ->
                    startActivity(Intent(ctx, RingtoneCategoryActivity::class.java))
                }
            }

            openAll2.setOnClickListener {
                withSafeContext {  ctx ->
                    startActivity(Intent(ctx, FilteredRingtonesActivity::class.java))
                }
            }

            categoryViewModel.ringtoneCategory.observe(viewLifecycleOwner) { items ->
                categoryAdapter.submitList(items.take(6))
            }

            ringtoneViewModel.popular.observe(viewLifecycleOwner) { items ->
                RingtonePlayerRemote.setRingtoneQueue(items)
                ringToneAdapter.submitList(items.take(5))
            }

            categoryViewModel.loading.observe(viewLifecycleOwner) {
                loading1.isVisible = it
                loading2.isVisible = it
            }

            connectionViewModel.isConnectedLiveData.observe(viewLifecycleOwner) { isConnected ->
                println("isConnected: $isConnected")
                checkInternetConnected(isConnected)
            }

            binding.noInternet.tryAgain.setOnClickListener {
                // Optionally trigger a manual refresh of data or recheck
                val connected = connectionViewModel.isConnectedLiveData.value ?: false
                if (connected) {
                    // Do something if reconnected
                    binding.origin.visible()
                    binding.noInternet.root.visibility = View.VISIBLE
                    // Maybe reload your data
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Still no internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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