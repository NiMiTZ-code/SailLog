package com.niemi.saillog.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.niemi.saillog.data.Maintenance
import com.niemi.saillog.data.MaintenanceCategory
import com.niemi.saillog.repository.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*

data class AddMaintenanceUiState(
    val selectedDate: Date = Date(),
    val category: MaintenanceCategory = MaintenanceCategory.OTHER,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddMaintenanceViewModel(application: Application) : AndroidViewModel(application) {

    private val maintenanceRepository = MaintenanceRepository()

    private val _uiState = MutableStateFlow(AddMaintenanceUiState())
    val uiState: StateFlow<AddMaintenanceUiState> = _uiState.asStateFlow()

    private var sailboatId: String? = null

    fun setSailboatId(id: String) {
        sailboatId = id
    }

    fun updateDate(date: Date) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun updateCategory(category: MaintenanceCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveMaintenance() {
        val currentSailboatId = sailboatId ?: run {
            _uiState.update { it.copy(errorMessage = "No sailboat selected") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val maintenance = Maintenance(
                sailboatId = currentSailboatId,
                category = _uiState.value.category,
                notes = _uiState.value.notes,
                timestamp = Timestamp(_uiState.value.selectedDate)
            )

            maintenanceRepository.addMaintenance(maintenance)
                .fold(
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Failed to save maintenance"
                            )
                        }
                    }
                )
        }
    }
}