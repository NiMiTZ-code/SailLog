package com.niemi.saillog.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niemi.saillog.data.Sailboat
import com.niemi.saillog.repository.SailboatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = SailboatRepository()

    private val _sailboats = MutableStateFlow<List<Sailboat>>(emptyList())
    val sailboats: StateFlow<List<Sailboat>> = _sailboats.asStateFlow()

    private val _selectedSailboat = MutableStateFlow<Sailboat?>(null)
    val selectedSailboat: StateFlow<Sailboat?> = _selectedSailboat.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserSailboats()
    }

    private fun loadUserSailboats() {
        viewModelScope.launch {
            repository.getUserSailboats().collect { sailboatList ->
                _sailboats.value = sailboatList

                // Auto-select first sailboat if none selected
                if (_selectedSailboat.value == null && sailboatList.isNotEmpty()) {
                    _selectedSailboat.value = sailboatList.first()
                }

                _isLoading.value = false
            }
        }
    }

    fun selectSailboat(sailboat: Sailboat) {
        _selectedSailboat.value = sailboat
    }
}