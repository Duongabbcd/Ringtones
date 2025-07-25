package com.example.ringtone.remote.model

import com.google.gson.annotations.SerializedName

data class CategoriesResponse(
    @SerializedName("data")
    val dataPage: DataPage
)

data class DataPage(
    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("data")
    val categories: List<Category>
)


data class Category(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("thumbnail")
    val thumbnail: Thumbnail?,  // nullable

    @SerializedName("type")
    val type: Int,

    @SerializedName("content_count")
    val contentCount: Int,   // Kotlin camelCase property name

    @SerializedName("active")
    val active: Int,

    @SerializedName("order")
    val order: Int,

    @SerializedName("daily_rating")
    val dailyRating: Int,

    @SerializedName("weekly_rating")
    val weeklyRating: Int,

    @SerializedName("monthly_rating")
    val monthlyRating: Int,

    @SerializedName("like")
    val like: Int,

    @SerializedName("set")
    val set: Int,

    @SerializedName("download")
    val download: Int,

    @SerializedName("country")
    val country: Int,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("created_at")
    val createdAt: String
) {
    companion object {
        val EMPTY_CATEGORY = Category(
            -99, "EMPTY_CATEGORY", Thumbnail("", Size(0,0), Url("","","","") ),0,0,0,0,0,0,0,0,0,0,0,"",""
        )
    }
}

data class Thumbnail(
    val path: String,
    val size: Size,
    val url: Url
)

data class Size(
    val width: Int,
    val height: Int
)

data class Url(
    val full: String,
    val medium: String,
    val small: String,
    val extra_small: String
)