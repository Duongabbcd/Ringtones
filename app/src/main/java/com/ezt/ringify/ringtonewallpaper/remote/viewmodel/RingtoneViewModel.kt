package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingtoneViewModel @Inject constructor(
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

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage1 = 1
    private var hasMorePages1 = true
    private var currentPage2 = 1
    private var hasMorePages2 = true
    private var currentPage3 = 1
    private var hasMorePages3 = true

    val allWallpapers1 = mutableListOf<Ringtone>()
    val allWallpapers2 = mutableListOf<Ringtone>()
    val allWallpapers3 = mutableListOf<Ringtone>()

    fun loadPopular(orderBy: String = "name+asc" ) = viewModelScope.launch {
        if (!hasMorePages1 || _loading.value ==  true ) return@launch
        _loading.value = true
        try {
            val result = repository.fetchPopularRingtones(currentPage1)
            result.data.data.onEach {
                println("loadPopular: $it")
            }

            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++
            allWallpapers1.addAll(result.data.data)
            _popular.value = allWallpapers1

            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadTrending() = viewModelScope.launch {
        if (!hasMorePages2 || _loading.value ==  true ) return@launch
        _loading.value = true
        try {
            val result = repository.fetchTrendingRingtones(currentPage2)
            _trending.value = result.data.data

            hasMorePages2 = result.data.nextPageUrl != null
            currentPage2++
            allWallpapers2.addAll(result.data.data)
            _trending.value = allWallpapers2
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadSelectedRingtones(categoryId: Int, orderBy: String  = "name+asc") = viewModelScope.launch {
        if (!hasMorePages3 || _loading.value ==  true ) return@launch
        _loading.value = true
        try {
            val result = repository.fetchRingtoneByCategory(categoryId, orderBy, currentPage3)
            println("loadSelectedRingtones: ${ result.data.firstPageUrl}")
            println("loadSelectedRingtones: ${ result.data.nextPageUrl}")
            hasMorePages3 = result.data.nextPageUrl != null
            currentPage3++
            allWallpapers3.addAll(result.data.data)
            _selectedRingtone.value = allWallpapers3

            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun searchRingtonesByName(input: String) = viewModelScope.launch {
        _loading.value = true
        try {
            println("searchRingtonesByName $input")
            val result = repository.searchRingtonesByName(input)
            _search.value = result.data
            _error.value = null
        } catch (e: Exception) {
            println("searchRingtonesByName Exception: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }


}