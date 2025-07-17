package com.example.ringtone.local

import android.app.Application
import androidx.room.Room
import com.example.ringtone.local.dao.RingtoneDao
import com.example.ringtone.local.dao.WallpaperDao
import com.example.ringtone.local.database.FavouriteDatabase
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
}
