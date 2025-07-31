package com.example.rentalinn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import com.example.rentalinn.model.Rental
import com.example.rentalinn.repository.AdminRentalRepository

class AdminRentalViewModel(
    private val repository: AdminRentalRepository
) : ViewModel() {

    private val _pendingRentals = MutableStateFlow<List<Rental>>(emptyList())
    val pendingRentals: StateFlow<List<Rental>> = _pendingRentals.asStateFlow()

    private val _allRentals = MutableStateFlow<List<Rental>>(emptyList())
    val allRentals: StateFlow<List<Rental>> = _allRentals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun getPendingRentals() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val rentals = repository.getPendingRentals()
                _pendingRentals.value = rentals

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load pending rentals"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllRentals() {
        viewModelScope.launch {
            try {
                if (!_isLoading.value) {
                    _isLoading.value = true
                }
                _error.value = null

                val rentals = repository.getAllRentals()
                _allRentals.value = rentals

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load rentals"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Load both pending and all rentals concurrently
                val pendingDeferred = async { repository.getPendingRentals() }
                val allDeferred = async { repository.getAllRentals() }

                val pendingRentals = pendingDeferred.await()
                val allRentals = allDeferred.await()

                _pendingRentals.value = pendingRentals
                _allRentals.value = allRentals

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load rental data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveRental(rentalId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                repository.approveRental(rentalId)
                _successMessage.value = "Rental approved successfully"
                
                // Refresh data
                getPendingRentals()
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to approve rental"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectRental(rentalId: Int, reason: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.rejectRental(rentalId, reason)
                _successMessage.value = "Rental rejected successfully"

                // Refresh data
                getPendingRentals()

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reject rental"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun completeRental(rentalId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.completeRental(rentalId)
                _successMessage.value = "Rental completed successfully"

                // Refresh data
                loadAllData()

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to complete rental"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdminRentalViewModel::class.java)) {
                return AdminRentalViewModel(AdminRentalRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
