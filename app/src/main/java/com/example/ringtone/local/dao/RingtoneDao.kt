package com.example.ringtone.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ringtone.local.RingtoneEntity

@Dao
interface RingtoneDao {
    @Query("SELECT * FROM ringtone WHERE id = :id")
    suspend fun getById(id: Int): RingtoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ringtone: RingtoneEntity)

    @Delete
    suspend fun delete(ringtone: RingtoneEntity)
}