package com.niemi.saillog.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.niemi.saillog.data.Sailboat
import com.niemi.saillog.repository.SailboatRepository
import com.niemi.saillog.services.ImageUploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddSailboatViewModel(application: Application) : AndroidViewModel(application) {

    private val imageUploadService = ImageUploadService(application)
    private val repository = SailboatRepository(imageUploadService)

    private val _uiState = MutableStateFlow(AddSailboatUiState())
    val uiState: StateFlow<AddSailboatUiState> = _uiState.asStateFlow()

    fun saveSailboat(
        boatName: String,
        modelName: String,
        manufacturer: String?,
        year: Int?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                var imageStoragePath: String? = null

                // Upload image if provided
                if (imageUri != null) {
                    imageStoragePath = imageUploadService.uploadSailboatImage(imageUri)
                    if (imageStoragePath == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to upload image"
                        )
                        return@launch
                    }
                }

                // Create sailboat object
                val sailboat = Sailboat(
                    boatName = boatName,
                    modelName = modelName,
                    manufacturer = manufacturer,
                    year = year,
                    imageStoragePath = imageStoragePath
                )

                // Save to Firestore
                val success = repository.saveSailboat(sailboat)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = success,
                    error = if (!success) "Failed to save sailboat" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}

data class AddSailboatUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)