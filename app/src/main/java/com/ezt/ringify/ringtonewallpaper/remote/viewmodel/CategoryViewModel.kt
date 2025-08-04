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
    val allWallpapers1 = mutableListOf<Category>()

    private var _categoryName1 = MutableLiveData<String>()
    val categoryName1: LiveData<String> = _categoryName1

    private var _categoryName2 = MutableLiveData<String>()
    val categoryName2: LiveData<String> = _categoryName2

    private var _categoryName3 = MutableLiveData<String>()
    val categoryName3: LiveData<String> = _categoryName3


    // Store wallpapers per category id
    private val _wallpapersMap = MutableLiveData<Map<Int, List<Wallpaper>>>()
    val wallpapersMap: LiveData<Map<Int, List<Wallpaper>>> get() = _wallpapersMap

    private val wallpaperCache = mutableMapOf<Int, MutableList<Wallpaper>>() // prevent duplicate calls

    fun loadRingtoneCategories() = viewModelScope.launch {
        if (!hasMorePages1 || _loading.value == true) return@launch
        _loading.value = true
        try {
            val result = repository.fetchRingtoneCategories(currentPage1)

            hasMorePages1 = result.dataPage.nextPageUrl != null
            currentPage1++
            allWallpapers1.addAll(result.dataPage.categories)
            _ringtoneCategory.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadWallpaperCategories() = viewModelScope.launch {
        if (!hasMorePages1 || _loading.value == true) return@launch
        _loading.value = true
        try {
            val result = repository.fetchAllWallpaperCategories(currentPage1)
            hasMorePages1 = result.dataPage.nextPageUrl != null
            currentPage1++
            println("loadWallpaperCategories 123: ${result.dataPage.nextPageUrl}")
            allWallpapers1.addAll(result.dataPage.categories)
            _wallpaperCategory.value = allWallpapers1
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

    fun getFirstCategory(categoryId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.getCategoryById(categoryId)
            _categoryName1.value = result.dataPage.categories.first().name

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun getSecondCategory(categoryId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.getCategoryById(categoryId)
            _categoryName2.value = result.dataPage.categories.first().name

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun getThirdCategory(categoryId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.getCategoryById(categoryId)
            _categoryName3.value = result.dataPage.categories.first().name

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    private val currentPageMap = mutableMapOf<Int, Int>()
    private val hasMorePagesMap = mutableMapOf<Int, Boolean>()
    private val loadingCategories = mutableSetOf<Int>()

    fun loadWallpapersByCategory(categoryId: Int, adapter: CategoryWallpaperAdapter) {
        // If already cached or loading, skip
        if (wallpaperCache.containsKey(categoryId) || loadingCategories.contains(categoryId)) return

        adapter.setCategoryLoading(categoryId, true)
        loadingCategories.add(categoryId)

        viewModelScope.launch {
            try {
                var page = currentPageMap[categoryId] ?: 1
                var hasMorePages = hasMorePagesMap[categoryId] ?: true
                val wallpapersForCategory =
                    wallpaperCache[categoryId]?.toMutableList() ?: mutableListOf()

                while (hasMorePages) {
                    val result =
                        repository.fetchWallpaperByCategory(categoryId = categoryId, page = page)
                    wallpapersForCategory.addAll(result.data.data)

                    hasMorePages = result.data.nextPageUrl != null
                    page++

                    // Update pagination maps for this category
                    currentPageMap[categoryId] = page
                    hasMorePagesMap[categoryId] = hasMorePages
                }

                // Cache the full list for the category
                wallpaperCache[categoryId] = wallpapersForCategory
                _wallpapersMap.value = wallpaperCache.toMap()
                _error.value = null

            } catch (e: Exception) {
                Log.e("ViewModel", "Failed: ${e.message}")
                _error.value = e.message
            } finally {
                loadingCategories.remove(categoryId)
                adapter.setCategoryLoading(categoryId, false)
            }
        }
    }


}