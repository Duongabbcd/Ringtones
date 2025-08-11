package com.ezt.ringify.ringtonewallpaper.local

import android.app.Application
import androidx.room.Room
import com.ezt.ringify.ringtonewallpaper.local.dao.LiveWallpaperDao
import com.ezt.ringify.ringtonewallpaper.local.dao.RingtoneDao
import com.ezt.ringify.ringtonewallpaper.local.dao.SlideWallpaperDao
import com.ezt.ringify.ringtonewallpaper.local.dao.WallpaperDao
import com.ezt.ringify.ringtonewallpaper.local.database.FavouriteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFavouriteDatabase(app: Application): FavouriteDatabase {
        return Room.databaseBuilder(app, FavouriteDatabase::class.java, "favourite_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideRingtoneDao(db: FavouriteDatabase): RingtoneDao = db.ringToneDao()

    @Provides
    @Singleton
    fun provideWallpaperDao(db: FavouriteDatabase): WallpaperDao = db.wallPaperDao()


    @Provides
    @Singleton
    fun provideLiveWallpaperDao(db: FavouriteDatabase): LiveWallpaperDao = db.liveWallpaperDao()


    @Provides
    @Singleton
    fun provideSlideWallpaperDao(db: FavouriteDatabase): SlideWallpaperDao = db.slideWallpaperDao()
}
