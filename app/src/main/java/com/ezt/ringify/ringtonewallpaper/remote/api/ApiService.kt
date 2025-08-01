package com.ezt.ringify.ringtonewallpaper.remote.api

import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.CategoriesResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.model.RingtoneResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.Tag
import com.ezt.ringify.ringtonewallpaper.remote.model.WallpaperResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    //Ringtones
    @GET("api/v1/ringtones?app=1&with=author+id,name,active-categories+id,name,thumbnail,active,content_count&order_by=id+desc")
    suspend fun getRingtones(): RingtoneResponse

    @GET("api/v1/ringtones?app=1&with=author+id,name,active-categories+id,name,thumbnail,active,content_count&where=popular+1&order_by=name+asc")
    suspend fun getPopularRingtones(
        @Query("page")page: Int = 1
    ): RingtoneResponse

    @GET("api/v1/ringtones?app=1&with=author+id,name,active-categories+id,name,thumbnail,active,content_count&where=trend+1")
    suspend fun getTrendingRingtones(
        @Query("page")page: Int = 1
    ): RingtoneResponse


    //Categories
    @GET("api/v1/categories?app=1&with=author+id%2Cname%2Clink&type=2")
    suspend fun getCategory(): CategoriesResponse

    @GET("api/v1/categories?app=1&where=type+2")
    suspend fun getRingtoneCategory(
        @Query("page") page: Int = 1
    ): CategoriesResponse

    @GET("api/v1/ringtones?app=1")
    suspend fun getRingtonesByCategory(
        @Query("category") categoryId: Int,
        @Query("with") with: String = "author+id,name,active-categories+id,name,thumbnail,active,content_count",
        @Query("order_by") orderBy: String,
        @Query("page") page: Int = 1
    ): RingtoneResponse

    //Wallpaper
//    @GET("api/v1/categories?&with=author+name,id&where=type+1")
//    suspend fun getWallpaperCategory(): CategoriesResponse

    @GET("api/v1/categories?app=1&where=type+1")
    suspend fun getAllWallpaperCategories(
        @Query("page") page: Int = 1
    ): CategoriesResponse

    @GET("api/v1/wallpapers?app=1&where=trend+1,type+1")
    suspend fun getTrendingWallpapers(
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("api/v1/wallpapers?app=1&where=type+1&order_by=updated_at+desc")
    suspend fun getNewWallpapers(
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("api/v1/wallpapers?app=1")
    suspend fun getWallpapersByCategory(
        @Query("with") with: String = "tags+id,name-apps+id,name",
        @Query("app") appId: Int = 1,
        @Query("category") categoryId: Int,
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("api/v1/categories?app=1")
    suspend fun getAllExcludingCategory(
        @Query("where") where: String = "type+1,id+!=+75"
    ): CategoriesResponse

    //Search
    @POST("api/v1/ringtones/search?app=1&with=author+id,name,active-categories+id,name,thumbnail,active,content_count")
    suspend fun searchRingtonesByName(
        @Body request: SearchRequest
    ):  SearchResponse

    @POST("api/v1/tags/search?app=1")
    suspend fun searchTags(
        @Body request: SearchRequest
    ):  TagResponse

    @GET("api/v1/wallpapers?app=1")
    suspend fun getWallpapersByTag(
        @Query("with") with: String = "tags+id,name-apps+id,name",
        @Query("app") app: Int = 1,
        @Query("tag") tagId: Int
    ): WallpaperResponse

    @GET("api/v1/wallpapers?where=type+2")
    suspend fun getLiveWallpaper(
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("api/v1/wallpapers?where=type+4")
    suspend fun getPremiumVideoWallpaper(
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("/api/v1/wallpapers?where=type+3,private+0")
    suspend fun getSlideWallpaper(
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("/api/v1/wallpapers?where=type+3,private+1")
    suspend fun getSingleWallpaper(
        @Query("page") page: Int = 1
    ): WallpaperResponse

    @GET("api/v1/wallpapers?app=1&category=75&private=1&with=tags+id,name")
    suspend fun getPremiumWallpaper(): WallpaperResponse


    @POST("api/v1/interactions?app=1")
    suspend fun updateStatus(
        @Body request: InteractionRequest)


    @GET("api/v1/categories?app=1")
    suspend fun getCategoryById(
        @Query("with") with: String = "author name,id",
        @Query("where") where: String
    ): CategoriesResponse

    //Callscreen
    @GET("api/v1/call_screens?app=1")
    suspend fun getCallScreens(): CallScreenResponse

    @GET("api/v1/contents?app=1&where=type+3")
    suspend fun getCallScreenContent(
        @Query("call_screen") callScreen: Int
    ): ContentResponse

    @GET("api/v1/contents?app=1&where=type+1")
    suspend fun getBackgroundContent(
        @Query("call_screen") callScreen: Int
    ): ContentResponse

    @GET("api/v1/contents?app=1&where=type+1")
    suspend fun getAllBackgroundContent(
        @Query("page") page: Int = 1
    ): ContentResponse

    @GET("api/v1/contents?app=1&where=type+2")
    suspend fun getAllAvatarContent(
        @Query("page") page: Int = 1
    ): ContentResponse

    @GET("api/v1/contents?app=1&where=type+3")
    suspend fun getAllIconContent(
        @Query("page") page: Int = 1
    ): ContentResponse
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

data class TagResponse(
    val data: List<Tag>
)