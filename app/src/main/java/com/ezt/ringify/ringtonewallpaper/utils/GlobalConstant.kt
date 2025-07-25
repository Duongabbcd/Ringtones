package com.ezt.ringify.ringtonewallpaper.utils

import android.content.Context
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.screen.language.adapter.Language

object GlobalConstant {
    fun getListLocation(context: Context): ArrayList<Language> {
        val listLanguage: ArrayList<Language> = ArrayList()
        listLanguage.add(Language(R.drawable.english, context.resources.getString(R.string.english), "en"))
        listLanguage.add(Language(R.drawable.arabic, context.resources.getString(R.string.arabic), "ar"))
        listLanguage.add(Language(R.drawable.bengali, context.resources.getString(R.string.bengali),"bn"))
        listLanguage.add(Language(R.drawable.german, context.resources.getString(R.string.german), "de"))
        listLanguage.add(Language(R.drawable.spanish ,context.resources.getString(R.string.spanish),"es"))
        listLanguage.add(Language(R.drawable.french, context.resources.getString(R.string.french), "fr"))
        listLanguage.add(Language(R.drawable.hindi, context.resources.getString(R.string.hindi), "hi"))
        listLanguage.add(Language(R.drawable.indonesian, context.resources.getString(R.string.indonesian),"in"))
        listLanguage.add(Language(R.drawable.portuguese, context.resources.getString(R.string.portuguese), "pt"))
        listLanguage.add(Language(R.drawable.italia, context.resources.getString(R.string.italian), "it"))
        listLanguage.add(Language(R.drawable.russia, context.resources.getString(R.string.russian), "ru"))
        listLanguage.add(Language(R.drawable.korean,context.resources.getString(R.string.korean),"ko"))



        return listLanguage
    }
}