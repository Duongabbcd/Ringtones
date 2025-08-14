package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentItem
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _contents = MutableLiveData<ContentResponse>()
    val contents: LiveData<ContentResponse> = _contents

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _loading1 = MutableLiveData<Boolean>()
    val loading1: LiveData<Boolean> = _loading1

    private val _loading2 = MutableLiveData<Boolean>()
    val loading2: LiveData<Boolean> = _loading2

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    private val _callScreenContent = MutableLiveData<List<ImageContent>>()
    val callScreenContent: LiveData<List<ImageContent>> = _callScreenContent

    private val _backgroundContent = MutableLiveData<List<ImageContent>>()
    val backgroundContent: LiveData<List<ImageContent>> = _backgroundContent
    private val _selectCallScreenContent = MutableLiveData<List<ContentItem>>()
    val selectCallScreenContent: LiveData<List<ContentItem>> = _selectCallScreenContent

    private val _iconContent = MutableLiveData<List<Pair<ImageContent, ImageContent>>>()
    val iconContent: LiveData<List<Pair<ImageContent, ImageContent>>> = _iconContent

     var currentPage1 = 1
    private var hasMorePages1 = true
    val allWallpapers1 = mutableListOf<ImageContent>()
    val allWallpapers2 = mutableListOf<Pair<ImageContent, ImageContent>>()
    val allCallScreen = mutableListOf<ContentItem>()

    private var isLoadingMore = false

    fun getCallScreenContent(callScreenId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.getCallScreenContent(callScreenId)
            println("getCallScreenContent: ${result.data.data.first().contents}")
            _callScreenContent.value = result.data.data.first().contents

            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun getBackgroundContent(callScreenId: Int) = viewModelScope.launch {
        _loading2.value = true
        try {
            val result = repository.getBackgroundContent(callScreenId)
            _backgroundContent.value = result.data.data.first().contents

            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
        }
    }

    fun getAllCallScreenBackgrounds() = viewModelScope.launch {
        if (!hasMorePages1 || _loading.value == true) return@launch
        _loading.value = true
        try {
            println("currentPage1: $currentPage1")
            val result = repository.getAllBackgroundContent(currentPage1)
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++

//            val newItems = result.data.data.mapNotNull { item ->
//                val imageContent = item.contents.firstOrNull { content ->
//                    content.url.full.endsWith(".jpg", ignoreCase = true) ||
//                            content.url.full.endsWith(".png", ignoreCase = true) ||
//                            content.url.full.endsWith(".webp", ignoreCase = true)
//                }
//                imageContent
//            }
            val newItems = result.data.data

            allCallScreen.addAll(newItems)
            _selectCallScreenContent.value = allCallScreen
            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
            isLoadingMore = false // ✅ RESET HERE
        }
    }


    fun getAllCallScreenAvatars() = viewModelScope.launch {
        if (!hasMorePages1 || _loading1.value == true) return@launch
        _loading1.value = true
        try {
            println("currentPage1: $currentPage1")
            val result = repository.getAllAvatarContent(currentPage1)
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++

            val newItems = result.data.data.mapNotNull { item ->
                // Get image preview from content
                val imageContent = item.contents.firstOrNull { content ->
                    content.url.full.endsWith(".jpg", ignoreCase = true) ||
                            content.url.full.endsWith(".png", ignoreCase = true) ||
                            content.url.full.endsWith(".webp", ignoreCase = true)
                }
                imageContent // Only return if found, will be added to list
            }

            allWallpapers1.addAll(newItems)
            _backgroundContent.value = allWallpapers1
            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading1.value = false
            isLoadingMore = false // ✅ RESET HERE
        }
    }


    fun getAllCallScreenIcons() = viewModelScope.launch {
        if (!hasMorePages1 || _loading2.value == true) return@launch
        _loading2.value = true
        try {
            println("currentPage1: $currentPage1")
            val result = repository.getAllIconContent(currentPage1)
            hasMorePages1 = result.data.nextPageUrl != null
            currentPage1++

            // ✅ Extract ImageContent from contents
            val imageItems = result.data.data.flatMap { item ->
                item.contents.filterIsInstance<ImageContent>()
            }

            // ✅ Group into pairs
            val newItems = imageItems
                .chunked(2)
                .filter { it.size == 2 }
                .map { Pair(it[0], it[1]) }

            allWallpapers2.addAll(newItems)
            _iconContent.value = allWallpapers2
            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading2.value = false
            isLoadingMore = false // ✅ RESET HERE
        }
    }


}