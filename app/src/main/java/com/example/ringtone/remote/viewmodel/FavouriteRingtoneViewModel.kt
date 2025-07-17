package com.example.ringtone.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.ringtone.remote.api.InteractionRequest
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.repository.FavouriteRepository
import com.example.ringtone.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import javax.inject.Inject

@HiltViewModel
class FavouriteRingtoneViewModel @Inject constructor(
    private val repository: FavouriteRepository,
    private val ringtoneRepository : RingtoneRepository,
) : ViewModel() {
    private val _ringtone = MutableLiveData<Ringtone>()
    val ringtone: LiveData<Ringtone> get() = _ringtone

    fun loadRingtoneById(id: Int) {
        viewModelScope.launch {
            _ringtone.value = repository.getRingtoneById(id)
        }
    }


    fun insertRingtone(ringtone: Ringtone) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertRingtone(ringtone).also {
            println("insertRingtone: ${ringtone.id}")
            ringtoneRepository.setLike(InteractionRequest(
                3, 1 , ringtone.id
            ))
        }
    }

    fun deleteRingtone(ringtone: Ringtone) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteRingtone(ringtone)
    }
}