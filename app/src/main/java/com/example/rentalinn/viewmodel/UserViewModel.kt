package com.example.rentalinn.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.User
import com.example.rentalinn.model.CreateUserRequest
import com.example.rentalinn.model.UsersResponse
import com.example.rentalinn.model.UsersListResponse
import com.example.rentalinn.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.io.IOException

class UserViewModel(context: Context) : ViewModel() {
    private val apiService = RetrofitClient.getInstance(context).apiService
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private var loadJob: Job? = null
    private var lastLoadTime: Long = 0
    private val CACHE_DURATION = 30_000 // 30 seconds cache

    init {
        loadUsers(forceRefresh = true)
    }

    fun loadUsers(forceRefresh: Boolean = false, onLoaded: () -> Unit = {}) {
        // Cancel any ongoing load
        loadJob?.cancel()

        // Check if we should use cached data
        if (!forceRefresh && System.currentTimeMillis() - lastLoadTime < CACHE_DURATION) {
            onLoaded()
            return
        }

        loadJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getUsers()
                if (response.status == "success") {
                    _users.value = response.data.sortedBy { it.name }
                    lastLoadTime = System.currentTimeMillis()
                } else {
                    _error.value = response.message
                }
            } catch (e: IOException) {
                _error.value = "Network error: Please check your connection"
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
                onLoaded()
            }
        }
    }

    fun createUser(request: CreateUserRequest, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.createUser(request)
                if (response.status == "success") {
                    _successMessage.value = "User created successfully"
                    // Refresh the users list
                    loadUsers(forceRefresh = true)
                    onSuccess()
                } else {
                    _error.value = response.message
                }
            } catch (e: IOException) {
                _error.value = "Network error: Please check your connection"
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUser(user: User, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.updateUser(user.id, user)
                if (response.status == "success") {
                    _successMessage.value = "User updated successfully"
                    // Refresh the users list
                    loadUsers(forceRefresh = true)
                    onSuccess()
                } else {
                    _error.value = response.message
                }
            } catch (e: IOException) {
                _error.value = "Network error: Please check your connection"
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = apiService.deleteUser(userId)
                if (response.success) {
                    loadUsers(forceRefresh = true) {
                        _successMessage.value = response.message
                        _isLoading.value = false
                    }
                    return@launch
                } else {
                    _error.value = response.message
                }
            } catch (e: IOException) {
                _error.value = "Network error: Please check your connection"
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                if (_isLoading.value) _isLoading.value = false
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
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                return UserViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 