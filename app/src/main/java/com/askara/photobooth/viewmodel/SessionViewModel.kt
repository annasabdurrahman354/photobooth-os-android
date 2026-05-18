package com.askara.photobooth.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askara.photobooth.BuildConfig
import com.askara.photobooth.data.model.Session
import com.askara.photobooth.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class SessionUiState(
    val isLoading: Boolean = false,
    val session: Session? = null,
    val captures: List<ByteArray> = emptyList(),
    val totalSlots: Int = 0,
    val currentState: SessionState = SessionState.IDLE,
    val finalImageUrl: String? = null,
    val selectedPreviewIndex: Int? = null,
    val renderServerUrl: String = BuildConfig.RENDER_SERVER_URL,
    val error: String? = null,
)

enum class SessionState {
    IDLE, SHOOTING, REVIEW, RENDERING, DONE
}

class SessionViewModel : ViewModel() {
    private val repo = SessionRepository()

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState

    private fun syncStatus(status: String) {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch { repo.updateSessionStatus(sessionId, status) }
    }

    fun initSession(boothId: String, templateId: String, totalSlots: Int) {
        _uiState.value = _uiState.value.copy(totalSlots = totalSlots, isLoading = true)
        viewModelScope.launch {
            repo.createSession(boothId, templateId)
                .onSuccess { session ->
                    repo.updateBoothStatus(boothId, "in_session", session.id)
                    _uiState.value = _uiState.value.copy(session = session, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }

    fun startShooting() {
        _uiState.value = _uiState.value.copy(currentState = SessionState.SHOOTING)
        syncStatus("shooting")
    }

    fun setState(state: SessionState) {
        _uiState.value = _uiState.value.copy(currentState = state)
        when (state) {
            SessionState.SHOOTING -> syncStatus("shooting")
            SessionState.REVIEW -> syncStatus("review")
            SessionState.RENDERING -> syncStatus("rendering")
            SessionState.DONE -> syncStatus("completed")
            else -> {}
        }
    }

    fun selectPreview(index: Int?) {
        _uiState.value = _uiState.value.copy(selectedPreviewIndex = index)
    }

    fun addCapture(bitmap: Bitmap) {
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                compressBitmap(bitmap)
            }
            val current = _uiState.value.captures.toMutableList()
            current.add(bytes)
            _uiState.value = _uiState.value.copy(captures = current)
        }
    }

    fun retakeCapture(index: Int, bitmap: Bitmap) {
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) { compressBitmap(bitmap) }
            val current = _uiState.value.captures.toMutableList()
            current[index] = bytes
            _uiState.value = _uiState.value.copy(
                captures = current,
                currentState = SessionState.SHOOTING
            )
            _uiState.value = _uiState.value.copy(currentState = SessionState.REVIEW)
        }
    }

    fun goToReview() {
        if (_uiState.value.captures.size >= _uiState.value.totalSlots) {
            _uiState.value = _uiState.value.copy(currentState = SessionState.REVIEW)
            syncStatus("review")
        }
    }

    fun retakeAll() {
        _uiState.value = _uiState.value.copy(captures = emptyList(), currentState = SessionState.SHOOTING)
        syncStatus("shooting")
    }

    fun confirmAndUpload(onSuccess: () -> Unit) {
        val session = _uiState.value.session ?: return
        val captures = _uiState.value.captures
        
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        syncStatus("uploading")
        
        viewModelScope.launch {
            try {
                captures.forEachIndexed { index, bytes ->
                    val uploadResult = repo.uploadCapture(session.id, index, bytes)
                    
                    if (uploadResult.isSuccess) {
                        val url = uploadResult.getOrThrow()
                        val dbResult = repo.upsertCapture(session.id, url, index)
                        if (dbResult.isFailure) {
                            throw Exception("Failed to save photo ${index + 1}: ${dbResult.exceptionOrNull()?.message}")
                        }
                    } else {
                        throw Exception("Failed to upload photo ${index + 1}: ${uploadResult.exceptionOrNull()?.message}")
                    }
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                syncStatus("review")
            }
        }
    }

    fun renderFinal() {
        val session = _uiState.value.session ?: return
        val templateId = session.template_id
        _uiState.value = _uiState.value.copy(currentState = SessionState.RENDERING, isLoading = true)
        syncStatus("rendering")

        viewModelScope.launch {
            repo.renderFinalFromServer(
                serverUrl = _uiState.value.renderServerUrl,
                sessionId = session.id,
                templateId = templateId,
                photoBytes = _uiState.value.captures
            )
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        currentState = SessionState.DONE,
                        finalImageUrl = result.final_image_url,
                        session = result.session,
                        isLoading = false
                    )
                    syncStatus("completed")
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        currentState = SessionState.REVIEW,
                        isLoading = false,
                        error = it.message
                    )
                    syncStatus("review")
                }
        }
    }

    fun resetSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            repo.updateBoothStatus(session.booth_id, "online", null)
        }
        _uiState.value = SessionUiState()
    }

    fun onRenderComplete(finalImageUrl: String) {
        _uiState.value = _uiState.value.copy(
            currentState = SessionState.DONE,
            finalImageUrl = finalImageUrl,
            isLoading = false,
            error = null
        )
        syncStatus("completed")
    }

    fun onRenderError(error: String) {
        _uiState.value = _uiState.value.copy(
            error = error,
            isLoading = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getAccessToken(): String? {
        return repo.getAccessToken()
    }

    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        var quality = 90
        var outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        
        val targetSize = 500 * 1024
        
        while ((outputStream.toByteArray().size > targetSize) && (quality > 20)) {
            outputStream = ByteArrayOutputStream()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }
        
        return outputStream.toByteArray()
    }
}
