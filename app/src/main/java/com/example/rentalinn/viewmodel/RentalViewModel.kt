package com.example.rentalinn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.*
import com.example.rentalinn.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class RentalViewModel(private val repository: RentalRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _createdRental = MutableStateFlow<CreateRentalData?>(null)
    val createdRental: StateFlow<CreateRentalData?> = _createdRental

    private val _rentals = MutableStateFlow<List<Rental>>(emptyList())
    val rentals: StateFlow<List<Rental>> = _rentals

    private val _rental = MutableStateFlow<Rental?>(null)
    val rental: StateFlow<Rental?> = _rental

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _payment = MutableStateFlow<Payment?>(null)
    val payment: StateFlow<Payment?> = _payment

    fun createRental(
        vehicleId: Int? = null,
        unitId: Int? = null,
        startDate: String,
        endDate: String,
        pickupLocation: String?,
        pickupLatitude: Double?,
        pickupLongitude: Double?,
        returnLocation: String?,
        returnLatitude: Double?,
        returnLongitude: Double?,
        notes: String?
    ) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.createRental(
                CreateRentalRequest(
                    vehicleId = vehicleId,
                    unitId = unitId,
                    startDate = startDate,
                    endDate = endDate,
                    pickupLocation = pickupLocation,
                    pickupLatitude = pickupLatitude,
                    pickupLongitude = pickupLongitude,
                    returnLocation = returnLocation,
                    returnLatitude = returnLatitude,
                    returnLongitude = returnLongitude,
                    notes = notes
                )
            )
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { response ->
                    if (response.success) {
                        _createdRental.value = response.data
                        _successMessage.value = response.message
                    } else {
                        _error.value = response.message
                    }
                    _isLoading.value = false
                }
        }
    }

    fun getUserRentals(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null
    ) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getUserRentals(page, limit, status)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { rentals ->
                    _rentals.value = rentals
                    _isLoading.value = false
                }
        }
    }

    fun getRentalById(id: Int) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getRentalById(id)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { rental ->
                    _rental.value = rental
                    _isLoading.value = false
                }
        }
    }



    fun getPaymentStatus(orderId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getPaymentStatus(orderId)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { payment ->
                    _payment.value = payment
                    _isLoading.value = false
                }
        }
    }

    fun retryPayment(rentalId: Int) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.retryPayment(rentalId)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { paymentData ->
                    // Handle payment retry success
                    _successMessage.value = "Payment retry created successfully"
                    _isLoading.value = false
                }
        }
    }

    fun getUserPayments(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null
    ) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getUserPayments(page, limit, status)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { payments ->
                    _payments.value = payments
                    _isLoading.value = false
                }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

    fun clearCreatedRental() {
        _createdRental.value = null
    }

    fun cancelRental(rentalId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                repository.cancelRental(rentalId)
                _successMessage.value = "Rental cancelled successfully"

                // Refresh user rentals
                getUserRentals()

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to cancel rental"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun autoUpdatePaymentStatus(rentalId: Int) {
        viewModelScope.launch {
            try {
                // Call auto-update endpoint
                repository.autoUpdatePaymentStatus(rentalId)

                // Refresh rental data after update
                kotlinx.coroutines.delay(2000) // Wait 2 seconds for backend processing
                getRentalById(rentalId)

            } catch (e: Exception) {
                // Silent fail - webhook might work, or manual update can be done
                android.util.Log.w("RentalViewModel", "Auto-update failed: ${e.message}")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RentalViewModel::class.java)) {
                return RentalViewModel(RentalRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
