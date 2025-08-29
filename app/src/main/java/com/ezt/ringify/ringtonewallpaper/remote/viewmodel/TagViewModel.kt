package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.api.SearchRequest
import com.ezt.ringify.ringtonewallpaper.remote.model.Tag
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.emptyList

@HiltViewModel
class TagViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _tag = MutableLiveData<List<Tag>>()
    val tag: LiveData<List<Tag>> = _tag


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentPage1 = 1
    private var hasMorePages1 = true
    val allTags1 = mutableListOf<Tag>()


    fun loadAllTags(isReset: Boolean = false) = viewModelScope.launch {
        println("loadWallpaperCategories 123: $hasMorePages1 and ${_loading.value}")
        if (!hasMorePages1 || _loading.value == true) return@launch
        _loading.value = true
        try {
            if (isReset) {
                allTags1.clear()
            }
            val result = repository.getAllTags()
            allTags1.addAll(result.data.data)
            println("loadWallpaperCategories 123: $allTags1")
            _tag.value = allTags1
            _error.value = null
        } catch (e: Exception) {
            println("Exception: ${e.message}")
            _tag.value = emptyList<Tag>()
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    //Later
    fun searchTag(searchText: String) = viewModelScope.launch {
        _loading.value = true
        try {
            if (searchText.isEmpty()) {
                _tag.value = mutableListOf<Tag>()// Clear the old tag first
            }
            val result = repository.searchTag(SearchRequest(searchText))
            println("searchTag: $searchText and $result")
            if (result.data.isEmpty()) {
                _error.value = "No matching tags found"
                _tag.value = mutableListOf<Tag>() // explicitly notify observers there's no tag
            } else {
                _tag.value = result.data// only set if not empty
                _error.value = null
            }
        } catch (e: Exception) {
            println("searchTag exception: ${e.message}")
            _tag.value = mutableListOf<Tag>()
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }
}