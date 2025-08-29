package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _searchWallpapers = MutableLiveData<List<Wallpaper>>()
    val searchWallpapers: LiveData<List<Wallpaper>> = _searchWallpapers

    private val _searchWallpapers1 = MutableLiveData<List<Wallpaper>>()
    val searchWallpapers1: LiveData<List<Wallpaper>> = _searchWallpapers1

    private val _searchWallpapers2 = MutableLiveData<List<Wallpaper>>()
    val searchWallpapers2: LiveData<List<Wallpaper>> = _searchWallpapers2

    private val _searchWallpapers3 = MutableLiveData<List<Wallpaper>>()
    val searchWallpapers3: LiveData<List<Wallpaper>> = _searchWallpapers3

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

    private var currentSortOption: Int = -1
    fun loadTrendingWallpapers(limit: Int = 21) = viewModelScope.launch {
        if (!hasMorePages1 || _loading1.value ==  true ) return@launch
        _loading1.value = true
        try {
            val firstLimit = 2
            val secondLimit = if (limit == 5) 3 else limit
            val result = repository.fetchTrendingWallpapers(
                currentPage1,
                if (limit == 5) firstLimit else limit
            )
            val result2 = repository.fetchTrendingSpecialWallpapers(
                currentPage1,
                if (limit == 5) secondLimit else limit
            )
            _total1.value = result.data.total

            hasMorePages1 = result.data.nextPageUrl != null && result2.data.nextPageUrl != null
            currentPage1++
            allWallpapers1.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            allWallpapers1.addAll(result2.data.data.filter { it.contents.isNotEmpty() })
            _trendingWallpaper.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _trendingWallpaper.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun loadNewWallpapers(limit: Int = 21) = viewModelScope.launch {
        if (!hasMorePages2 || _loading2.value ==  true ) return@launch
        _loading2.value = true
        try {
            val result = repository.fetchNewWallpapers(currentPage2, limit)
            _total2.value = result.data.total
            hasMorePages2 = result.data.nextPageUrl != null
            currentPage2++
            allWallpapers2.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            _newWallpaper.value = allWallpapers2
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _newWallpaper.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
        }
    }

    fun loadSubWallpapers1(categoryId: Int, limit: Int = 21) = viewModelScope.launch {
        if (!hasMorePages3 || _loading3.value ==  true ) return@launch
        _loading3.value = true
        try {
            val result = repository.fetchWallpaperByCategory(categoryId, currentPage3, limit)
            _total3.value = result.data.total
            hasMorePages3 = result.data.nextPageUrl != null
            currentPage3++
            allWallpapers3.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            _subWallpaper1.value = allWallpapers3
            println("loadSubWallpapers1: ${result.data.total}")
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _subWallpaper1.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading3.value = false
        }
    }

    fun loadSubWallpapers2(pageId: Int, limit: Int = 21) = viewModelScope.launch {
        if (!hasMorePages4 || _loading4.value ==  true ) return@launch
        _loading4.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId, page = currentPage4, limit)
            _total4.value= result.data.total
            hasMorePages4 = result.data.nextPageUrl != null
            currentPage4++
            allWallpapers4.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            _subWallpaper2.value = allWallpapers4
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _subWallpaper2.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading4.value = false
        }
    }

    fun loadSubWallpapers3(pageId: Int, limit: Int = 21) = viewModelScope.launch {
        if (!hasMorePages5 || _loading5.value ==  true ) return@launch
        _loading5.value = true
        try {
            val result = repository.fetchWallpaperByCategory(pageId, page = currentPage5, limit)
            _total5.value = result.data.total
            hasMorePages5 = result.data.nextPageUrl != null
            currentPage5++
            allWallpapers5.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            _subWallpaper3.value = allWallpapers5
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _subWallpaper3.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading5.value = false
        }
    }

    fun loadLiveWallpapers(option: Int = 0) = viewModelScope.launch {
        // If new sort option, reset everything
        if (option != currentSortOption) {
            currentSortOption = option
            currentPage1 = 1
            hasMorePages1 = true
            allWallpapers1.clear()
        }

        if (!hasMorePages1 || _loading1.value ==  true ) return@launch
        _loading1.value = true
        try {
            val result = when (option) {
                0 -> repository.getLiveWallpaper(currentPage1)
                1 -> repository.getTrendingLiveWallpaper(currentPage1)
                2 -> repository.getNewLiveWallpaper(currentPage1)
                else -> repository.getLiveWallpaper(currentPage1)
            }
            _total1.value = result.data.total
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++

            allWallpapers1.addAll(result.data.data.filter { it.contents.isNotEmpty() })

            // ✅ Force LiveData to emit a new list instance
            _liveWallpapers.value = allWallpapers1.toList()

            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _liveWallpapers.value = emptyList<Wallpaper>()
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

            allWallpapers2.addAll(result.data.data.filter { it.contents.size > 1 })
            _slideWallpaper.value = allWallpapers2
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _slideWallpaper.value = emptyList<Wallpaper>()
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

            allWallpapers3.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            _singleWallpapers.value = allWallpapers3
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _singleWallpapers.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading3.value = false
        }
    }

    fun loadPremiumVideoWallpaper() = viewModelScope.launch {
        if (!hasMorePages1 || _loading1.value == true) return@launch
        _loading1.value = true
        try {
            val result = repository.getPremiumVideoWallpaper(currentPage1)
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++

            allWallpapers1.addAll(result.data.data.filter { it.contents.isNotEmpty() })
            _premiumWallpapers.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _premiumWallpapers.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun searchWallpaperByTag(tagId: Int) = viewModelScope.launch {
        try {
            val result = repository.getAllWallPapersByTag(tagId)

            _searchWallpapers.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _searchWallpapers.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun searchSingleWallpaperByTag(tagId: Int, limit: Int = 30) = viewModelScope.launch {
        val first = if (limit == 5) 2 else limit
        val second = if (limit == 5) 3 else limit
        if (!hasMorePages1 || _loading1.value == true) return@launch
        _loading1.value = true
        try {
            val result1 =
                repository.getWallPapersByTag(tagId, 1, page = currentPage1, limit = first)
            val result2 =
                repository.getWallPapersByTag(tagId, 3, isActive = 1, currentPage1, limit = second)
            hasMorePages1 = result1.data.nextPageUrl != null && result2.data.nextPageUrl != null
            currentPage1++

            allWallpapers1.addAll(result1.data.data.filter { it.contents.isNotEmpty() })
            allWallpapers1.addAll(result2.data.data.filter { it.contents.isNotEmpty() })
            println("searchSingleWallpaperByTag: $allWallpapers1")
            _searchWallpapers1.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _searchWallpapers1.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun searchSlideWallpaperByTag(tagId: Int, limit: Int = 30) = viewModelScope.launch {
        if (!hasMorePages2 || _loading2.value == true) return@launch
        _loading2.value = true
        try {
            val result1 =
                repository.getWallPapersByTag(tagId, 3, page = currentPage2, limit = limit)
            hasMorePages2 = result1.data.nextPageUrl != null
            currentPage2++

            allWallpapers2.addAll(result1.data.data.filter { it.contents.isNotEmpty() })
            _searchWallpapers2.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _searchWallpapers2.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
        }
    }

    fun searchVideoWallpaperByTag(tagId: Int, limit: Int = 30) = viewModelScope.launch {
        val first = if (limit == 5) 2 else limit
        val second = if (limit == 5) 3 else limit
        if (!hasMorePages3 || _loading3.value == true) return@launch
        _loading3.value = true
        try {
            val result1 =
                repository.getWallPapersByTag(tagId, 2, page = currentPage3, limit = first)
            val result2 =
                repository.getWallPapersByTag(tagId, 4, page = currentPage3, limit = second)

            hasMorePages3 = result1.data.nextPageUrl != null && result2.data.nextPageUrl != null
            currentPage3++

            allWallpapers3.addAll(result1.data.data.filter { it.contents.isNotEmpty() })
            allWallpapers3.addAll(result2.data.data.filter { it.contents.isNotEmpty() })
            _searchWallpapers3.value = allWallpapers3
            _error.value = null
        } catch (e: Exception) {
            println("loadWallpapers: ${e.message}")
            _searchWallpapers3.value = emptyList<Wallpaper>()
            _error.value = e.localizedMessage
        } finally {
            _loading3.value = false
        }
    }

    fun resetSearchPaging() {
        currentPage1 = 1
        currentPage2 = 1
        currentPage3 = 1
        hasMorePages1 = true
        hasMorePages2 = true
        hasMorePages3 = true
        allWallpapers1.clear()
        allWallpapers2.clear()
        allWallpapers3.clear()
    }

}