package com.example.ringtone.remote.repository

import com.example.ringtone.remote.api.ApiService
import com.example.ringtone.remote.model.CallScreenResponse
import com.example.ringtone.remote.model.ContentResponse
import com.example.ringtone.remote.model.RingtoneResponse
import com.example.ringtone.remote.model.WallpaperResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingtoneRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchRingtones(): RingtoneResponse = apiService.getRingtones()
    suspend fun fetchWallpapers(): WallpaperResponse = apiService.getWallpapers()
    suspend fun fetchCallScreens(): CallScreenResponse = apiService.getCallScreens()
    suspend fun fetchContents(): ContentResponse = apiService.getContents()

    companion object {
        var TOKEN: String = ""
    }
}