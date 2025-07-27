package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenItem
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallScreenViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _callScreens = MutableLiveData<List<CallScreenItem>>()
    val callScreens: LiveData<List<CallScreenItem>> = _callScreens

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCallScreens() = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchCallScreens()
            _callScreens.value = result.data.data

            _error.value = null
        } catch (e: Exception) {
            println("loadCallScreens: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }
}