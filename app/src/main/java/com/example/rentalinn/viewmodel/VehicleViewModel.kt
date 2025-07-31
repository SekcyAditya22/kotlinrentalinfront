
package com.example.rentalinn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.repository.VehicleRepository
import com.example.rentalinn.ui.components.VehicleFilter
import com.example.rentalinn.ui.components.SortOption
import com.example.rentalinn.ui.components.ErrorType
import com.example.rentalinn.ui.components.getErrorType
import com.example.rentalinn.utils.NetworkStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import com.example.rentalinn.screens.admin.vehicle.VehicleUnitInput
import com.example.rentalinn.model.VehicleUnit

class VehicleViewModel(
    private val repository: VehicleRepository,
    private val networkStateManager: NetworkStateManager
) : ViewModel() {

    private val _allVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val _vehicle = MutableStateFlow<Vehicle?>(null)
    val vehicle: StateFlow<Vehicle?> = _vehicle

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _errorType = MutableStateFlow<ErrorType?>(null)
    val errorType: StateFlow<ErrorType?> = _errorType

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable: StateFlow<Boolean> = _isNetworkAvailable

    private val _retryCount = MutableStateFlow(0)
    private val maxRetries = 3

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentFilter = MutableStateFlow(VehicleFilter())
    val currentFilter: StateFlow<VehicleFilter> = _currentFilter

    private val _currentSort = MutableStateFlow(SortOption.NEWEST_FIRST)
    val currentSort: StateFlow<SortOption> = _currentSort

    // Filtered and sorted vehicles
    val filteredVehicles = combine(
        _allVehicles,
        _searchQuery,
        _currentFilter,
        _currentSort
    ) { vehicles, query, filter, sort ->
        vehicles
            .filter { vehicle -> matchesSearch(vehicle, query) }
            .filter { vehicle -> matchesFilter(vehicle, filter) }
            .let { sortVehicles(it, sort) }
    }

    init {
        loadVehicles()

        // Monitor network state (simplified)
        viewModelScope.launch {
            networkStateManager.isNetworkAvailable.collect { isAvailable ->
                _isNetworkAvailable.value = isAvailable
            }
        }

        // Update filtered vehicles when data changes
        viewModelScope.launch {
            filteredVehicles.collect { filtered ->
                println("DEBUG VIEWMODEL: Filtered vehicles count: ${filtered.size}")
                _vehicles.value = filtered
            }
        }
    }

    fun loadVehicles(forceRefresh: Boolean = false) {
        if (_allVehicles.value.isEmpty() || forceRefresh) {
            _isLoading.value = true
            _error.value = null
            _errorType.value = null

            viewModelScope.launch {
                try {
                    repository.getVehiclesWithAvailability()
                        .catch { e ->
                            println("DEBUG VIEWMODEL: Error in loadVehicles: ${e.message}")
                            _error.value = e.message ?: "Failed to load vehicles"
                            _errorType.value = getErrorType(e)
                            _isLoading.value = false
                        }
                        .collect { vehicleList ->
                            println("DEBUG VIEWMODEL: Successfully loaded ${vehicleList.size} vehicles with availability")
                            _allVehicles.value = vehicleList
                            println("DEBUG VIEWMODEL: _allVehicles now has ${_allVehicles.value.size} vehicles")
                            updateCategories(vehicleList)
                            _isLoading.value = false
                            _retryCount.value = 0 // Reset retry count on success
                        }
                } catch (e: Exception) {
                    println("DEBUG VIEWMODEL: Exception in loadVehicles: ${e.message}")
                    _error.value = e.message ?: "Failed to load vehicles"
                    _errorType.value = getErrorType(e)
                    _isLoading.value = false
                }
            }
        }
    }

    // Removed handleError function to prevent auto-retry conflicts

    fun refreshVehicles() {
        _isRefreshing.value = true
        _error.value = null
        _errorType.value = null
        _retryCount.value = 0 // Reset retry count for manual refresh

        viewModelScope.launch {
            try {
                repository.getVehiclesWithAvailability()
                    .catch { e ->
                        println("DEBUG VIEWMODEL: Error in refreshVehicles: ${e.message}")
                        _error.value = e.message ?: "Failed to refresh vehicles"
                        _errorType.value = getErrorType(e)
                        _isRefreshing.value = false
                    }
                    .collect { vehicleList ->
                        println("DEBUG VIEWMODEL: Successfully refreshed ${vehicleList.size} vehicles with availability")
                        _allVehicles.value = vehicleList
                        updateCategories(vehicleList)
                        _isRefreshing.value = false
                    }
            } catch (e: Exception) {
                println("DEBUG VIEWMODEL: Exception in refreshVehicles: ${e.message}")
                _error.value = e.message ?: "Failed to refresh vehicles"
                _errorType.value = getErrorType(e)
                _isRefreshing.value = false
            }
        }
    }

    fun getVehicleById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getVehicleById(id)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { vehicle ->
                    _vehicle.value = vehicle
                    _isLoading.value = false
                }
        }
    }

    fun getAvailableVehicles() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getAvailableVehicles()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { vehicleList ->
                    _allVehicles.value = vehicleList
                    _isLoading.value = false
                }
        }
    }

    // Search and filter methods
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: VehicleFilter) {
        _currentFilter.value = filter
    }

    fun updateSort(sort: SortOption) {
        _currentSort.value = sort
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun resetFilter() {
        _currentFilter.value = VehicleFilter()
    }

    fun getVehiclesByCategory(category: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getVehiclesByCategory(category)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { vehicleList ->
                    _allVehicles.value = vehicleList
                    _isLoading.value = false
                }
        }
    }

    fun createVehicle(
        title: String,
        brand: String,
        model: String,
        vehicleCategory: String,
        year: Int,
        licensePlate: String,
        pricePerDay: Double,
        unit: Int,
        description: String?,
        transmission: String,
        fuelType: String,
        passengerCapacity: Int,
        features: List<String>?,
        photos: List<File>?,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.createVehicle(
                title = title,
                brand = brand,
                model = model,
                vehicleCategory = vehicleCategory,
                year = year,
                licensePlate = licensePlate,
                pricePerDay = pricePerDay,
                unit = unit,
                description = description,
                transmission = transmission,
                fuelType = fuelType,
                passengerCapacity = passengerCapacity,
                features = features,
                photos = photos
            )
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { _ ->
                    _successMessage.value = "Vehicle created successfully"
                    loadVehicles(true)
                    _isLoading.value = false
                    onSuccess()
                }
        }
    }

    fun createVehicleWithUnits(
        title: String,
        brand: String,
        model: String,
        vehicleCategory: String,
        year: Int,
        pricePerDay: Double,
        description: String?,
        transmission: String,
        fuelType: String,
        passengerCapacity: Int,
        features: List<String>?,
        photos: List<File>?,
        units: List<VehicleUnitInput>,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.createVehicleWithUnits(
                title = title,
                brand = brand,
                model = model,
                vehicleCategory = vehicleCategory,
                year = year,
                pricePerDay = pricePerDay,
                description = description,
                transmission = transmission,
                fuelType = fuelType,
                passengerCapacity = passengerCapacity,
                features = features,
                photos = photos,
                units = units
            )
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { _ ->
                    _isLoading.value = false
                    _successMessage.value = "Vehicle created successfully"
                    loadVehicles(true)
                    onSuccess()
                }
        }
    }

    fun updateVehicle(
        id: Int,
        title: String? = null,
        brand: String? = null,
        model: String? = null,
        vehicleCategory: String? = null,
        year: Int? = null,
        licensePlate: String? = null,
        pricePerDay: Double? = null,
        unit: Int? = null,
        description: String? = null,
        status: String? = null,
        transmission: String? = null,
        fuelType: String? = null,
        passengerCapacity: Int? = null,
        features: List<String>? = null,
        photos: List<File>? = null,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        println("DEBUG VIEWMODEL: Starting vehicle update for ID: $id")
        println("DEBUG VIEWMODEL: Title: $title")
        println("DEBUG VIEWMODEL: Brand: $brand")
        println("DEBUG VIEWMODEL: Model: $model")
        println("DEBUG VIEWMODEL: Category: $vehicleCategory")
        println("DEBUG VIEWMODEL: Year: $year")
        println("DEBUG VIEWMODEL: License: $licensePlate")
        println("DEBUG VIEWMODEL: Price: $pricePerDay")
        println("DEBUG VIEWMODEL: Features: $features")
        println("DEBUG VIEWMODEL: Photos: ${photos?.size ?: 0} new photos to upload")
        
        viewModelScope.launch {
            repository.updateVehicle(
                id = id,
                title = title,
                brand = brand,
                model = model,
                vehicleCategory = vehicleCategory,
                year = year,
                licensePlate = licensePlate,
                pricePerDay = pricePerDay,
                unit = unit,
                description = description,
                status = status,
                transmission = transmission,
                fuelType = fuelType,
                passengerCapacity = passengerCapacity,
                features = features,
                photos = photos
            )
                .catch { e ->
                    println("DEBUG VIEWMODEL: Update error: ${e.message}")
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { vehicle ->
                    println("DEBUG VIEWMODEL: Update successful for vehicle: ${vehicle.id}")
                    _successMessage.value = "Vehicle updated successfully"
                    loadVehicles(true)
                    _isLoading.value = false
                    onSuccess()
                }
        }
    }

    fun deleteVehicle(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.deleteVehicle(id)
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { success ->
                    if (success) {
                        _successMessage.value = "Vehicle deleted successfully"
                        loadVehicles(true)
                    }
                    _isLoading.value = false
                }
        }
    }

    private fun updateCategories(vehicles: List<Vehicle>) {
        val categorySet = vehicles.map { it.vehicleCategory }.toSet()
        println("DEBUG VIEWMODEL: Categories found: $categorySet")
        _categories.value = categorySet.toList()
    }

    fun clearMessages() {
        _error.value = null
        _errorType.value = null
        _successMessage.value = null
    }

    // Vehicle Units Management
    fun createVehicleUnit(
        vehicleId: Int,
        plateNumber: String,
        location: String?,
        notes: String?,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // TODO: Implement repository method
                _isLoading.value = false
                _successMessage.value = "Vehicle unit added successfully"
                getVehicleById(vehicleId) // Refresh vehicle data
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateVehicleUnit(
        unit: VehicleUnit,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // TODO: Implement repository method
                _isLoading.value = false
                _successMessage.value = "Vehicle unit updated successfully"
                unit.vehicle?.let { getVehicleById(it.id) } // Refresh vehicle data
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun deleteVehicleUnit(
        unitId: Int,
        vehicleId: Int,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // TODO: Implement repository method
                _isLoading.value = false
                _successMessage.value = "Vehicle unit deleted successfully"
                getVehicleById(vehicleId) // Refresh vehicle data
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun retryLastOperation() {
        when {
            _isLoading.value || _isRefreshing.value -> return // Already loading
            _allVehicles.value.isEmpty() -> loadVehicles(true)
            else -> refreshVehicles()
        }
    }

    // Helper methods for filtering and sorting
    private fun matchesSearch(vehicle: Vehicle, query: String): Boolean {
        if (query.isBlank()) return true
        val searchQuery = query.lowercase()
        return vehicle.title.lowercase().contains(searchQuery) ||
                vehicle.brand.lowercase().contains(searchQuery) ||
                vehicle.model.lowercase().contains(searchQuery) ||
                vehicle.vehicleCategory.lowercase().contains(searchQuery) ||
                vehicle.description?.lowercase()?.contains(searchQuery) == true
    }

    private fun matchesFilter(vehicle: Vehicle, filter: VehicleFilter): Boolean {
        // Category filter
        if (filter.category != null && vehicle.vehicleCategory != filter.category) {
            return false
        }

        // Price filter
        if (filter.minPrice != null && vehicle.pricePerDay < filter.minPrice) {
            return false
        }
        if (filter.maxPrice != null && vehicle.pricePerDay > filter.maxPrice) {
            return false
        }

        // Transmission filter
        if (filter.transmission != null && vehicle.transmission != filter.transmission) {
            return false
        }

        // Fuel type filter
        if (filter.fuelType != null && vehicle.fuelType != filter.fuelType) {
            return false
        }

        // Passenger capacity filter
        if (filter.minPassengers != null && vehicle.passengerCapacity < filter.minPassengers) {
            return false
        }

        // Available only filter
        if (filter.availableOnly && !vehicle.isAvailable()) {
            return false
        }

        return true
    }

    private fun sortVehicles(vehicles: List<Vehicle>, sort: SortOption): List<Vehicle> {
        return when (sort) {
            SortOption.PRICE_LOW_TO_HIGH -> vehicles.sortedBy { it.pricePerDay }
            SortOption.PRICE_HIGH_TO_LOW -> vehicles.sortedByDescending { it.pricePerDay }
            SortOption.NEWEST_FIRST -> vehicles.sortedByDescending { it.year }
            SortOption.OLDEST_FIRST -> vehicles.sortedBy { it.year }
            SortOption.ALPHABETICAL -> vehicles.sortedBy { it.title }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
                return try {
                    val apiService = RetrofitClient.getInstance(context).apiService
                    val repository = VehicleRepository(apiService)
                    val networkStateManager = NetworkStateManager.getInstance(context)
                    VehicleViewModel(repository, networkStateManager) as T
                } catch (e: Exception) {
                    // Log the error and rethrow to be handled by the UI
                    android.util.Log.e("VehicleViewModel", "Failed to create ViewModel", e)
                    throw e
                }
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 