package com.askara.photobooth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.askara.photobooth.data.model.Booth
import com.askara.photobooth.data.model.Template
import com.askara.photobooth.data.repository.BoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TemplateSelectUiState(
    val isLoading: Boolean = false,
    val templates: List<Template> = emptyList(),
    val booth: Booth? = null,
    val error: String? = null
)

class TemplateSelectViewModel : ViewModel() {
    private val repo = BoothRepository()

    private val _uiState = MutableStateFlow(TemplateSelectUiState())
    val uiState: StateFlow<TemplateSelectUiState> = _uiState

    fun loadData(tenantId: String, boothId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load booth info
            repo.getBooth(boothId).onSuccess { booth ->
                _uiState.value = _uiState.value.copy(booth = booth)
            }

            // Load templates
            repo.getTemplates(tenantId)
                .onSuccess { templates ->
                    _uiState.value = _uiState.value.copy(templates = templates, isLoading = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }
}
