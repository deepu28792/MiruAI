package com.miruai.app.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface StabilityAiApi {

    @Multipart
    @POST("v2beta/image-to-video")
    suspend fun imageToVideo(
        @Header("Authorization") apiKey: String,
        @Part image: MultipartBody.Part,
        @Part("seed") seed: RequestBody,
        @Part("cfg_scale") cfgScale: RequestBody,
        @Part("motion_bucket_id") motionBucketId: RequestBody
    ): Response<VideoGenerationResponse>

    @GET("v2beta/image-to-video/result/{id}")
    suspend fun getVideoResult(
        @Header("Authorization") apiKey: String,
        @Path("id") generationId: String,
        @Header("Accept") accept: String = "video/*"
    ): Response<ResponseBody>

    @Multipart
    @POST("v2beta/stable-video-diffusion")
    suspend fun textToVideo(
        @Header("Authorization") apiKey: String,
        @Part("prompt") prompt: RequestBody,
        @Part("cfg_scale") cfgScale: RequestBody,
        @Part("motion_bucket_id") motionBucketId: RequestBody,
        @Part("seed") seed: RequestBody
    ): Response<VideoGenerationResponse>
}

data class VideoGenerationResponse(
    val id: String,
    val status: String? = null
)

data class VideoResultResponse(
    val status: String,
    val video: String? = null,
    val finish_reason: String? = null
)
