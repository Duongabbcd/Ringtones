package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.CategoryWallpaperAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _ringtoneCategory = MutableLiveData<List<Category>>()
    val ringtoneCategory: LiveData<List<Category>> = _ringtoneCategory

    private val _wallpaperCategory = MutableLiveData<List<Category>>()
    val wallpaperCategory: LiveData<List<Category>> = _wallpaperCategory

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage1 = 1
    private var hasMorePages1 = true
    val allWallpapers1 = mutableListOf<Wallpaper>()

    // Store wallpapers per category id
    private val _wallpapersMap = MutableLiveData<Map<Int, List<Wallpaper>>>()
    val wallpapersMap: LiveData<Map<Int, List<Wallpaper>>> get() = _wallpapersMap

    private val wallpaperCache = mutableMapOf<Int, MutableList<Wallpaper>>() // prevent duplicate calls

    fun loadRingtoneCategories() = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchRingtoneCategories()
            _ringtoneCategory.value = result.dataPage.categories
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadWallpaperCategories() = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchAllWallpaperCategories()
            result.dataPage.categories.onEach {
                println("fetchWallpaperCategories: $it")
            }

            _wallpaperCategory.value = result.dataPage.categories.take(12)
            _error.value = null
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun getCategoryByName(categoryId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.getCategoryById(categoryId)
            _category.value = result.dataPage.categories.first()

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }



    fun loadWallpapersByCategory(categoryId: Int, adapter: CategoryWallpaperAdapter) {
        if (wallpaperCache.containsKey(categoryId)) return

        adapter.setCategoryLoading(categoryId, true)

        viewModelScope.launch {
            try {
                if (!hasMorePages1 || _loading.value ==  true ) return@launch
                _loading.value = true
                val result = repository.fetchWallpaperByCategory(categoryId = categoryId, page = currentPage1)
                hasMorePages1 = result.data.nextPageUrl != null
                currentPage1++

                wallpaperCache[categoryId]?.addAll(result.data.data)
                _wallpapersMap.value = wallpaperCache.toMap()

                _error.value = null
            } catch (e: Exception) {
                Log.e("ViewModel", "Failed: ${e.message}")
            } finally {
                _loading.value = false
                adapter.setCategoryLoading(categoryId, false)
            }
        }
    }

}