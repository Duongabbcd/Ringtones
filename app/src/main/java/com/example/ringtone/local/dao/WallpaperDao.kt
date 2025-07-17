package com.example.ringtone.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringtone.local.WallpaperEntity

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpaper WHERE id = :id")
    suspend fun getById(id: Int): WallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wallpaper: WallpaperEntity)

    @Delete
    suspend fun delete(wallpaper: WallpaperEntity)
}