package com.ezt.ringify.ringtonewallpaper.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.ezt.ringify.ringtonewallpaper.local.LiveWallpaperEntity
import com.ezt.ringify.ringtonewallpaper.local.RingtoneEntity
import com.ezt.ringify.ringtonewallpaper.local.WallpaperEntity
import com.ezt.ringify.ringtonewallpaper.local.dao.LiveWallpaperDao
import com.ezt.ringify.ringtonewallpaper.local.dao.RingtoneDao
import com.ezt.ringify.ringtonewallpaper.local.dao.WallpaperDao
import com.ezt.ringify.ringtonewallpaper.remote.model.Author
import com.ezt.ringify.ringtonewallpaper.remote.model.Category
import com.ezt.ringify.ringtonewallpaper.remote.model.ImageContent
import com.ezt.ringify.ringtonewallpaper.remote.model.RingtoneContents
import com.ezt.ringify.ringtonewallpaper.remote.model.WallpaperContent
import com.google.common.reflect.TypeToken
import com.google.gson.Gson


@Database(
    entities = [RingtoneEntity::class, WallpaperEntity::class, LiveWallpaperEntity::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class FavouriteDatabase() : RoomDatabase() {
    abstract fun ringToneDao(): RingtoneDao
    abstract fun wallPaperDao(): WallpaperDao
    abstract fun liveWallpaperDao(): LiveWallpaperDao


    companion object {
        @Volatile
        private var instance: FavouriteDatabase? = null

        fun getInstance(context: Context): FavouriteDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FavouriteDatabase::class.java,
                    "FavouriteDatabase"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }

}

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun ringtoneContentsToJson(contents: RingtoneContents): String = gson.toJson(contents)
    @TypeConverter
    fun ringtoneContentsFromJson(json: String): RingtoneContents =
        gson.fromJson(json, RingtoneContents::class.java)

    @TypeConverter
    fun authorToJson(author: Author): String = gson.toJson(author)
    @TypeConverter
    fun authorFromJson(json: String): Author = gson.fromJson(json, Author::class.java)

    @TypeConverter
    fun categoryToJson(category: List<Category>): String = gson.toJson(category)
    @TypeConverter
    fun categoryFromJson(json: String): List<Category> = gson.fromJson(json, object : TypeToken<List<Category>>() {}.type)

    @TypeConverter
    fun wallpaperContentToJson(thumbnail: WallpaperContent): String = gson.toJson(thumbnail)
    @TypeConverter
    fun wallpaperContentFromJson(json: String): WallpaperContent =
        gson.fromJson(json, WallpaperContent::class.java)

    @TypeConverter
    fun contentsToJson(contents: List<ImageContent>): String = gson.toJson(contents)
    @TypeConverter
    fun contentsFromJson(json: String): List<ImageContent> =
        gson.fromJson(json, object : TypeToken<List<ImageContent>>() {}.type)
}