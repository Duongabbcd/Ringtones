package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.api.SearchRequest
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



    fun loadTrendingWallpapers() = viewModelScope.launch {
        _loading1.value = true
        try {
            val result = repository.fetchTrendingWallpapers()
            _total1.value = result.data.total
            _trendingWallpaper.value = result.data.data
            result.data.data.apply {
                this.onEach {
               println("loadTrendingWallpapers: $it")
                }
            }
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }


    fun loadNewWallpapers() = viewModelScope.launch {
        _loading2.value = true
        try {
            val result = repository.fetchNewWallpapers()
            _total2.value = result.data.total
            _newWallpaper.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
        }
    }

    fun loadSubWallpapers1(pageId: Int) = viewModelScope.launch {
        _loading3.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId)
            _total3.value = result.data.total
            _subWallpaper1.value = result.data.data
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
        _loading4.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId)
            _total4.value= result.data.total
            _subWallpaper2.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading4.value = false
        }
    }

    fun loadSubWallpapers3(pageId: Int) = viewModelScope.launch {
        _loading5.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId)
            _total5.value = result.data.total
            _subWallpaper3.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading5.value = false
        }
    }

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

    fun loadLiveWallpapers() = viewModelScope.launch {
        _loading1.value = true
        try {
            val result = repository.getLiveWallpaper()
            _liveWallpapers.value = result.data.data

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }


    fun loadPremiumWallpaper() = viewModelScope.launch {
        _loading1.value = true
        try {
            val result = repository.getPremiumWallpaper()
            _premiumWallpapers.value = result.data.data

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }
}