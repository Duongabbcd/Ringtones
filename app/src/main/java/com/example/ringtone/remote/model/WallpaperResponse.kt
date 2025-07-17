package com.example.ringtone.remote.model

import com.google.gson.annotations.SerializedName

data class WallpaperResponse(
    val data: WallpaperPaging
)

data class WallpaperPaging(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<Wallpaper>,
    @SerializedName("first_page_url") val firstPageUrl: String,
    val from: Int,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int
)

data class Wallpaper(
    val id: Int,
    val name: String,
    val thumbnail: WallpaperContent,
    val contents: List<ImageContent>,
    @SerializedName("original_id") val originalId: Long,
    val type: Int,
    val active: Int,
    @SerializedName("order") val orderIndex: Int,
    @SerializedName("private") val isPrivate: Int,
    val trend: Int,
    val popular: Int,
    @SerializedName("daily_rating") val dailyRating: Int,
    @SerializedName("weekly_rating") val weeklyRating: Int,
    @SerializedName("monthly_rating") val monthlyRating: Int,
    val like: Int,
    val set: Int,
    val download: Int,
    val country: Int,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("created_at") val createdAt: String
) {
    companion object {
         val EMPTY_WALLPAPER = Wallpaper(
            0, "", thumbnail = WallpaperContent("", size =WallpaperSize(0,0), url = WallpaperUrls (
                "", "", "", ""
            ) ), contents = listOf(), 0L,0,0,0,0,0,0,0,0,0,0,0,0,0,"", ""
        )
    }
}

data class WallpaperContent(
    val path: String,
    val size: WallpaperSize,
    val url: WallpaperUrls
)

data class WallpaperSize(
    val width: Int,
    val height: Int
)

data class WallpaperUrls(
    val full: String,
    val medium: String,
    val small: String,
    @SerializedName("extra_small") val extraSmall: String
)
