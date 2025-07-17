package com.example.ringtone.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtone.remote.model.Category
import com.example.ringtone.remote.repository.RingtoneRepository
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

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

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
            val result = repository.fetchWallpaperCategories()
            _ringtoneCategory.value = result.dataPage.categories
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }
}