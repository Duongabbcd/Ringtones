package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.crop

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImage.toOvalBitmap
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageView.CropResult
import com.canhub.cropper.CropImageView.OnCropImageCompleteListener
import com.canhub.cropper.CropImageView.OnSetImageUriCompleteListener
import com.ezt.ringify.ringtonewallpaper.base.BaseActivity
import com.ezt.ringify.ringtonewallpaper.databinding.ActivityCropBinding
import com.ezt.ringify.ringtonewallpaper.remote.connection.InternetConnectionViewModel
import com.ezt.ringify.ringtonewallpaper.screen.wallpaper.live.LiveWallpaperActivity
import com.ezt.ringify.ringtonewallpaper.utils.Common.gone
import com.ezt.ringify.ringtonewallpaper.utils.Common.visible
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import kotlin.getValue

class CropActivity : BaseActivity<ActivityCropBinding>(ActivityCropBinding::inflate),
    OnSetImageUriCompleteListener,
    OnCropImageCompleteListener {

    private var options: CropImageOptions? = null
    private val connectionViewModel: InternetConnectionViewModel by viewModels()
    private val imageUrl by lazy {
        intent.getStringExtra("imageUrl")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionViewModel.isConnectedLiveData.observe(this@CropActivity) { isConnected ->
            println("isConnected: $isConnected")
            checkInternetConnected(isConnected)
        }

    }

    private fun loadRemoteImageIntoCropView(
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
                    Log.e("CropActivity", "Failed to download image: ${response.code}")
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
            Toast.makeText(this, "Image load failed: " + error.message, Toast.LENGTH_LONG).show()
        } else {
            binding.cropImageView.doOnLayout {
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels

                binding.cropImageView.apply {
                    setFixedAspectRatio(true)
                    setAspectRatio(screenWidth, screenHeight)
                }
            }
        }
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
                val file = File.createTempFile("cropped_", ".jpg", cacheDir)
                val out = file.outputStream()
                croppedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                val resultIntent = Intent().apply {
                    putExtra("croppedImageUri", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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


    private fun checkInternetConnected(isConnected: Boolean = true) {
        if (!isConnected) {
            binding.origin.gone()
            binding.noInternet.root.visible()
        } else {
            binding.origin.visible()
            binding.apply {
                // Attach listeners
                cropImageView.setOnSetImageUriCompleteListener(this@CropActivity)
                cropImageView.setOnCropImageCompleteListener(this@CropActivity)

                imageUrl?.let {
                    loadRemoteImageIntoCropView(this@CropActivity, it, cropImageView)
                }

                btnReset.setOnClickListener {
                    cropImageView.resetCropRect()
                    imageUrl?.let {
                        loadRemoteImageIntoCropView(this@CropActivity, it, cropImageView)
                    }
                }

                btnCrop.setOnClickListener {
                    cropImageView.croppedImageAsync()
                }
            }
            binding.noInternet.root.gone()
        }
    }

    companion object {
        var croppedImageBitmap: Bitmap? = null
    }
}
