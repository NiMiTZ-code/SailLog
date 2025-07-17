package com.niemi.saillog.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.niemi.saillog.data.Maintenance
import com.niemi.saillog.repository.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceListUiState(
    val maintenanceList: List<Maintenance> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MaintenanceListViewModel(application: Application) : AndroidViewModel(application) {

    private val maintenanceRepository = MaintenanceRepository()

    private val _uiState = MutableStateFlow(MaintenanceListUiState())
    val uiState: StateFlow<MaintenanceListUiState> = _uiState.asStateFlow()

    fun loadMaintenance(sailboatId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                maintenanceRepository.getMaintenanceForSailboat(sailboatId)
                    .collect { maintenanceList ->
                        _uiState.update {
                            it.copy(
                                maintenanceList = maintenanceList,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
}