package com.niemi.saillog.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.niemi.saillog.data.Maintenance
import com.niemi.saillog.data.Sailboat
import com.niemi.saillog.repository.MaintenanceRepository
import com.niemi.saillog.repository.SailboatRepository
import com.niemi.saillog.services.ImageUploadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val imageUploadService = ImageUploadService(application)
    private val repository = SailboatRepository(imageUploadService)
    private val maintenanceRepository = MaintenanceRepository()

    private val _maintenanceList = MutableStateFlow<List<Maintenance>>(emptyList())
    val maintenanceList: StateFlow<List<Maintenance>> = _maintenanceList.asStateFlow()

    private val _sailboats = MutableStateFlow<List<Sailboat>>(emptyList())
    val sailboats: StateFlow<List<Sailboat>> = _sailboats.asStateFlow()

    private val _selectedSailboat = MutableStateFlow<Sailboat?>(null)
    val selectedSailboat: StateFlow<Sailboat?> = _selectedSailboat.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadSailboats()
        observeSelectedSailboat()
    }

    private fun observeSelectedSailboat() {
        viewModelScope.launch {
            selectedSailboat.collect { sailboat ->
                sailboat?.let {
                    loadMaintenanceForSailboat(it.id)
                } ?: run {
                    _maintenanceList.value = emptyList()
                }
            }
        }
    }

    private fun loadMaintenanceForSailboat(sailboatId: String) {
        viewModelScope.launch {
            try {
                maintenanceRepository.getMaintenanceForSailboat(sailboatId, limit = 3)
                    .collect { maintenanceList ->
                        _maintenanceList.value = maintenanceList
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                _maintenanceList.value = emptyList()
            }
        }
    }

    private fun loadSailboats() {
        viewModelScope.launch {
            try {
                repository.getUserSailboats().collect { boats ->
                    _sailboats.value = boats

                    // Load signed URLs for each boat
                    boats.forEach { boat ->
                        if (!boat.imageStoragePath.isNullOrEmpty()) {
                            viewModelScope.launch {
                                val updatedBoat = repository.getSailboatWithSignedUrl(boat)
                                _sailboats.value = _sailboats.value.map {
                                    if (it.id == boat.id) updatedBoat else it
                                }

                                // Update selected boat if it's the same
                                if (_selectedSailboat.value?.id == boat.id) {
                                    _selectedSailboat.value = updatedBoat
                                }
                            }
                        }
                    }

                    // Select first boat by default
                    if (_selectedSailboat.value == null && boats.isNotEmpty()) {
                        selectSailboat(boats.first())
                    }

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun selectSailboat(sailboat: Sailboat) {
        viewModelScope.launch {
            _selectedSailboat.value = repository.getSailboatWithSignedUrl(sailboat)
        }
    }

    fun refreshSignedUrl(sailboat: Sailboat) {
        viewModelScope.launch {
            val updatedBoat = repository.getSailboatWithSignedUrl(sailboat)
            if (_selectedSailboat.value?.id == sailboat.id) {
                _selectedSailboat.value = updatedBoat
            }
            _sailboats.value = _sailboats.value.map {
                if (it.id == sailboat.id) updatedBoat else it
            }
        }
    }
}