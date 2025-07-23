package com.example.ringtone.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtone.remote.api.InteractionRequest
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.model.Wallpaper
import com.example.ringtone.remote.repository.FavouriteRepository
import com.example.ringtone.remote.repository.RingtoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteWallpaperViewModel @Inject constructor(
    private val repository: FavouriteRepository,
    private val ringtoneRepository : RingtoneRepository,
) : ViewModel() {
    private var _wallpaper = MutableLiveData<Wallpaper>()
    val wallpaper: LiveData<Wallpaper> get() = _wallpaper

    private var _allWallpapers = MutableLiveData<List<Wallpaper>>()
    val allWallpapers: LiveData<List<Wallpaper>> get() = _allWallpapers

    fun loadWallpaperById(id: Int) {
        println("loadRingtoneById 0: $id")
        viewModelScope.launch {
            _wallpaper.value = repository.getWallpaperById(id)
            println("loadRingtoneById: ${_wallpaper.value}")
        }
    }

    fun loadAllWallpapers() {
        viewModelScope.launch {
            _allWallpapers.value = repository.getAllWallpapers()
            println("loadRingtoneById: ${_wallpaper.value}")
        }
    }


    fun insertWallpaper(wallpaper: Wallpaper) = viewModelScope.launch(Dispatchers.IO) {
        println("insertWallpaper: $wallpaper")
        repository.insertWallpaper(wallpaper).also {

            ringtoneRepository.updateStatus(InteractionRequest(
                3, 3 , wallpaper.id
            ))
        }
    }

    fun deleteWallpaper(wallpaper: Wallpaper) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteWallpaper(wallpaper)
    }

    fun increaseDownload(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.updateStatus(InteractionRequest (
                2,3, wallpaper.id
            ))
        }
    }

    fun increaseSet(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            ringtoneRepository.updateStatus(InteractionRequest (
                1,3, wallpaper.id
            ))
        }
    }
}