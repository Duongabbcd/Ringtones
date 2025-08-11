package com.ezt.ringify.ringtonewallpaper.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ezt.ringify.ringtonewallpaper.local.LiveWallpaperEntity

@Dao
interface LiveWallpaperDao {
    @Query("SELECT * FROM live_wallpaper")
    suspend fun getAllWallpaper(): List<LiveWallpaperEntity>?

    @Query("SELECT * FROM live_wallpaper WHERE id = :id")
    suspend fun getById(id: Int): LiveWallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: LiveWallpaperEntity)

    @Delete
    suspend fun delete(wallpaper: LiveWallpaperEntity)
}