package com.example.ringtone.screen.home.subscreen.ringtone

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ringtone.base.BaseFragment
import com.example.ringtone.databinding.FragmentRingtoneBinding
import com.example.ringtone.remote.viewmodel.CategoryViewModel
import com.example.ringtone.remote.viewmodel.RingtoneViewModel
import com.example.ringtone.screen.home.subscreen.ringtone.adapter.CategoryAdapter
import com.example.ringtone.screen.home.subscreen.ringtone.adapter.RingtoneAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingtoneFragment: BaseFragment<FragmentRingtoneBinding>(FragmentRingtoneBinding::inflate) {
    private val ringtoneViewModel: RingtoneViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private val categoryAdapter : CategoryAdapter by lazy {
        CategoryAdapter()
    }

    private val ringToneAdapter : RingtoneAdapter by lazy {
        RingtoneAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryViewModel.loadCategories()
        ringtoneViewModel.loadPopular()

        binding.apply {
            allCategories.adapter = categoryAdapter
            allPopular.adapter = ringToneAdapter
            withSafeContext { ctx ->
                allCategories.layoutManager = GridLayoutManager(ctx, 2)
            }

            categoryViewModel.category.observe(viewLifecycleOwner) { items ->
                categoryAdapter.submitList(items.take(6))
            }

            ringtoneViewModel.popular.observe(viewLifecycleOwner) { items ->
                ringToneAdapter.submitList(items.take(2))
            }

            categoryViewModel.loading.observe(viewLifecycleOwner) {
                loading1.isVisible = it
            }
            ringtoneViewModel.loading.observe(viewLifecycleOwner) {
                loading2.isVisible = it
            }

            openAll1.setOnClickListener {

            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = RingtoneFragment().apply { }

    }

}