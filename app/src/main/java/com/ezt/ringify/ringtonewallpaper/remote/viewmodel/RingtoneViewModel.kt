package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import com.ezt.ringify.ringtonewallpaper.utils.Common
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RingtoneViewModel @Inject constructor(
    @ApplicationContext private val contexts: Context,
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _ringtones = MutableLiveData<List<Ringtone>>()
    val ringtones: LiveData<List<Ringtone>> = _ringtones

    private val _selectedRingtone = MutableLiveData<List<Ringtone>>()
    val selectedRingtone: LiveData<List<Ringtone>> = _selectedRingtone

    private val _popular = MutableLiveData<List<Ringtone>>()
    val popular: LiveData<List<Ringtone>> = _popular

    private val _trending = MutableLiveData<List<Ringtone>>()
    val trending: LiveData<List<Ringtone>> = _trending

    private val _search = MutableLiveData<List<Ringtone>>()
    val search: LiveData<List<Ringtone>> = _search

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _loading1 = MutableLiveData<Boolean>()
    val loading1: LiveData<Boolean> = _loading1
    private val _customRingtones = MutableLiveData<List<Ringtone>>()
    val customRingtones: LiveData<List<Ringtone>> = _customRingtones

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _total = MutableLiveData<Int>()
    val total: LiveData<Int> = _total

    var currentPage1 = 1
    var hasMorePages1 = true
    var currentPage2 = 1
    var hasMorePages2 = true
    var currentPage3 = 1
    var hasMorePages3 = true

    val allWallpapers1 = mutableListOf<Ringtone>()
    val allWallpapers2 = mutableListOf<Ringtone>()
    val allWallpapers3 = mutableListOf<Ringtone>()

    private var currentSortOption: String = ""

    fun loadPopular(orderBy: String = "name+asc" ) = viewModelScope.launch {
        println("hasMorePages1: $hasMorePages1 and ${_loading.value}")
        if (!hasMorePages1 || _loading.value ==  true ) return@launch
        _loading.value = true
        try {
            println("loadPopular: $orderBy")
            val result = repository.fetchPopularRingtones(currentPage1, orderBy)
            result.data.data.filter { it.duration > 0 }

            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++
            allWallpapers1.addAll(result.data.data)
            _popular.value = allWallpapers1 

            _error.value = null
        } catch (e: Exception) {
            println("loadPopular error: ${e.message}")
            _customRingtones.value = emptyList<Ringtone>()
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadTrending(limit: Int = 15, isReset: Boolean = true) = viewModelScope.launch {
        if (!hasMorePages2 || _loading.value ==  true ) return@launch
        _loading.value = true
        try {
            val result = repository.fetchTrendingRingtones(currentPage2, limit = limit)

            hasMorePages2 = result.data.nextPageUrl != null
            currentPage2++
            allWallpapers2.addAll(result.data.data.filter { it.duration > 0 })
            _trending.value = allWallpapers2 
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _customRingtones.value = emptyList<Ringtone>()
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadSelectedRingtones(categoryId: Int, orderBy: String = "name+asc") =
        viewModelScope.launch {
            // Reset paging if order changed
            if (orderBy != currentSortOption) {
                currentSortOption = orderBy
                currentPage3 = 1
                hasMorePages3 = true
                allWallpapers3.clear()
            }

            // Stop loading if already loading or no more pages
            if (!hasMorePages3 || _loading.value == true) return@launch

        _loading.value = true
        try {
            val result = repository.fetchRingtoneByCategory(categoryId, orderBy, currentPage3)
            println("loadSelectedRingtones: $categoryId ${result.data.firstPageUrl}")
            println("loadSelectedRingtones: ${result.data.nextPageUrl}")

            hasMorePages3 = result.data.nextPageUrl != null
            currentPage3++

            // Avoid duplicates by filtering out items already in allWallpapers3
            val newItems = result.data.data.filter { it.duration > 0 }.filterNot { newItem ->
                allWallpapers3.any { existing -> existing.id == newItem.id }
            }
            allWallpapers3.addAll(newItems)
            println("allWallpapers3: $allWallpapers3")

            _selectedRingtone.value = allWallpapers3.toList()
            _total.value = _selectedRingtone.value?.size ?: 0
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _customRingtones.value = emptyList<Ringtone>()
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun searchRingtonesByName(input: String) = viewModelScope.launch {
        val trimmedInput = input.trim()
        if (trimmedInput.isEmpty()) {
            _search.value = emptyList()
            _error.value = null
            return@launch
        }

        _loading.value = true
        try {
            println("searchRingtonesByName $trimmedInput")
            val result = repository.searchRingtonesByName(trimmedInput)
            _search.value = result.data
            _error.value = null
        } catch (e: Exception) {
            println("searchRingtonesByName Exception: ${e.message}")
            _search.value = emptyList() // Clear on error
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }


    fun loadNewRingtones(sortOrder: String = "name+asc") = viewModelScope.launch {
        _loading1.value = true
        try {
            println("loadNewRingtones: $sortOrder")
            val backupRingtones = List(5) { Ringtone.EMPTY_RINGTONE }.toMutableList()
            val allNewRingtones = List(5) { Ringtone.EMPTY_RINGTONE }.toMutableList()

            val backupList = Common.getAllNewRingtones(contexts)
            if (backupList.isNotEmpty()) {
                backupRingtones.clear()
                val backupRingtones = backupList.flatMap { id ->
                    repository.getRingtoneById(id).data.data.filter { it.duration > 0 }
                }
                val sorted = backupRingtones.sortByCondition(sortOrder)
                _customRingtones.value = sorted
                _error.value = null
                return@launch
            }

            val firstPage = repository.fetchNewRingtones(1, sortOrder)
            val totalPage = maxOf(1, (firstPage.data.total + 29) / 30)
            val randomPage = (1..totalPage).random()

            allNewRingtones.clear()
            allNewRingtones.addAll(
                repository.fetchNewRingtones(
                    randomPage,
                    sortOrder
                ).data.data.filter { it.duration > 0 })
            Common.setAllNewRingtones(contexts, allNewRingtones.map { it.id })

            _customRingtones.value = allNewRingtones 
            _error.value = null

        } catch (e: CancellationException) {
            println("loadNewRingtones cancelled")
            throw e
        } catch (e: Exception) {
            println("searchRingtonesByName Exception: ${e.message}")
            _customRingtones.value = emptyList()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }


    fun loadWeeklyTrendingRingtones(sortOrder: String = "name+asc") = viewModelScope.launch {
        _loading1.value = true
        try {
            val backupRingtones = List(5) { Ringtone.EMPTY_RINGTONE }.toMutableList()
            val allNewRingtones = List(5) { Ringtone.EMPTY_RINGTONE }.toMutableList()

            val backupList = Common.getAllTrendingRingtones(contexts)
            if (backupList.isNotEmpty()) {
                backupRingtones.clear()
                backupList.onEach { id ->
                    backupRingtones.addAll(repository.getRingtoneById(id).data.data.filter { it.duration > 0 })
                }
                val sorted = backupRingtones.sortByCondition(sortOrder)
                _customRingtones.value = sorted

                _error.value = null
                return@launch
            }
            val result = repository.fetchTrendingRingtones(1)
            val totalPage = maxOf(1, (result.data.total + 29) / 30)
            val randomNumbers = (1..totalPage).shuffled().take(1)

            println("randomNumbers: $randomNumbers")
            allNewRingtones.clear()
            allNewRingtones.addAll(
                repository.fetchTrendingRingtones(
                    randomNumbers.first(),
                    sortOrder
                ).data.data.filter { it.duration > 0 })
            val fixedRingtoneId = allNewRingtones.map { it.id }
            Common.setAllWeeklyTrendingRingtones(context = contexts, fixedRingtoneId)
            _customRingtones.value = allNewRingtones

            _error.value = null
        } catch (e: CancellationException) {
            // coroutine was cancelled (normal lifecycle), don't mark as error
            println("loadNewRingtones cancelled")
            throw e // always rethrow cancellation so coroutine can stop properly
        } catch (e: Exception) {
            println("searchRingtonesByName Exception: ${e.message}")
            _customRingtones.value = emptyList<Ringtone>()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    fun loadEditorChoicesRingtones(sortOrder: String = "name+asc") = viewModelScope.launch {
        _loading1.value = true
        try {
            val backupRingtones = List(5) { Ringtone.EMPTY_RINGTONE }.toMutableList()
            val allNewRingtones = List(5) { Ringtone.EMPTY_RINGTONE }.toMutableList()

            val backupList = Common.getAllEditorChoices(contexts)
            if (backupList.isNotEmpty()) {
                backupRingtones.clear()
                backupList.onEach { id ->
                    backupRingtones.addAll(repository.getRingtoneById(id).data.data.filter { it.duration > 0 })
                }
                val sorted = backupRingtones.sortByCondition(sortOrder)
                _customRingtones.value = sorted

                _error.value = null
                return@launch
            }

            val result = repository.fetchPrivateRingtones(1)
            val totalPage = maxOf(1, (result.data.total + 29) / 30)
            val randomNumbers = (1..totalPage).shuffled().take(1)
            println("randomNumbers: $randomNumbers")
            allNewRingtones.clear()
            allNewRingtones.addAll(
                repository.fetchPrivateRingtones(
                    randomNumbers.first(),
                    sortOrder
                ).data.data.filter { it.duration > 0 })
            val fixedRingtoneId = allNewRingtones.map { it.id }
            Common.setAllEditorChoices(context = contexts, fixedRingtoneId)
            _customRingtones.value = allNewRingtones

            _error.value = null
        } catch (e: CancellationException) {
            // coroutine was cancelled (normal lifecycle), don't mark as error
            println("loadNewRingtones cancelled")
            throw e // always rethrow cancellation so coroutine can stop properly
        } catch (e: Exception) {
            println("searchRingtonesByName Exception: ${e.message}")
            _customRingtones.value = emptyList<Ringtone>()
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
        }
    }

    private fun List<Ringtone>.sortByCondition(sortOrder: String = ""): List<Ringtone> {
        return when (sortOrder) {
            "name+asc" -> this.sortedBy { it.name }
            "name+desc" -> this.sortedByDescending { it.name }
            "created_at+asc" -> this.sortedBy { it.createdAt }
            "created_at+desc" -> this.sortedByDescending { it.createdAt }
            else -> this.sortedBy { it.name }
        }
    }
}