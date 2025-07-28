package com.ezt.ringify.ringtonewallpaper.remote.repository

import com.ezt.ringify.ringtonewallpaper.remote.api.ApiService
import com.ezt.ringify.ringtonewallpaper.remote.api.InteractionRequest
import com.ezt.ringify.ringtonewallpaper.remote.api.SearchRequest
import com.ezt.ringify.ringtonewallpaper.remote.api.SearchResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.CallScreenResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.CategoriesResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.ContentResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.RingtoneResponse
import com.ezt.ringify.ringtonewallpaper.remote.model.WallpaperResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingtoneRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun fetchRingtones(): RingtoneResponse = apiService.getRingtones()
    suspend fun fetchPopularRingtones(orderBy: String): RingtoneResponse = apiService.getPopularRingtones()
    suspend fun fetchTrendingRingtones(): RingtoneResponse = apiService.getTrendingRingtones()

    suspend fun fetchCallScreens(): CallScreenResponse = apiService.getCallScreens()
    suspend fun fetchContents(): ContentResponse = apiService.getContents()
    suspend fun fetchRingtoneCategories(): CategoriesResponse = apiService.getRingtoneCategory()

    suspend fun fetchNewWallpapers(): WallpaperResponse = apiService.getNewWallpapers()
//    suspend fun fetchWallpaperCategories(): CategoriesResponse = apiService.getWallpaperCategory()
    suspend fun fetchAllWallpaperCategories(): CategoriesResponse = apiService.getAllWallpaperCategories()
    suspend fun fetchTrendingWallpapers(): WallpaperResponse = apiService.getTrendingWallpapers()
    suspend fun fetchWallpaperByCategory(categoryId: Int): WallpaperResponse = apiService.getWallpapersByCategory(categoryId = categoryId)
    suspend fun getAllExcludingCategory(): CategoriesResponse = apiService.getAllExcludingCategory()

    suspend fun fetchRingtoneByCategory(categoryId: Int, orderBy: String): RingtoneResponse = apiService.getRingtonesByCategory(categoryId, orderBy = orderBy)
    suspend fun searchRingtonesByName(name: String): SearchResponse = apiService.searchRingtonesByName(
        SearchRequest(name))

   suspend fun updateStatus(request: InteractionRequest) = apiService.updateStatus(request)
   suspend fun searchTag(request: SearchRequest) = apiService.searchTags(request)
   suspend fun getWallPapersByTag(tagId: Int) = apiService.getWallpapersByTag(tagId = tagId)

    suspend fun getPremiumWallpaper() = apiService.getPremiumWallpaper()
   suspend fun getCategoryById(categoryId: Int) = apiService.getCategoryById(where = "type 1,id $categoryId")

    //live wallpaper
    suspend fun getLiveWallpaper() = apiService.getLiveWallpaper()
    suspend fun getPremiumVideoWallpaper() = apiService.getPremiumVideoWallpaper()
    suspend fun getSlideWallpaper() = apiService.getSlideWallpaper()
    suspend fun getSingleWallpaper() = apiService.getSingleWallpaper()

    companion object {
        var TOKEN: String = ""
    }
}