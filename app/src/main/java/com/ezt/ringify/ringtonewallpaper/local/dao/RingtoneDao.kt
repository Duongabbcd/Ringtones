package com.ezt.ringify.ringtonewallpaper.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ezt.ringify.ringtonewallpaper.local.RingtoneEntity

@Dao
interface RingtoneDao {
    @Query("SELECT * FROM ringtone WHERE id = :id")
    suspend fun getById(id: Int): RingtoneEntity?

    @Query("SELECT * FROM ringtone")
    suspend fun getAllRingtones(): List<RingtoneEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ringtone: RingtoneEntity)

    @Delete
    suspend fun delete(ringtone: RingtoneEntity)
}