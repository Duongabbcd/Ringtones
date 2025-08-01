package com.ezt.ringify.ringtonewallpaper.remote.model

import com.google.gson.annotations.SerializedName

data class CallScreenResponse(
    val data: CallScreenPaging
)

data class CallScreenPaging(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<CallScreenItem>,
    @SerializedName("first_page_url") val firstPageUrl: String,
    val from: Int,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int
)

data class CallScreenItem(
    val id: Int,
    val name: String,
    val thumbnail: Thumbnail,
    val contents: CallScreenContents,
    @SerializedName("author_id") val authorId: Int,
    val active: Int,
    @SerializedName("alert_license") val alertLicense: Int,
    @SerializedName("order") val orderIndex: Int,  // avoid Kotlin 'order' keyword
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
        val CALLSCREEN_EMPTY = CallScreenItem(
            -1, "CALLSCREEN_EMPTY", Thumbnail("", Size(0, 0), Url("", "", "", "")),
            CallScreenContents("", "", ""), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", ""
        )
    }
}

data class CallScreenContents(
    val disk: String?,
    val path: String,
    val url: String
)
