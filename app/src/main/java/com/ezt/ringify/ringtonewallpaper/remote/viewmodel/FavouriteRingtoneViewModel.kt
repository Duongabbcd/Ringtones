package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.api.InteractionRequest
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.repository.FavouriteRepository
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
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

    private val _allRingtones = MutableLiveData<List<Ringtone>>()
    val allRingtones: LiveData<List<Ringtone>> get() = _allRingtones

    fun loadAllRingtones() {
        viewModelScope.launch {
            _allRingtones.value = repository.getAllRingtones()
            println("loadRingtoneById: ${_ringtone.value}")
        }
    }

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