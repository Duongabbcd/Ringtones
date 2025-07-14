package com.example.ringtone.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtone.remote.model.RingtoneResponse
import com.example.ringtone.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingtoneViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _ringtones = MutableLiveData<RingtoneResponse>()
    val ringtones: LiveData<RingtoneResponse> = _ringtones

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRingtones() = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchRingtones()
            _ringtones.value = result
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }
}