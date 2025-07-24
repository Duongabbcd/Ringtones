package com.example.ringtone.screen.wallpaper.crop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImage.toOvalBitmap
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageView.CropResult
import com.canhub.cropper.CropImageView.OnCropImageCompleteListener
import com.canhub.cropper.CropImageView.OnSetImageUriCompleteListener
import com.example.ringtone.base.BaseActivity
import com.example.ringtone.databinding.ActivityCropBinding
import com.example.ringtone.screen.wallpaper.player.WallpaperActivity.Companion.imageBitmap
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File


class CropActivity : BaseActivity<ActivityCropBinding>(ActivityCropBinding::inflate),
    OnSetImageUriCompleteListener,
    OnCropImageCompleteListener {
    private var options: CropImageOptions? = null

    private val imageUrl by lazy {
        intent.getStringExtra("imageUrl")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            setOptions()
            imageUrl?.let {
                loadRemoteImageIntoCropView(this@CropActivity, it, cropImageView)
            }


            // Set the listener BEFORE calling croppedImageAsync()
            binding.cropImageView.setOnCropImageCompleteListener { view, result ->
                if (result.error == null) {
                    // Crop success! Get the cropped bitmap
                    val croppedBitmap =
                        if (binding.cropImageView.cropShape == CropImageView.CropShape.OVAL) {
                            result.bitmap?.let(CropImage::toOvalBitmap)
                        } else {
                            result.bitmap
                        }

                    if (croppedBitmap != null) {
                        val file = File.createTempFile("cropped_", ".jpg", cacheDir)
                        file.outputStream().use { out ->
                            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        }

                        val uri = FileProvider.getUriForFile(
                            this@CropActivity,
                            "${packageName}.fileprovider", // must match manifest provider
                            file
                        )

                        val intent = Intent().apply {
                            putExtra("croppedImageUri", uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        setResult(RESULT_OK, intent)
                    } else {
                        setResult(RESULT_CANCELED)
                    }
                    // Finish the activity
                    finish()
                } else {
                    // Crop failed
                    Toast.makeText(
                        this@CropActivity,
                        "Crop failed: ${result.error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnReset.setOnClickListener {
                binding.cropImageView.resetCropRect()
                imageUrl?.let {
                    loadRemoteImageIntoCropView(this@CropActivity, it, cropImageView)
                }

            }

            btnCrop.setOnClickListener {
                binding.cropImageView.croppedImageAsync()
            }

        }
    }

    fun loadRemoteImageIntoCropView(
        context: Context,
        imageUrl: String,
        cropImageView: CropImageView
    ) {
        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(imageUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    println("Failed to download image: ${response.code}")
                    return@Thread
                }

                val tempFile = File.createTempFile("temp_crop_image", ".jpg", context.cacheDir)
                val sink = tempFile.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()

                val imageUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                // Load into cropper (on UI thread)
                (context as? android.app.Activity)?.runOnUiThread {
                    cropImageView.setImageUriAsync(imageUri)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Log.e("CropActivity", "Failed to load image by URI: $error")
            Toast.makeText(this, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                .show()
        }
    }


    private fun setOptions() {
        binding.cropImageView.cropRect = Rect(100, 300, 500, 1200)
    }

    override fun onCropImageComplete(view: CropImageView, result: CropResult) {
        if (result.error == null && result.bitmap != null) {
            val croppedBitmap =
                if (binding.cropImageView.cropShape == CropImageView.CropShape.OVAL) {
                    result.bitmap?.let(CropImage::toOvalBitmap)
                } else {
                    result.bitmap
                }

            try {
                // Save bitmap to a temporary file
                val file = File.createTempFile("cropped_", ".jpg", cacheDir)
                val out = file.outputStream()
                croppedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                // Convert to Uri using FileProvider
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                // Return to caller
                val resultIntent = Intent().apply {
                    putExtra("croppedImageUri", uri)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "Crop failed: ${result.error?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        var croppedImageBitmap: Bitmap? = null
    }

}