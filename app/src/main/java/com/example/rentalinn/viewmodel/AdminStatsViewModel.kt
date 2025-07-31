package com.example.rentalinn.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.AdminStats
import com.example.rentalinn.model.Rental
import com.example.rentalinn.repository.AdminStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminStatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminStatsRepository(application.applicationContext)

    private val _adminStats = MutableStateFlow<AdminStats?>(null)
    val adminStats: StateFlow<AdminStats?> = _adminStats.asStateFlow()

    private val _recentRentals = MutableStateFlow<List<Rental>>(emptyList())
    val recentRentals: StateFlow<List<Rental>> = _recentRentals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load admin stats and recent rentals concurrently
                val stats = repository.getAdminStats()
                val rentals = repository.getRecentRentals(5)

                _adminStats.value = stats
                _recentRentals.value = rentals

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load admin data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadAdminData()
    }

    fun clearError() {
        _error.value = null
    }
}

class AdminStatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminStatsViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
