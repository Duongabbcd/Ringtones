package com.ezt.ringify.ringtonewallpaper.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ezt.ringify.ringtonewallpaper.local.SlideWallpaperEntity

@Dao
interface SlideWallpaperDao {
    @Query("SELECT * FROM slide_wallpaper LIMIT :limit")
    suspend fun getAllWallpaper(limit: Int = 100000): List<SlideWallpaperEntity>?

    @Query("SELECT * FROM slide_wallpaper WHERE id = :id")
    suspend fun getById(id: Int): SlideWallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: SlideWallpaperEntity)

    @Delete
    suspend fun delete(wallpaper: SlideWallpaperEntity)
}