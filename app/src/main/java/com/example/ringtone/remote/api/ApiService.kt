package com.example.ringtone.remote.api

import com.example.ringtone.remote.model.CallScreenResponse
import com.example.ringtone.remote.model.ContentResponse
import com.example.ringtone.remote.model.RingtoneResponse
import com.example.ringtone.remote.model.WallpaperResponse
import retrofit2.http.GET

interface ApiService {
    @GET("api/v1/ringtones?order_by=id+desc")
    suspend fun getRingtones(): RingtoneResponse

    @GET("api/v1/wallpapers?page=2")
    suspend fun getWallpapers(): WallpaperResponse

    @GET("api/v1/call_screens")
    suspend fun getCallScreens(): CallScreenResponse

    @GET("api/v1/contents?page=2")
    suspend fun getContents(): ContentResponse
}