package com.phpthinky.screencapture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.nativephp.mobile.bridge.BridgeFunction
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen capture functions for saving images directly to device storage
 * Namespace: "ScreenCapture.*"
 */
object ScreenCaptureFunctions {

    /**
     * Save a base64 encoded image directly to device storage
     * 
     * Parameters:
     *   - data: string - Base64 encoded image data (with or without data URL prefix)
     *   - filename: string (optional) - Custom filename (auto-generated if not provided)
     *   - path: string (optional) - Custom path (default: Pictures/Screenshots)
     * 
     * Returns:
     *   - success: boolean - Whether the operation succeeded
     *   - path: string - The path where the file was saved
     *   - error: string (optional) - Error message if operation failed
     */
    class SaveBase64(private val activity: FragmentActivity) : BridgeFunction {
        override fun execute(parameters: Map<String, Any>): Map<String, Any> {
            val rawData = parameters["data"] as? String
            var filename = parameters["filename"] as? String
            val customPath = parameters["path"] as? String
            val quality = (parameters["quality"] as? Number)?.toInt()?.coerceIn(1, 100) ?: 85

            Log.d("ScreenCapture.SaveBase64", "Saving base64 image - filename: $filename, path: $customPath, quality: $quality")

            if (rawData.isNullOrEmpty()) {
                return mapOf("success" to false, "error" to "'data' parameter is required")
            }

            try {
                // Remove data URL prefix if present (e.g., "data:image/png;base64,")
                var base64Data = rawData
                if (base64Data.contains(",")) {
                    base64Data = base64Data.substringAfter(",")
                }

                // Decode base64 to bytes
                val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)

                // Compress image to JPEG to reduce memory usage before saving
                val compressedBytes = compressBitmap(imageBytes, quality)
                    ?: return mapOf("success" to false, "error" to "Failed to compress image - insufficient memory")

                // Generate filename if not provided
                if (filename.isNullOrEmpty()) {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    filename = "screenshot_$timestamp.jpg"
                }

                // Ensure .jpg extension
                if (!filename.endsWith(".jpg", ignoreCase = true) && !filename.endsWith(".jpeg", ignoreCase = true)) {
                    filename = filename.substringBeforeLast(".").let { base ->
                        if (base == filename) "$filename.jpg" else "$base.jpg"
                    }
                }

                // Save the compressed image
                val savedPath = if (customPath != null) {
                    saveToCustomPath(activity.applicationContext, compressedBytes, filename, customPath)
                } else {
                    saveToMediaStore(activity.applicationContext, compressedBytes, filename)
                }

                if (savedPath != null) {
                    Log.d("ScreenCapture.SaveBase64", "Image saved successfully: $savedPath")
                    return mapOf(
                        "success" to true,
                        "path" to savedPath,
                        "filename" to filename
                    )
                } else {
                    return mapOf("success" to false, "error" to "Failed to save image")
                }

            } catch (e: Exception) {
                Log.e("ScreenCapture.SaveBase64", "Error saving image: ${e.message}", e)
                return mapOf("success" to false, "error" to (e.message ?: "Unknown error"))
            }
        }

        private fun compressBitmap(imageBytes: ByteArray, quality: Int): ByteArray? {
            return try {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: return null
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                bitmap.recycle()
                outputStream.toByteArray()
            } catch (e: OutOfMemoryError) {
                Log.e("ScreenCapture", "Out of memory compressing image", e)
                null
            }
        }

        private fun saveToMediaStore(context: Context, imageBytes: ByteArray, filename: String): String? {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ using MediaStore API (works with scoped storage)
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Screenshots")
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(imageBytes)
                            outputStream.flush()
                            return uri.toString()
                        }
                    }
                    null
                } else {
                    // Android 9 and below using external storage
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val screenshotsDir = File(picturesDir, "Screenshots")
                    
                    if (!screenshotsDir.exists()) {
                        screenshotsDir.mkdirs()
                    }

                    val imageFile = File(screenshotsDir, filename)
                    FileOutputStream(imageFile).use { outputStream ->
                        outputStream.write(imageBytes)
                        outputStream.flush()
                    }

                    // Scan media to appear in gallery
                    android.media.MediaScannerConnection.scanFile(
                        context,
                        arrayOf(imageFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )

                    imageFile.absolutePath
                }
            } catch (e: Exception) {
                Log.e("ScreenCapture", "Error saving to MediaStore: ${e.message}", e)
                null
            }
        }

        private fun saveToCustomPath(context: Context, imageBytes: ByteArray, filename: String, customPath: String): String? {
            return try {
                val file = File(customPath, filename)
                
                // Create directories if they don't exist
                file.parentFile?.let { parent ->
                    if (!parent.exists()) {
                        parent.mkdirs()
                    }
                }

                FileOutputStream(file).use { outputStream ->
                    outputStream.write(imageBytes)
                    outputStream.flush()
                }

                // Scan media to appear in gallery
                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )

                file.absolutePath
            } catch (e: Exception) {
                Log.e("ScreenCapture", "Error saving to custom path: ${e.message}", e)
                null
            }
        }
    }
}