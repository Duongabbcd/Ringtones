package com.example.ringtone.remote.repository

import com.example.ringtone.remote.api.ApiService
import com.example.ringtone.remote.api.InteractionRequest
import com.example.ringtone.remote.api.SearchRequest
import com.example.ringtone.remote.api.SearchResponse
import com.example.ringtone.remote.model.CallScreenResponse
import com.example.ringtone.remote.model.CategoriesResponse
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
    suspend fun fetchPopularRingtones(orderBy: String): RingtoneResponse = apiService.getPopularRingtones()
    suspend fun fetchTrendingRingtones(): RingtoneResponse = apiService.getTrendingRingtones()

    suspend fun fetchWallpapers(): WallpaperResponse = apiService.getWallpapers()
    suspend fun fetchCallScreens(): CallScreenResponse = apiService.getCallScreens()
    suspend fun fetchContents(): ContentResponse = apiService.getContents()
    suspend fun fetchRingtoneCategories(): CategoriesResponse = apiService.getRingtoneCategory()
    suspend fun fetchWallpaperCategories(): CategoriesResponse = apiService.getWallpaperCategory()

    suspend fun fetchRingtoneByCategory(categoryId: Int, orderBy: String): RingtoneResponse = apiService.getRingtonesByCategory(categoryId, orderBy = orderBy)
    suspend fun searchRingtonesByName(name: String): SearchResponse = apiService.searchRingtonesByName(
        SearchRequest(name))

   suspend fun setLike(request: InteractionRequest) = apiService.updateStatus(request)

    companion object {
        var TOKEN: String = ""
    }
}