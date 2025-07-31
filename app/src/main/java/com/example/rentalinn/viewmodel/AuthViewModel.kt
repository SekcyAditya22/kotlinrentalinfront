package com.example.rentalinn.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentalinn.model.AuthResponse
import com.example.rentalinn.model.User
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.repository.UserRepository
import com.example.rentalinn.utils.DataStoreManager
import com.example.rentalinn.utils.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(
        val token: String?,
        val user: User?,
        val message: String?
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    private val dataStore: DataStoreManager
    private val userSessionManager: UserSessionManager

    init {
        val context: Context = application.applicationContext
        val apiService = RetrofitClient.getInstance(context).apiService
        repository = UserRepository(apiService, context)
        dataStore = DataStoreManager.getInstance(context)
        userSessionManager = UserSessionManager.getInstance(context)
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                val response = repository.login(email, password)
                if (response.success) {
                    // Store token and user data using UserSessionManager
                    if (response.token != null && response.user != null) {
                        userSessionManager.saveUserSession(response.token, response.user)
                    }
                    _uiState.value = AuthUiState.Success(
                        token = response.token,
                        user = response.user,
                        message = response.message
                    )
                } else {
                    _uiState.value = AuthUiState.Error(response.message ?: "Invalid credentials")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun register(name: String, email: String, password: String, phoneNumber: String = "") {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                val response = repository.register(name, email, password, phoneNumber)
                if (response.success) {
                    // Store token and user data using UserSessionManager
                    if (response.token != null && response.user != null) {
                        userSessionManager.saveUserSession(response.token, response.user)
                    }
                    _uiState.value = AuthUiState.Success(
                        token = response.token,
                        user = response.user,
                        message = response.message
                    )
                } else {
                    _uiState.value = AuthUiState.Error(response.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                userSessionManager.clearUserSession()
                _uiState.value = AuthUiState.Success(
                    token = null,
                    user = null,
                    message = "Logged out successfully"
                )
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to logout")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                val response = repository.changePassword(currentPassword, newPassword)
                if (response.success) {
                    _uiState.value = AuthUiState.Success(
                        token = null,
                        user = null,
                        message = response.message ?: "Password changed successfully"
                    )
                } else {
                    _uiState.value = AuthUiState.Error(response.message ?: "Failed to change password")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                val response = repository.deleteCurrentUser()
                if (response.success) {
                    _uiState.value = AuthUiState.Success(
                        token = null,
                        user = null,
                        message = response.message ?: "Account deleted successfully"
                    )
                } else {
                    _uiState.value = AuthUiState.Error(response.message ?: "Failed to delete account")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
} 