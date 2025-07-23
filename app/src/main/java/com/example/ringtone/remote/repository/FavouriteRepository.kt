package com.example.ringtone.remote.repository

import com.example.ringtone.local.RingtoneEntity
import com.example.ringtone.local.WallpaperEntity
import com.example.ringtone.local.dao.RingtoneDao
import com.example.ringtone.local.dao.WallpaperDao
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.model.Wallpaper
import com.example.ringtone.remote.model.WallpaperContent.Companion.OBJECT_EMPTY
import javax.inject.Inject

class FavouriteRepository @Inject constructor(
    private val ringtoneDao: RingtoneDao,
    private val wallpaperDao: WallpaperDao
) {

    suspend fun getRingtoneById(id: Int): Ringtone =
        ringtoneDao.getById(id)?.toDomain() ?: Ringtone.EMPTY_RINGTONE

    suspend fun getAllRingtones(): List<Ringtone> =
        ringtoneDao.getAllRingtones()?.map { it.toDomain() } ?: listOf()

    suspend fun insertRingtone(ringtone: Ringtone)  {
        println("insertRingtone: $ringtone")
        ringtoneDao.insert(ringtone.toEntity())
    }

    suspend fun deleteRingtone(ringtone: Ringtone) =
        ringtoneDao.delete(ringtone.toEntity())


    suspend fun getAllWallpapers(): List<Wallpaper> =
        wallpaperDao.getAllWallpaper()?.map { it.toDomain() } ?: listOf()

    suspend fun getWallpaperById(id: Int): Wallpaper =
        wallpaperDao.getById(id)?.toDomain() ?: Wallpaper.EMPTY_WALLPAPER

    suspend fun insertWallpaper(wallpaper: Wallpaper) =
        wallpaperDao.insert(wallpaper.toEntity())

    suspend fun deleteWallpaper(wallpaper: Wallpaper) =
        wallpaperDao.delete(wallpaper.toEntity())
}


fun Ringtone.toEntity() : RingtoneEntity =
    RingtoneEntity( id = id, name = name, contents = contents,
        author = author, category = categories,
        active = active, alertLicense = alertLicense, order = order,
        isPrivate = isPrivate, trend = trend, popular = popular,
        dailyRating = dailyRating, weeklyRating = weeklyRating,
        monthlyRating = monthlyRating, like = like, set = set,
        download = download, country = country,
        updatedAt = updatedAt, createdAt = createdAt,
        duration = duration)

fun RingtoneEntity.toDomain() : Ringtone =
    Ringtone( id = id, name = name, contents = contents,
        author = author, categories = category,
        active = active, alertLicense = alertLicense, order = order,
        isPrivate = isPrivate, trend = trend, popular = popular,
        dailyRating = dailyRating, weeklyRating = weeklyRating,
        monthlyRating = monthlyRating, like = like, set = set,
        download = download, country = country,
        updatedAt = updatedAt, createdAt = createdAt,
        duration = duration)


fun Wallpaper.toEntity() : WallpaperEntity =
    WallpaperEntity( id = id, name = name, thumbnail = thumbnail ?: OBJECT_EMPTY,
        contents = contents, originalId = originalId, type, active, orderIndex, isPrivate, trend, popular, dailyRating, weeklyRating, monthlyRating,
        like, set, download, country, updatedAt,createdAt
)

fun WallpaperEntity.toDomain() : Wallpaper =
    Wallpaper( id = id, name = name, thumbnail = if(thumbnail == OBJECT_EMPTY) null else thumbnail,
        contents = contents, originalId = originalId, type, active, orderIndex, isPrivate, trend, popular, dailyRating, weeklyRating, monthlyRating,
        like, set, download, country, updatedAt,createdAt
)