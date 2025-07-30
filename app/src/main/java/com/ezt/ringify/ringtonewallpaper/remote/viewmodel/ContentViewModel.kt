package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentItem
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.model.RingtoneResponse
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

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    private val _content = MutableLiveData<List<ImageContent>>()
    val content: LiveData<List<ImageContent>> = _content

    fun getCallScreenContent(callScreenId: Int) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.getCallScreenContent(callScreenId)
            _content.value = result.data.data.first().contents

            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }
}