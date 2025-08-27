package com.ezt.ringify.ringtonewallpaper.remote.repository

import com.ezt.ringify.ringtonewallpaper.local.LiveWallpaperEntity
import com.ezt.ringify.ringtonewallpaper.local.RingtoneEntity
import com.ezt.ringify.ringtonewallpaper.local.SlideWallpaperEntity
import com.ezt.ringify.ringtonewallpaper.local.WallpaperEntity
import com.ezt.ringify.ringtonewallpaper.local.dao.LiveWallpaperDao
import com.ezt.ringify.ringtonewallpaper.local.dao.RingtoneDao
import com.ezt.ringify.ringtonewallpaper.local.dao.SlideWallpaperDao
import com.ezt.ringify.ringtonewallpaper.local.dao.WallpaperDao
import com.ezt.ringify.ringtonewallpaper.remote.model.Author
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.remote.model.Wallpaper
import com.ezt.ringify.ringtonewallpaper.remote.model.WallpaperContent.Companion.OBJECT_EMPTY
import javax.inject.Inject

class FavouriteRepository @Inject constructor(
    private val ringtoneDao: RingtoneDao,
    private val wallpaperDao: WallpaperDao,
    private val liveWallpaperDao: LiveWallpaperDao,
    private val slideWallpaperDao: SlideWallpaperDao,
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


    suspend fun getAllWallpapers(limit: Int = 5): List<Wallpaper> {
        val list = wallpaperDao.getAllWallpaper(limit)
        println("getAllWallpapers: $list")
        return list?.map { it.toDomain() } ?: listOf()
    }


    suspend fun getAllLiveWallpapers(limit: Int = 5): List<Wallpaper> {
        val list = liveWallpaperDao.getAllWallpaper(limit)
        println("getAllLiveWallpapers: $list")
        return list?.map { it.toDomain() } ?: listOf()
    }

    suspend fun getAllSlideWallpapers(limit: Int = 5): List<Wallpaper> {
        val list = slideWallpaperDao.getAllWallpaper(limit)
        println("getAllSlideWallpapers: $list")
        return list?.map { it.toDomain() } ?: listOf()
    }

    suspend fun getWallpaperById(id: Int): Wallpaper =
        wallpaperDao.getById(id)?.toDomain() ?: Wallpaper.EMPTY_WALLPAPER

    suspend fun getLiveWallpaperById(id: Int): Wallpaper =
        liveWallpaperDao.getById(id)?.toDomain() ?: Wallpaper.EMPTY_WALLPAPER


    suspend fun getSlideWallpaperById(id: Int): Wallpaper {
        val origin = slideWallpaperDao.getById(id)
        println("getSlideWallpaperById: $origin")
        return origin?.toDomain() ?: Wallpaper.EMPTY_WALLPAPER
    }

    suspend fun insertWallpaper(wallpaper: Wallpaper) =
        wallpaperDao.insert(wallpaper.toEntity())

    suspend fun deleteWallpaper(wallpaper: Wallpaper) =
        wallpaperDao.delete(wallpaper.toEntity())

    suspend fun insertLiveWallpaper(wallpaper: Wallpaper) =
        liveWallpaperDao.insert(wallpaper.toLiveEntity())

    suspend fun deleteLiveWallpaper(wallpaper: Wallpaper) =
        liveWallpaperDao.delete(wallpaper.toLiveEntity())

    suspend fun insertSlideWallpaper(wallpaper: Wallpaper) =
        slideWallpaperDao.insert(wallpaper.toSlideEntity())

    suspend fun deleteSlideWallpaper(wallpaper: Wallpaper) =
        slideWallpaperDao.delete(wallpaper.toSlideEntity())
}


fun Ringtone.toEntity() : RingtoneEntity =
    RingtoneEntity( id = id, name = name, contents = contents,
        author = author ?: Author(-1, "", 0), category = categories ?: listOf(),
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

fun Wallpaper.toLiveEntity(): LiveWallpaperEntity =
    LiveWallpaperEntity(
        id = id,
        name = name,
        thumbnail = thumbnail ?: OBJECT_EMPTY,
        contents = contents,
        originalId = originalId,
        type,
        active,
        orderIndex,
        isPrivate,
        trend,
        popular,
        dailyRating,
        weeklyRating,
        monthlyRating,
        like,
        set,
        download,
        country,
        updatedAt,
        createdAt
    )

fun Wallpaper.toSlideEntity(): SlideWallpaperEntity =
    SlideWallpaperEntity(
        id = id,
        name = name,
        thumbnail = thumbnail ?: OBJECT_EMPTY,
        contents = contents,
        originalId = originalId,
        type,
        active,
        orderIndex,
        isPrivate,
        trend,
        popular,
        dailyRating,
        weeklyRating,
        monthlyRating,
        like,
        set,
        download,
        country,
        updatedAt,
        createdAt
    )

fun WallpaperEntity.toDomain() : Wallpaper =
    Wallpaper( id = id, name = name, thumbnail = if(thumbnail == OBJECT_EMPTY) null else thumbnail,
        contents = contents, originalId = originalId, type, active, orderIndex, isPrivate, trend, popular, dailyRating, weeklyRating, monthlyRating,
        like, set, download, country, updatedAt,createdAt
)

fun LiveWallpaperEntity.toDomain(): Wallpaper =
    Wallpaper(
        id = id,
        name = name,
        thumbnail = if (thumbnail == OBJECT_EMPTY) null else thumbnail,
        contents = contents,
        originalId = originalId,
        type,
        active,
        orderIndex,
        isPrivate,
        trend,
        popular,
        dailyRating,
        weeklyRating,
        monthlyRating,
        like,
        set,
        download,
        country,
        updatedAt,
        createdAt
    )


fun SlideWallpaperEntity.toDomain(): Wallpaper =
    Wallpaper(
        id = id,
        name = name,
        thumbnail = if (thumbnail == OBJECT_EMPTY) null else thumbnail,
        contents = contents,
        originalId = originalId,
        type,
        active,
        orderIndex,
        isPrivate,
        trend,
        popular,
        dailyRating,
        weeklyRating,
        monthlyRating,
        like,
        set,
        download,
        country,
        updatedAt,
        createdAt
    )