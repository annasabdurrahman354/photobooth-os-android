package com.askara.photobooth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askara.photobooth.data.model.Profile
import com.askara.photobooth.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            repo.awaitInitialization()
            checkSession()
        }
    }

    private fun checkSession() {
        val userId = repo.getCurrentUserId()
        if (userId != null) {
            _uiState.value = _uiState.value.copy(isSignedIn = true)
            loadProfile(userId)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.signIn(email, password)
                .onSuccess {
                    val userId = repo.getCurrentUserId() ?: return@onSuccess
                    _uiState.value = _uiState.value.copy(isSignedIn = true, isLoading = false)
                    loadProfile(userId)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }

    private fun loadProfile(userId: String) {
        viewModelScope.launch {
            repo.getProfile(userId).onSuccess { profile ->
                _uiState.value = _uiState.value.copy(profile = profile)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repo.signOut()
            _uiState.value = AuthUiState()
        }
    }
}