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
    suspend fun getRingtoneById(ringtoneId: Int): RingtoneResponse =
        apiService.getRingtoneById(where = "id+$ringtoneId")
    suspend fun fetchPopularRingtones(page: Int): RingtoneResponse = apiService.getPopularRingtones(page)
    suspend fun fetchTrendingRingtones(currentPage2: Int): RingtoneResponse = apiService.getTrendingRingtones(currentPage2)
    suspend fun fetchPrivateRingtones(currentPage2: Int): RingtoneResponse =
        apiService.fetchPrivateRingtones(currentPage2)

    suspend fun fetchNewRingtones(currentPage2: Int): RingtoneResponse =
        apiService.getNewRingtones(page = currentPage2)

    suspend fun fetchRingtoneCategories(page: Int): CategoriesResponse =
        apiService.getRingtoneCategory(page)

    suspend fun fetchNewWallpapers(page: Int): WallpaperResponse = apiService.getNewWallpapers(page = page)
//    suspend fun fetchWallpaperCategories(): CategoriesResponse = apiService.getWallpaperCategory()
suspend fun fetchAllWallpaperCategories(page: Int): CategoriesResponse =
    apiService.getAllWallpaperCategories(page)
    suspend fun fetchTrendingWallpapers(page: Int): WallpaperResponse = apiService.getTrendingWallpapers(page)
    suspend fun fetchWallpaperByCategory(
        categoryId: Int,
        page: Int,
        limit: Int = 30
    ): WallpaperResponse =
        apiService.getWallpapersByCategory(categoryId = categoryId, page = page, limit = limit)
    suspend fun getAllExcludingCategory(): CategoriesResponse = apiService.getAllExcludingCategory()

    suspend fun fetchRingtoneByCategory(categoryId: Int, orderBy: String, page: Int): RingtoneResponse = apiService.getRingtonesByCategory(categoryId, orderBy = orderBy, page = page)
    suspend fun searchRingtonesByName(name: String): SearchResponse = apiService.searchRingtonesByName(
        SearchRequest(name))

   suspend fun updateStatus(request: InteractionRequest) = apiService.updateStatus(request)
   suspend fun searchTag(request: SearchRequest) = apiService.searchTags(request)
   suspend fun getWallPapersByTag(tagId: Int) = apiService.getWallpapersByTag(tagId = tagId)

    suspend fun getPremiumWallpaper() = apiService.getPremiumWallpaper()
   suspend fun getCategoryById(categoryId: Int) = apiService.getCategoryById(where = "type 1,id $categoryId")

    //live wallpaper
    suspend fun getLiveWallpaper(page: Int) = apiService.getLiveWallpaper(page)
    suspend fun getNewLiveWallpaper(page: Int) = apiService.getNewLiveWallpaper(page)
    suspend fun getTrendingLiveWallpaper(page: Int) = apiService.getTrendingLiveWallpaper(page)
    suspend fun getPremiumVideoWallpaper(page: Int) = apiService.getPremiumVideoWallpaper(page)
    suspend fun getSlideWallpaper(page: Int) = apiService.getSlideWallpaper(page)
    suspend fun getSingleWallpaper(page: Int) = apiService.getSingleWallpaper(page)

    //Callscreen
    suspend fun fetchCallScreens(): CallScreenResponse = apiService.getCallScreens()
    suspend fun getCallScreenContent(callScreenId: Int): ContentResponse =
        apiService.getCallScreenContent(callScreenId)

    suspend fun getBackgroundContent(callScreenId: Int): ContentResponse =
        apiService.getBackgroundContent(callScreenId)

    suspend fun getAllBackgroundContent(page: Int): ContentResponse =
        apiService.getAllBackgroundContent(page)

    suspend fun getAllAvatarContent(page: Int): ContentResponse =
        apiService.getAllAvatarContent(page)

    suspend fun getAllIconContent(page: Int): ContentResponse =
        apiService.getAllIconContent(page)

    companion object {
        var TOKEN: String = ""
    }
}