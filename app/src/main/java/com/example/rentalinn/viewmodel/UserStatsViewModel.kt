package com.example.rentalinn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.Rental
import com.example.rentalinn.model.UserStatsData
import com.example.rentalinn.repository.RentalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class UserStatsViewModel(private val repository: RentalRepository) : ViewModel() {
    
    private val _userStats = MutableStateFlow<UserStatsData?>(null)
    val userStats: StateFlow<UserStatsData?> = _userStats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadUserStats()
    }

    fun loadUserStats() {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getUserStats()
                .catch { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
                .collect { stats ->
                    _userStats.value = stats
                    _isLoading.value = false
                }
        }
    }

    fun refreshUserStats() {
        _isRefreshing.value = true
        _error.value = null
        
        viewModelScope.launch {
            repository.getUserStats()
                .catch { e ->
                    _error.value = e.message
                    _isRefreshing.value = false
                }
                .collect { stats ->
                    _userStats.value = stats
                    _isRefreshing.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserStatsViewModel::class.java)) {
                return UserStatsViewModel(RentalRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
