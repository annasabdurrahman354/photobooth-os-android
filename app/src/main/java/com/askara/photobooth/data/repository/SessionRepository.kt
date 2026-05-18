package com.askara.photobooth.data.repository

import android.annotation.SuppressLint
import com.askara.photobooth.data.model.Session
import com.askara.photobooth.data.model.CaptureInsert
import com.askara.photobooth.data.model.BoothUpdate
import com.askara.photobooth.data.remote.SupabaseClientProvider
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class SessionRepository {
    private val client = SupabaseClientProvider.client

    fun getAccessToken(): String? {
        return client.gotrue.currentSessionOrNull()?.accessToken
    }

    suspend fun createSession(boothId: String, templateId: String): Result<Session> = runCatching {
        withContext(Dispatchers.IO) {
            val shareToken = UUID.randomUUID().toString().substring(0, 8)
            client.postgrest["sessions"].insert(mapOf(
                "booth_id" to boothId,
                "template_id" to templateId,
                "status" to "idle",
                "share_token" to shareToken
            )) {
                select()
            }.decodeSingle<Session>()
        }
    }

    suspend fun uploadCapture(
        sessionId: String,
        captureIndex: Int,
        imageBytes: ByteArray,
        contentType: String = "image/jpeg"
    ): Result<String> = runCatching {
        withContext(Dispatchers.IO) {
            val fileName = "${sessionId}_${captureIndex}.jpg"
            val sanitizedFileName = fileName.removePrefix("/")
            
            client.storage.from("captures").upload(
                path = sanitizedFileName,
                data = imageBytes,
                upsert = true
            )

            client.storage.from("captures").publicUrl(sanitizedFileName)
        }
    }

    suspend fun upsertCapture(sessionId: String, photoUrl: String, captureIndex: Int): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            // First try to delete existing capture at this index to avoid unique constraint violations
            try {
                client.postgrest["captures"].delete {
                    filter {
                        eq("session_id", sessionId)
                        eq("capture_index", captureIndex)
                    }
                }
            } catch (_: Exception) {}

            client.postgrest["captures"].insert(CaptureInsert(
                session_id = sessionId,
                photo_url = photoUrl,
                capture_index = captureIndex
            ))
            Unit
        }
    }

    suspend fun updateBoothStatus(boothId: String, status: String, sessionId: String? = null): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["booths"].update(BoothUpdate(status, sessionId)) {
                filter { eq("id", boothId) }
            }
            Unit
        }
    }

    suspend fun updateSessionStatus(sessionId: String, status: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["sessions"].update(mapOf("status" to status)) {
                filter { eq("id", sessionId) }
            }
            Unit
        }
    }

    suspend fun completeSession(sessionId: String, finalImageUrl: String): Result<Session> = runCatching {
        withContext(Dispatchers.IO) {
            client.postgrest["sessions"].update(mapOf(
                "final_image_url" to finalImageUrl,
                "status" to "completed"
            )) {
                filter { eq("id", sessionId) }
                select()
            }.decodeSingle<Session>()
        }
    }

    suspend fun renderFinalFromServer(serverUrl: String, sessionId: String, templateId: String, photoBytes: List<ByteArray>): Result<RenderResult> = runCatching {
        withContext(Dispatchers.IO) {
            val okhttp = okhttp3.OkHttpClient()
            val requestBodyBuilder = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("template_id", templateId)
                .addFormDataPart("session_id", sessionId)

            photoBytes.forEachIndexed { index, bytes ->
                requestBodyBuilder.addFormDataPart(
                    "photos",
                    "photo_$index.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaType())
                )
            }

            val request = okhttp3.Request.Builder()
                .url("$serverUrl/api/render")
                .post(requestBodyBuilder.build())
                .build()

            val response = okhttp.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty response")

            if (!response.isSuccessful) {
                throw Exception("Render failed: ${response.code} - $responseBody")
            }

            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            json.decodeFromString<RenderResult>(responseBody)
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@kotlinx.serialization.Serializable
data class RenderResult(
    val success: Boolean,
    val session: Session? = null,
    val final_image_url: String? = null,
    val capture_urls: List<String>? = null,
    val error: String? = null
)
