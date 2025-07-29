package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.api.SearchRequest
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.model.Tag
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.model.WallpaperResponse
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _wallPaper = MutableLiveData<WallpaperResponse>()
    val wallPaper: LiveData<WallpaperResponse> = _wallPaper

    private val _trendingWallpaper = MutableLiveData<List<Wallpaper>>()
    val trendingWallpaper: LiveData<List<Wallpaper>> = _trendingWallpaper

    private val _newWallpaper = MutableLiveData<List<Wallpaper>>()
    val newWallpaper: LiveData<List<Wallpaper>> = _newWallpaper

    private val _subWallpaper1 = MutableLiveData<List<Wallpaper>>()
    val subWallpaper1: LiveData<List<Wallpaper>> = _subWallpaper1

    private val _subWallpaper2 = MutableLiveData<List<Wallpaper>>()
    val subWallpaper2: LiveData<List<Wallpaper>> = _subWallpaper2

    private val _subWallpaper3 = MutableLiveData<List<Wallpaper>>()
    val subWallpaper3: LiveData<List<Wallpaper>> = _subWallpaper3


    private val _tags = MutableLiveData<Tag?>()
    val tags: LiveData<Tag?> = _tags

    private val _searchWallpapers = MutableLiveData<List<Wallpaper>>()
    val searchWallpapers: LiveData<List<Wallpaper>> = _searchWallpapers

    private val _liveWallpapers = MutableLiveData<List<Wallpaper>>()
    val liveWallpapers: LiveData<List<Wallpaper>> = _liveWallpapers

    private val _singleWallpapers = MutableLiveData<List<Wallpaper>>()
    val singleWallpapers: LiveData<List<Wallpaper>> = _singleWallpapers

    private val _slideWallpaper = MutableLiveData<List<Wallpaper>>()
    val slideWallpaper: LiveData<List<Wallpaper>> = _slideWallpaper

    private val _premiumWallpapers = MutableLiveData<List<Wallpaper>>()
    val premiumWallpapers: LiveData<List<Wallpaper>> = _premiumWallpapers

    private var _total1 = MutableLiveData<Int>()
    val total1: LiveData<Int> get() = _total1
    private var _total2 = MutableLiveData<Int>()
    val total2: LiveData<Int> get() = _total2
    private var _total3 = MutableLiveData<Int>()
    val total3: LiveData<Int> get() = _total3
    private var _total4 = MutableLiveData<Int>()
    val total4: LiveData<Int> get() = _total4
    private var _total5 = MutableLiveData<Int>()
    val total5: LiveData<Int> get() = _total5

    private val _loading1 = MutableLiveData<Boolean>()
    val loading1: LiveData<Boolean> = _loading1
    private val _loading2 = MutableLiveData<Boolean>()
    val loading2: LiveData<Boolean> = _loading2
    private val _loading3 = MutableLiveData<Boolean>()
    val loading3: LiveData<Boolean> = _loading3
    private val _loading4 = MutableLiveData<Boolean>()
    val loading4: LiveData<Boolean> = _loading4
    private val _loading5 = MutableLiveData<Boolean>()
    val loading5: LiveData<Boolean> = _loading5

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage1 = 1
    private var hasMorePages1 = true
    private var currentPage2 = 1
    private var hasMorePages2 = true
    private var currentPage3 = 1
    private var hasMorePages3 = true
    private var currentPage4 = 1
    private var hasMorePages4 = true
    private var currentPage5 = 1
    private var hasMorePages5 = true

    val allWallpapers1 = mutableListOf<Wallpaper>()
    val allWallpapers2 = mutableListOf<Wallpaper>()
    val allWallpapers3 = mutableListOf<Wallpaper>()
    val allWallpapers4 = mutableListOf<Wallpaper>()
    val allWallpapers5 = mutableListOf<Wallpaper>()


    fun loadTrendingWallpapers() = viewModelScope.launch {
        if (!hasMorePages1 || _loading1.value ==  true ) return@launch
        _loading1.value = true
        try {
            val result = repository.fetchTrendingWallpapers(currentPage1)
            _total1.value = result.data.total

            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++
            allWallpapers1.addAll(result.data.data)
            _trendingWallpaper.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun loadNewWallpapers() = viewModelScope.launch {
        if (!hasMorePages2 || _loading2.value ==  true ) return@launch
        _loading2.value = true
        try {
            val result = repository.fetchNewWallpapers(currentPage2)
            _total2.value = result.data.total
            hasMorePages2 = result.data.nextPageUrl != null
            currentPage2++
            allWallpapers2.addAll(result.data.data)
            _newWallpaper.value = allWallpapers2
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
        }
    }

    fun loadSubWallpapers1(categoryId: Int) = viewModelScope.launch {
        if (!hasMorePages3 || _loading3.value ==  true ) return@launch
        _loading3.value = true
        try {
            val result = repository.fetchWallpaperByCategory(categoryId, currentPage3)
            _total3.value = result.data.total
            hasMorePages3 = result.data.nextPageUrl != null
            currentPage3++
            allWallpapers3.addAll(result.data.data)
            _subWallpaper1.value = allWallpapers3
            println("loadSubWallpapers1: ${result.data.total}")
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading3.value = false
        }
    }

    fun loadSubWallpapers2(pageId: Int) = viewModelScope.launch {
        if (!hasMorePages4 || _loading4.value ==  true ) return@launch
        _loading4.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId, page = currentPage4)
            _total4.value= result.data.total
            hasMorePages4 = result.data.nextPageUrl != null
            currentPage4++
            allWallpapers4.addAll(result.data.data)
            _subWallpaper2.value = allWallpapers4
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading4.value = false
        }
    }

    fun loadSubWallpapers3(pageId: Int) = viewModelScope.launch {
        if (!hasMorePages5 || _loading5.value ==  true ) return@launch
        _loading5.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId, page=currentPage5)
            _total5.value = result.data.total
            hasMorePages5 = result.data.nextPageUrl != null
            currentPage5++
            allWallpapers5.addAll(result.data.data)
            _subWallpaper3.value = allWallpapers5
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading5.value = false
        }
    }

    fun loadLiveWallpapers() = viewModelScope.launch {
        if (!hasMorePages1 || _loading1.value ==  true ) return@launch
        _loading1.value = true
        try {
            val result = repository.getLiveWallpaper(currentPage1)
            _total1.value = result.data.total
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++
            allWallpapers1.addAll(result.data.data)
            _liveWallpapers.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun loadSlideWallpaper() = viewModelScope.launch {
        if (!hasMorePages2 || _loading2.value ==  true ) return@launch
        _loading2.value = true
        try {
            val result = repository.getSlideWallpaper(currentPage2)
            _total2.value = result.data.total
            hasMorePages2 = result.data.nextPageUrl != null
            currentPage2++

            allWallpapers2.addAll(result.data.data)
            _slideWallpaper.value = allWallpapers2
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
        }
    }

    fun loadSingleWallpaper() = viewModelScope.launch {
        if (!hasMorePages3 || _loading3.value ==  true ) return@launch
        _loading3.value = true
        try {
            val result = repository.getSingleWallpaper(currentPage3)
            _total3.value = result.data.total
            hasMorePages3 = result.data.nextPageUrl != null
            currentPage3++

            allWallpapers3.addAll(result.data.data)
            _singleWallpapers.value = allWallpapers3
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading3.value = false
        }
    }


    //Later
    fun searchTag(searchText: String) = viewModelScope.launch {
        _loading1.value = true
        try {
            _tags.value = null // Clear the old tag first
            val result = repository.searchTag(SearchRequest(searchText))
            println("searchTag: $searchText and $result")
            if (result.data.isEmpty()) {
                _error.value = "No matching tags found"
                _tags.value = null // explicitly notify observers there's no tag
            } else {
                _tags.value = result.data.first() // only set if not empty
                _error.value = null
            }
        } catch (e: Exception) {
            println("searchTag exception: ${e.message}")
            _tags.value = null
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun searchWallpaperByTag(tagId: Int) = viewModelScope.launch {
        _loading1.value = true
        try {
            val result = repository.getWallPapersByTag(tagId)
            _searchWallpapers.value = result.data.data

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun loadPremiumVideoWallpaper() = viewModelScope.launch {
        if (!hasMorePages1 || _loading1.value == true) return@launch
        _loading1.value = true
        try {
            val result = repository.getPremiumVideoWallpaper(currentPage1)
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++

            allWallpapers1.addAll(result.data.data)
            _premiumWallpapers.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

}