package com.example.ringtone.utils

import com.example.ringtone.remote.model.Ringtone

object RingtonePlayerRemote {
    val allSelectedRingtones : MutableList<Ringtone> = mutableListOf()
    var currentPlayingRingtone = Ringtone.EMPTY_RINGTONE

    fun setPlayingQueue(list: List<Ringtone>) {
        allSelectedRingtones.clear()
        allSelectedRingtones.addAll(list)
    }

    fun setCurrentRingtone(ringtone: Ringtone) {
        currentPlayingRingtone = ringtone
    }


}