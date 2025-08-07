package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object RingtoneHelper {

    fun getMissingAudioPermissions(context: Context): List<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun getMissingPhotoPermissions(context: Context) : List<String> {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestWriteSettingsPermission(activity: Activity) {
        if (!Settings.System.canWrite(activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${activity.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }

    fun hasWriteSettingsPermission(context: Context): Boolean {
        return Settings.System.canWrite(context).also {
            println("hasWriteSettingsPermission: $it")
        }
    }

    suspend fun downloadRingtoneFile(
        context: Context,
        url: String,
        title: String,
        onProgress: (Int) -> Unit  // progress 0-100
    ): Uri? =
        withContext(Dispatchers.IO) {
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

                val uri =
                    resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?: return@withContext null

                resolver.openOutputStream(uri)?.use { output ->
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connect()

                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        resolver.delete(uri, null, null)
                        return@withContext null
                    }

                    val totalSize = connection.contentLength
                    var downloadedSize = 0

                    connection.inputStream.use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } >= 0) {
                            output.write(buffer, 0, bytesRead)
                            downloadedSize += bytesRead
                            val progress = if (totalSize > 0) (downloadedSize * 100 / totalSize) else 0
                            onProgress(progress)
                        }
                    }
                }

                return@withContext uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun downloadImage(context: Context, imageUrl: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val fileName = "image_${imageUrl.hashCode()}.jpg" // safer file name

                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                // Query for existing file
                val selection =
                    "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs = arrayOf(
                    fileName,
                    "${Environment.DIRECTORY_PICTURES}/"
                )

                resolver.query(
                    collection,
                    arrayOf(MediaStore.MediaColumns._ID),
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val existingUri = ContentUris.withAppendedId(collection, id)
                        // Delete the existing file
                        resolver.delete(existingUri, null, null)
                    }
                }

                // Prepare new image to insert
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val uri = resolver.insert(collection, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        URL(imageUrl).openStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    suspend fun downloadVideo(
        context: Context,
        videoUrl: String,
        fileName: String = "video_$videoUrl.mp4"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver

            // Delete existing file (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(fileName)

                // Query existing file
                val cursor = resolver.query(
                    collection,
                    arrayOf(MediaStore.Downloads._ID),
                    selection,
                    selectionArgs,
                    null
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                        val deleteUri = ContentUris.withAppendedId(collection, id)
                        resolver.delete(deleteUri, null, null)
                    }
                }
            } else {
                // Delete file manually for Android 9 and below
                val downloadsPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsPath, fileName)
                if (file.exists()) {
                    file.delete()
                }
            }

            // Start connection
            val connection = URL(videoUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return@withContext null
            }

            val inputStream = connection.inputStream

            val savedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = resolver.insert(collection, contentValues)

                if (itemUri == null) {
                    inputStream.close()
                    return@withContext null
                }

                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)

                itemUri
            } else {
                val downloadsPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsPath.exists()) downloadsPath.mkdirs()
                val file = File(downloadsPath, fileName)

                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()

                Uri.fromFile(file)
            }

            connection.disconnect()
            savedUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun setWallpaperFromUrl(
        context: Context,
        bitmap: Bitmap,
        target: WallpaperTarget = WallpaperTarget.BOTH
    ) : Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val wallpaperManager = WallpaperManager.getInstance(context)
//                val resizeBitmap = resizeBitmapToScreen(context, bitmap)
                val resizeBitmap = bitmap
                when (target) {
                    WallpaperTarget.HOME -> {
                        wallpaperManager.setBitmap(
                            resizeBitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM
                        )
                    }
                    WallpaperTarget.LOCK -> {
                        wallpaperManager.setBitmap(
                            resizeBitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_LOCK
                        )
                    }
                    WallpaperTarget.BOTH -> {
                        wallpaperManager.setBitmap(
                            resizeBitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM
                        )
                        wallpaperManager.setBitmap(
                            resizeBitmap,
                            null,
                            true,
                            WallpaperManager.FLAG_LOCK
                        )
                    }
                }

                true

            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun resizeBitmapToScreen(
        context: Context,
        bitmap: Bitmap
    ): Bitmap {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        val display = wm.defaultDisplay
        val size = android.graphics.Point()
        display.getRealSize(size)
        val screenWidth = size.x
        val screenHeight = size.y

        return Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true)
    }



    fun setAsSystemRingtone(context: Context, uri: Uri, isNotification: Boolean =  false): Boolean {
        return try {
            if (!Settings.System.canWrite(context)) return false

            Settings.System.putString(
                context.contentResolver,
                Settings.System.RINGTONE,
                uri.toString()
            )

            val type = if(isNotification) RingtoneManager.TYPE_NOTIFICATION else RingtoneManager.TYPE_RINGTONE

            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                type, //  RingtoneManager.TYPE_NOTIFICATION,
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
        val selection =
            "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
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

enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH
}