package com.ezt.ringify.ringtonewallpaper.remote.model

import com.google.gson.annotations.SerializedName


data class RingtoneResponse(
    val data: RingtoneData
)

data class RingtoneData(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<Ringtone>,
    @SerializedName("first_page_url") val firstPageUrl: String,
    val from: Int,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int
)

data class Ringtone(
    val id: Int,
    val name: String,
    val contents: RingtoneContents,
    val author: Author,

    @SerializedName("categories")
    val categories: List<Category>,

    val active: Int,
    @SerializedName("alert_license") val alertLicense: Int,
    @SerializedName("order") val order: Int,
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
    @SerializedName("created_at") val createdAt: String,
    val duration: Int
) {
    companion object {
        val EMPTY_RINGTONE = Ringtone(
            -1, "", RingtoneContents("", "", ""),
            Author(-1, "", -1),
            emptyList(), // 🛠 Corrected from "category" to "categories"
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, "", "", 0
        )
    }
}


data class RingtoneContents(
    val disk: String?,
    val path: String,
    val url: String
)

data class Author(
    val id: Int,
    val name: String,
    val active: Int
)
