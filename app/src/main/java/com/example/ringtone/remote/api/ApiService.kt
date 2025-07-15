package com.example.ringtone.remote.api

import com.example.ringtone.remote.model.CallScreenResponse
import com.example.ringtone.remote.model.CategoriesResponse
import com.example.ringtone.remote.model.ContentResponse
import com.example.ringtone.remote.model.RingtoneResponse
import com.example.ringtone.remote.model.WallpaperResponse
import retrofit2.http.GET

interface ApiService {
    //Ringtones
    @GET("api/v1/ringtones?with=author+id,name,active-categories+id,name,thumbnail,active,content_count&order_by=id+desc")
    suspend fun getRingtones(): RingtoneResponse

    @GET("api/v1/ringtones?with=author+id,name,active-categories+id,name,thumbnail,active,content_count&where=popular+1&order_by=id+desc")
    suspend fun getPopularRingtones() : RingtoneResponse

    @GET("api/v1/ringtones?with=author+id,name,active-categories+id,name,thumbnail,active,content_count&where=trend+1&order_by=id+desc")
    suspend fun getTrendingRingtones() : RingtoneResponse

    //Wallpapers
    @GET("api/v1/wallpapers?page=2")
    suspend fun getWallpapers(): WallpaperResponse

    @GET("api/v1/call_screens")
    suspend fun getCallScreens(): CallScreenResponse

    @GET("api/v1/contents?page=2")
    suspend fun getContents(): ContentResponse

    //Categories
    @GET("api/v1/categories?page=2&with=author+id%2Cname%2Clink&type=2")
    suspend fun getCategory(): CategoriesResponse

}