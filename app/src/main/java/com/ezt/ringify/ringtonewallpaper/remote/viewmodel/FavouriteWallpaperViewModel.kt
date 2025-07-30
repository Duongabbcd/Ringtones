package com.ezt.ringify.ringtonewallpaper.remote.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezt.ringify.ringtonewallpaper.remote.api.InteractionRequest
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.repository.FavouriteRepository
import com.ezt.ringify.ringtonewallpaper.remote.repository.RingtoneRepository
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

    private var _liveWallpaper = MutableLiveData<Wallpaper>()
    val liveWallpaper: LiveData<Wallpaper> get() = _liveWallpaper

    private var _allWallpapers = MutableLiveData<List<Wallpaper>>()
    val allWallpapers: LiveData<List<Wallpaper>> get() = _allWallpapers

    private var _allLiveWallpapers = MutableLiveData<List<Wallpaper>>()
    val allLiveWallpapers: LiveData<List<Wallpaper>> get() = _allLiveWallpapers

    private val _loading1 = MutableLiveData<Boolean>()
    val loading1: LiveData<Boolean> = _loading1
    private val _loading2 = MutableLiveData<Boolean>()
    val loading2: LiveData<Boolean> = _loading2

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadWallpaperById(id: Int) {
        println("loadWallpaperById 0: $id")
        viewModelScope.launch {
            _wallpaper.value = repository.getWallpaperById(id)
            println("loadWallpaperById: ${_wallpaper.value}")
        }
    }

    fun loadLiveWallpaperById(id: Int) {
        println("loadWallpaperById 0: $id")
        viewModelScope.launch {
            _liveWallpaper.value = repository.getLiveWallpaperById(id)
            println("loadWallpaperById: ${_liveWallpaper.value}")
        }
    }
    

    fun loadAllWallpapers() {
        viewModelScope.launch {
            _loading1.value = true
            try {
                _allWallpapers.value = repository.getAllWallpapers()
                println("loadWallpaperById: ${_wallpaper.value}")
                _error.value = null
            } catch (e: Exception) {
                println("loadWallpapers: ${e.message}")
                _error.value = e.localizedMessage
            } finally {
                _loading1.value = false
            }
        }
    }

    fun loadLiveAllWallpapers() {
        viewModelScope.launch {
            _loading2.value = true
            try {
                _allLiveWallpapers.value = repository.getAllLiveWallpapers()
                println("loadWallpaperById: ${_wallpaper.value}")
                _error.value = null
            } catch (e: Exception) {
                println("loadWallpapers: ${e.message}")
                _error.value = e.localizedMessage
            } finally {
                _loading2.value = false
            }
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

    fun insertLiveWallpaper(wallpaper: Wallpaper) = viewModelScope.launch(Dispatchers.IO) {
        println("insertWallpaper: $wallpaper")
        repository.insertLiveWallpaper(wallpaper).also {

            ringtoneRepository.updateStatus(
                InteractionRequest(
                    3, 3, wallpaper.id
                )
            )
        }
    }

    fun deleteWallpaper(wallpaper: Wallpaper) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteWallpaper(wallpaper)
    }

    fun deleteLiveWallpaper(wallpaper: Wallpaper) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteLiveWallpaper(wallpaper)
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