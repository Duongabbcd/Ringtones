package com.ezt.ringify.ringtonewallpaper.remote.model

import com.google.gson.annotations.SerializedName

data class TagResponse(
    val data: TagData
)

data class TagSearchingResponse(
    @SerializedName("data") val data: List<Tag>,
)

data class TagData(
    @SerializedName("data") val data: List<Tag>,
    @SerializedName("first_page_url") val firstPageUrl: String,
    val from: Int,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int
)


data class Tag(
    val id: Int,
    val name: String,
    val name_md5: String,
    val order: Int,
    val active: Int,
    val created_at: String,
    val updated_at: String
)