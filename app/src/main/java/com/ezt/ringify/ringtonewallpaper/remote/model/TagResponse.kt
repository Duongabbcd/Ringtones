package com.ezt.ringify.ringtonewallpaper.remote.model

data class TagResponse(
    val data: List<Tag>
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