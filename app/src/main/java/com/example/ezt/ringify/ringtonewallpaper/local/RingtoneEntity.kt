package com.example.ringtone.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ringtone.remote.model.Author
import com.example.ringtone.remote.model.Category
import com.example.ringtone.remote.model.ImageContent
import com.example.ringtone.remote.model.RingtoneContents
import com.example.ringtone.remote.model.WallpaperContent

@Entity(tableName = "ringtone")
data class RingtoneEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val contents: RingtoneContents,
    val author: Author,
    val category: List<Category>,
    val active: Int,
    @ColumnInfo(name = "alert_license") val alertLicense: Int,
    @ColumnInfo(name = "order_index") val order: Int,
    @ColumnInfo(name = "is_private") val isPrivate: Int,
    val trend: Int,
    val popular: Int,
    @ColumnInfo(name = "daily_rating") val dailyRating: Int,
    @ColumnInfo(name = "weekly_rating") val weeklyRating: Int,
    @ColumnInfo(name = "monthly_rating") val monthlyRating: Int,
    @ColumnInfo(name = "like_count") val like: Int,
    @ColumnInfo(name = "set_count") val set: Int,
    @ColumnInfo(name = "download_count") val download: Int,
    val country: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    val duration: Int
)

@Entity(tableName = "wallpaper")
data class WallpaperEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val thumbnail: WallpaperContent,
    val contents: List<ImageContent>,
    @ColumnInfo(name = "original_id") val originalId: Long,
    val type: Int,
    val active: Int,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "is_private") val isPrivate: Int,
    val trend: Int,
    val popular: Int,
    @ColumnInfo(name = "daily_rating") val dailyRating: Int,
    @ColumnInfo(name = "weekly_rating") val weeklyRating: Int,
    @ColumnInfo(name = "monthly_rating") val monthlyRating: Int,
    @ColumnInfo(name = "like_count") val like: Int,
    @ColumnInfo(name = "set_count") val set: Int,
    @ColumnInfo(name = "download_count") val download: Int,
    val country: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)
