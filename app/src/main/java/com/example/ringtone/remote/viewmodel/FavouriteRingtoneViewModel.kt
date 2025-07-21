package com.example.ringtone.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtone.remote.api.InteractionRequest
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.repository.FavouriteRepository
import com.example.ringtone.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteRingtoneViewModel @Inject constructor(
    private val repository: FavouriteRepository,
    private val ringtoneRepository : RingtoneRepository,
) : ViewModel() {
    private val _ringtone = MutableLiveData<Ringtone>()
    val ringtone: LiveData<Ringtone> get() = _ringtone

    fun loadRingtoneById(id: Int) {
        println("loadRingtoneById 0: $id")
        viewModelScope.launch {
            _ringtone.value = repository.getRingtoneById(id)
            println("loadRingtoneById: ${_ringtone.value}")
        }
    }


    fun insertRingtone(ringtone: Ringtone) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertRingtone(ringtone).also {
            println("insertRingtone: ${ringtone.id}")
            ringtoneRepository.updateStatus(InteractionRequest(
                3, 1 , ringtone.id
            ))
        }
    }

    fun deleteRingtone(ringtone: Ringtone) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteRingtone(ringtone)
    }

    fun increaseDownload(ringtone: Ringtone) {
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.updateStatus(InteractionRequest (
                2,1, ringtone.id
            ))
        }
    }

    fun increaseSet(ringtone: Ringtone) {
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.updateStatus(InteractionRequest (
                1,1, ringtone.id
            ))
        }
    }
}