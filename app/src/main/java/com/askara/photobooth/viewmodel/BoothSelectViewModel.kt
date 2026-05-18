package com.askara.photobooth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askara.photobooth.data.model.Booth
import com.askara.photobooth.data.repository.BoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BoothSelectUiState(
    val isLoading: Boolean = false,
    val booths: List<Booth> = emptyList(),
    val error: String? = null
)

class BoothSelectViewModel : ViewModel() {
    private val repo = BoothRepository()

    private val _uiState = MutableStateFlow(BoothSelectUiState())
    val uiState: StateFlow<BoothSelectUiState> = _uiState

    fun loadBooths(tenantId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repo.getBooths(tenantId)
                .onSuccess { booths ->
                    _uiState.value = _uiState.value.copy(booths = booths, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }
}
