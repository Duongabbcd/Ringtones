package com.example.ringtone.utils

import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.remote.model.Wallpaper

object RingtonePlayerRemote {
    val allSelectedRingtones : MutableList<Ringtone> = mutableListOf()
    val allSelectedWallpapers: MutableList<Wallpaper> = mutableListOf()
    var currentPlayingRingtone = Ringtone.EMPTY_RINGTONE
    var currentPlayingWallpaper = Wallpaper.EMPTY_WALLPAPER

    fun setRingtoneQueue(list: List<Ringtone>) {
        allSelectedRingtones.clear()
        allSelectedRingtones.addAll(list)
    }


    fun setWallpaperQueue(list: List<Wallpaper>) {
        allSelectedWallpapers.clear()
        allSelectedWallpapers.addAll(list)
    }

    fun setCurrentRingtone(ringtone: Ringtone) {
        currentPlayingRingtone = ringtone
    }

    fun setCurrentWallpaper(wallpaper: Wallpaper) {
        currentPlayingWallpaper = wallpaper
    }
}