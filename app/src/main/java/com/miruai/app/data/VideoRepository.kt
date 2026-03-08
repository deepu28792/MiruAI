package com.miruai.app.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import com.miruai.app.data.api.RetrofitClient
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

sealed class VideoResult {
    data class Success(val videoUrl: String) : VideoResult()
    data class Error(val message: String) : VideoResult()
    object Processing : VideoResult()
}

class VideoRepository(private val context: Context) {

    private val api = RetrofitClient.stabilityApi

    suspend fun generateFromText(
        apiKey: String,
        prompt: String,
        style: String,
        durationSeconds: Int
    ): VideoResult {
        return try {
            val fullPrompt = if (style != "Cinematic") "$prompt, $style style" else prompt
            val promptBody = fullPrompt.toRequestBody("text/plain".toMediaTypeOrNull())
            val outputFormatBody = "jpeg".toRequestBody("text/plain".toMediaTypeOrNull())
            val aspectRatioBody = "16:9".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.textToImage(
                apiKey = "Bearer $apiKey",
                prompt = promptBody,
                outputFormat = outputFormatBody,
                aspectRatio = aspectRatioBody
            )

            if (response.isSuccessful) {
                val imageBase64 = response.body()?.image
                if (imageBase64 != null) {
                    val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                    val file = saveImageToCache(imageBytes, System.currentTimeMillis().toString())
                    VideoResult.Success(file)
                } else {
                    VideoResult.Error("No image data received")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                VideoResult.Error("API Error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            VideoResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun generateFromImage(
        apiKey: String,
        imageUri: Uri,
        motionStrength: Int,
        motionPrompt: String?
    ): VideoResult {
        return try {
            val bitmap = loadBitmapFromUri(imageUri)
                ?: return VideoResult.Error("Failed to load image")
            val imageBytes = bitmapToBytes(bitmap)
            val imageRequestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", imageRequestBody)

            val motionBucketId = (motionStrength * 1.27).toInt().coerceIn(1, 127)
            val seedBody = "0".toRequestBody("text/plain".toMediaTypeOrNull())
            val cfgScaleBody = "2.5".toRequestBody("text/plain".toMediaTypeOrNull())
            val motionBody = motionBucketId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.imageToVideo(
                apiKey = "Bearer $apiKey",
                image = imagePart,
                seed = seedBody,
                cfgScale = cfgScaleBody,
                motionBucketId = motionBody
            )

            if (response.isSuccessful) {
                val generationId = response.body()?.id
                if (generationId != null) {
                    pollForResult(apiKey, generationId)
                } else {
                    VideoResult.Error("No generation ID received")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                VideoResult.Error("API Error ${response.code()}: $errorBody")
            }
        } catch (e: Exception) {
            VideoResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    private suspend fun pollForResult(apiKey: String, generationId: String): VideoResult {
        var attempts = 0
        val maxAttempts = 30

        while (attempts < maxAttempts) {
            delay(4000L)
            attempts++

            try {
                val resultResponse = api.getVideoResult(
                    apiKey = "Bearer $apiKey",
                    generationId = generationId
                )

                when {
                    resultResponse.isSuccessful -> {
                        val bytes = resultResponse.body()?.bytes()
                        if (bytes != null) {
                            val file = saveVideoToCache(bytes, generationId)
                            return VideoResult.Success(file)
                        }
                    }
                    resultResponse.code() == 202 -> continue
                    else -> return VideoResult.Error("Error ${resultResponse.code()}: ${resultResponse.message()}")
                }
            } catch (e: Exception) {
                if (attempts >= maxAttempts) {
                    return VideoResult.Error("Timed out waiting for video")
                }
            }
        }
        return VideoResult.Error("Generation timed out. Please try again.")
    }

    private fun saveImageToCache(bytes: ByteArray, id: String): String {
        val file = java.io.File(context.cacheDir, "miru_image_$id.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private fun saveVideoToCache(bytes: ByteArray, id: String): String {
        val file = java.io.File(context.cacheDir, "miru_video_$id.mp4")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) { null }
    }

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        val maxSize = 1024
        val resized = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else bitmap
        resized.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        return stream.toByteArray()
    }
}
