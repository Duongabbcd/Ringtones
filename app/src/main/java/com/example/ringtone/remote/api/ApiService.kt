package com.example.ringtone.remote.api

import com.example.ringtone.remote.model.CallScreenResponse
import com.example.ringtone.remote.model.CategoriesResponse
import com.example.ringtone.remote.model.ContentResponse
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.model.RingtoneResponse
import com.example.ringtone.remote.model.WallpaperResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    //Ringtones
    @GET("api/v1/ringtones?with=author+id,name,active-categories+id,name,thumbnail,active,content_count&order_by=id+desc")
    suspend fun getRingtones(): RingtoneResponse

    @GET("api/v1/ringtones?with=author+id,name,active-categories+id,name,thumbnail,active,content_count&where=popular+1&order_by=name+asc")
    suspend fun getPopularRingtones() : RingtoneResponse

    @GET("api/v1/ringtones?with=author+id,name,active-categories+id,name,thumbnail,active,content_count&where=trend+1")
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

    @GET("api/v1/categories?page=1&with=author+name,id&where=type+2")
    suspend fun getRingtoneCategory(): CategoriesResponse

    @GET("api/v1/ringtones")
    suspend fun getRingtonesByCategory(
        @Query("category") categoryId: Int,
        @Query("with") with: String = "author+id,name,active-categories+id,name,thumbnail,active,content_count",
        @Query("order_by") orderBy: String,
        @Query("page") page: Int = 1
    ): RingtoneResponse

    //Wallpaper
    @GET("api/v1/categories?page=1&with=author+name,id&where=type+1")
    suspend fun getWallpaperCategory(): CategoriesResponse

    @GET("api/v1/categories?where=type+1")
    suspend fun getAllWallpaperCategories(): CategoriesResponse

    //Search
    @POST("api/v1/ringtones/search?with=author+id,name,active-categories+id,name,thumbnail,active,content_count")
    suspend fun searchRingtonesByName(
        @Body request: SearchRequest
    ):  SearchResponse

    @POST("api/v1/tags/search")
    suspend fun searchTags(
        @Body request: SearchRequest
    ):  SearchResponse


    @POST("api/v1/wallpapers?with=tags+id,name-apps+id,name&tag=11&app=1")
    suspend fun searchWallpapersByTag(
    ):  WallpaperResponse

    @POST("api/v1/interactions")
    suspend fun updateStatus(
        @Body request: InteractionRequest)
}

data class InteractionRequest(
    val type: Int,           // 1: set, 2: download, 3: like
    @SerializedName("content_type") val contentType: Int, //1: ringtones, 2: content, 3:wallpaper: 4: callscreen
    @SerializedName("content_id") val contentId: Int
)

data class SearchRequest(
    val name: String
)

data class SearchResponse(
    val data: List<Ringtone>
)