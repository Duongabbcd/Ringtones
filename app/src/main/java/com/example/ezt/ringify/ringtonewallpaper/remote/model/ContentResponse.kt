package com.example.ringtone.remote.model

import com.google.gson.annotations.SerializedName

data class ContentResponse(
    val data: ContentPaging
)

data class ContentPaging(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<ContentItem>,
    @SerializedName("first_page_url") val firstPageUrl: String,
    val from: Int,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int
)

data class ContentItem(
    val id: Int,
    val name: String,
    val contents: List<ImageContent>,
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
    @SerializedName("time_public") val timePublic: Long,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("created_at") val createdAt: String
)

data class ImageContent(
    val path: String,
    val size: ImageSize,
    val url: ImageUrls
)

data class ImageSize(
    val width: Int,
    val height: Int
)

data class ImageUrls(
    val full: String,
    val medium: String,
    val small: String,
    @SerializedName("extra_small") val extraSmall: String
)
