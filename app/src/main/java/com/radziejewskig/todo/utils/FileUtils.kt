package com.radziejewskig.todo.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FileUtils @Inject constructor(
    val context: Context
) {
    fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )
            // Private, related to app only storage - no storage permissions needed
            val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )
        } catch (e: Exception) {
            null
        }
    }

    fun generateImageName(): String {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return UUID.randomUUID().toString() + "_" + timeStamp + ".jpg"
    }

    fun resizeBitmap(bitmap: Bitmap): Bitmap {
        var image = bitmap
        val maxWidth = maxImageWidth
        val maxHeight = maxImageHeight

        val width = image.width
        val height = image.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        return image
    }

    companion object {
        private const val maxImageWidth: Int = 1280
        private const val maxImageHeight: Int = 1280
    }
}
