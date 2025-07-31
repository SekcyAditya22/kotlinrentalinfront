package com.example.rentalinn.repository

import android.content.Context
import com.example.rentalinn.model.AuthResponse
import com.example.rentalinn.model.LoginRequest
import com.example.rentalinn.model.RegisterRequest
import com.example.rentalinn.model.UsersResponse
import com.example.rentalinn.model.UsersListResponse
import com.example.rentalinn.model.ChangePasswordRequest
import com.example.rentalinn.network.ApiService
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.DataStoreManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepository(
    private val apiService: ApiService,
    context: Context
) {
    private val dataStoreManager = DataStoreManager.getInstance(context)
    private val gson = Gson()

    suspend fun login(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(email, password))
            response.token?.let { dataStoreManager.saveToken(it) }
            response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            try {
                gson.fromJson(errorBody, AuthResponse::class.java) ?: createErrorResponse("Login gagal")
            } catch (e: JsonSyntaxException) {
                createErrorResponse("Login gagal: Format response tidak valid")
            }
        } catch (e: IOException) {
            createErrorResponse("Tidak dapat terhubung ke server")
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Terjadi kesalahan")
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String = ""
    ): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    phone_number = phoneNumber.ifEmpty { "-" },
                    role = "user"
                )
            )
            response.token?.let { dataStoreManager.saveToken(it) }
            response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            try {
                gson.fromJson(errorBody, AuthResponse::class.java) ?: createErrorResponse("Email sudah terdaftar")
            } catch (e: JsonSyntaxException) {
                createErrorResponse("Registrasi gagal: Format response tidak valid")
            }
        } catch (e: IOException) {
            createErrorResponse("Tidak dapat terhubung ke server")
        } catch (e: Exception) {
            createErrorResponse(e.message ?: "Terjadi kesalahan")
        }
    }

    private fun createErrorResponse(message: String): AuthResponse {
        return AuthResponse(
            success = false,
            message = message,
            token = null,
            user = null
        )
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            dataStoreManager.clearData()
        }
    }

    suspend fun getUsers(): Result<UsersListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsers()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changePassword(
                ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            )
            response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            try {
                gson.fromJson(errorBody, AuthResponse::class.java) ?: createErrorResponse("Failed to change password")
            } catch (e: JsonSyntaxException) {
                createErrorResponse("Change password failed: Invalid response format")
            }
        } catch (e: IOException) {
            createErrorResponse("Network error: Please check your connection")
        } catch (e: Exception) {
            createErrorResponse("Change password failed: ${e.message}")
        }
    }

    suspend fun deleteCurrentUser(): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteCurrentUser()
            // Clear local data after successful deletion
            dataStoreManager.clearData()
            response
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            try {
                gson.fromJson(errorBody, AuthResponse::class.java) ?: createErrorResponse("Failed to delete account")
            } catch (e: JsonSyntaxException) {
                createErrorResponse("Delete account failed: Invalid response format")
            }
        } catch (e: IOException) {
            createErrorResponse("Network error: Please check your connection")
        } catch (e: Exception) {
            createErrorResponse("Delete account failed: ${e.message}")
        }
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val role: String
) 