package com.example.ringtone.remote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ringtone.remote.api.SearchRequest
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.repository.RingtoneRepository
import com.example.ringtone.utils.Common
import com.example.ringtone.utils.Utils
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class RingtoneViewModel @Inject constructor(
    private val repository: RingtoneRepository
) : ViewModel() {

    private val _ringtones = MutableLiveData<List<Ringtone>>()
    val ringtones: LiveData<List<Ringtone>> = _ringtones

    private val _selectedRingtone = MutableLiveData<List<Ringtone>>()
    val selectedRingtone: LiveData<List<Ringtone>> = _selectedRingtone

    private val _popular = MutableLiveData<List<Ringtone>>()
    val popular: LiveData<List<Ringtone>> = _popular

    private val _trending = MutableLiveData<List<Ringtone>>()
    val trending: LiveData<List<Ringtone>> = _trending


    private val _search = MutableLiveData<List<Ringtone>>()
    val search: LiveData<List<Ringtone>> = _search

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRingtones() = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchRingtones()
            _ringtones.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadPopular(orderBy: String = "name+asc" ) = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchPopularRingtones(orderBy)
            result.data.data.onEach {
                println("loadPopular: $it")
            }
            _popular.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadTrending() = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchTrendingRingtones()
            _trending.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun loadSelectedRingtones(categoryId: Int, orderBy: String  = "name+asc") = viewModelScope.launch {
        _loading.value = true
        try {
            val result = repository.fetchRingtoneByCategory(categoryId, orderBy)
            result.data.data.onEach {
                println("loadSelectedRingtones: $it")
            }
            _selectedRingtone.value = result.data.data
            _error.value = null
        } catch (e: Exception) {
            println("loadRingtones: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    fun searchRingtonesByName(input: String) = viewModelScope.launch {
        _loading.value = true
        try {
            println("searchRingtonesByName $input")
            val result = repository.searchRingtonesByName(input)
            _search.value = result.data
            _error.value = null
        } catch (e: Exception) {
            println("searchRingtonesByName Exception: ${e.message}")
            _error.value = e.localizedMessage
        } finally {
            _loading.value = false
        }
    }

    suspend fun getRemoteFileLength(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()

                val length = connection.contentLengthLong
                connection.disconnect()
                Utils.formatDuration(length)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

}