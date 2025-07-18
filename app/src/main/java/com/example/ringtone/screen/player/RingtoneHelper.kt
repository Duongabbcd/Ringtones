package com.example.ringtone.screen.player

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object RingtoneHelper {

    fun getMissingMediaPermissions(context: Context): List<String>  {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestWriteSettingsPermission(activity: Activity) {
        if (!Settings.System.canWrite(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${activity.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }

    suspend fun downloadRingtoneFile(context: Context, url: String, title: String): Uri? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            println("downloadRingtoneFile 0: $url")
            println("downloadRingtoneFile 1: $title")
            val safeTitle = title.replace(Regex("[\\\\/:*?\"<>|]"), "").trim()
            val fileName = "$safeTitle.mp3"
            val relativePath = "Ringtones/Ringtone"

            // 🔁 Delete existing entry with same name and path
            fileAlreadyExists(context, fileName, "$relativePath/")?.let {
                resolver.delete(it, null, null)
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.TITLE, safeTitle)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                put(MediaStore.Audio.Media.IS_RINGTONE, true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
            }

            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext null

            resolver.openOutputStream(uri)?.use { output ->
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    resolver.delete(uri, null, null)
                    return@withContext null
                }

                connection.inputStream.use { input -> input.copyTo(output) }
            }

            return@withContext uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun setAsSystemRingtone(context: Context, uri: Uri): Boolean {
        return try {
            if (!Settings.System.canWrite(context)) return false

            Settings.System.putString(context.contentResolver, Settings.System.RINGTONE, uri.toString())

            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                uri
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    fun fileAlreadyExists(context: Context, fileName: String, relativePath: String): Uri? {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        val selectionArgs = arrayOf(fileName, relativePath)

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return ContentUris.withAppendedId(collection, id)
            }
        }
        return null
    }

}
