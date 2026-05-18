package com.askara.photobooth.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Profile(
    val id: String,
    val email: String,
    val role: String,
    val tenant_id: String?,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Booth(
    val id: String,
    val tenant_id: String,
    val name: String,
    val status: String,
    val current_session_id: String?,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BoothUpdate(
    val status: String,
    val current_session_id: String?
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Template(
    val id: String,
    val tenant_id: String,
    val name: String,
    val html_content: String?,
    val css_content: String?,
    val layout_json: String?,
    val thumbnail_url: String?,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Session(
    val id: String,
    val booth_id: String,
    val template_id: String,
    val status: String,
    val share_token: String,
    val final_image_url: String?,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Capture(
    val id: String,
    val session_id: String,
    val photo_url: String,
    val capture_index: Int,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CaptureInsert(
    val session_id: String,
    val photo_url: String,
    val capture_index: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Sticker(
    val id: String,
    val tenant_id: String?,
    val name: String,
    val url: String,
    val created_at: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LayoutJson(
    val width: Int = 400,
    val height: Int = 600,
    val canvasBg: String = "#ffffff",
    val canvasBgImage: String? = null,
    val dpi: Int = 300,
    val elements: List<ElementJson> = emptyList()
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ElementJson(
    val id: String,
    val type: String,
    val x: Int = 0,
    val y: Int = 0,
    val width: Int? = null,
    val height: Int? = null,
    val content: String? = null,
    val fontSize: Int? = null,
    val color: String? = null,
    val url: String? = null,
    val zIndex: Int? = null,
    val fontFamily: String? = null,
    val fontWeight: String? = null,
    val fontStyle: String? = null,
    val textAlign: String? = null,
    val textTransform: String? = null,
    val wrapText: Boolean? = null,
    val rotation: Int? = null,
    val opacity: Int? = null,
    val borderWidth: Int? = null,
    val borderColor: String? = null,
    val borderRadius: Int? = null,
    val shadowEnabled: Boolean? = null,
    val shadowColor: String? = null,
    val shadowBlur: Int? = null,
    val shadowOffsetX: Int? = null,
    val shadowOffsetY: Int? = null,
    val shadowOpacity: Int? = null,
    val ratioLock: Boolean? = null,
    val aspectRatio: Double? = null
)