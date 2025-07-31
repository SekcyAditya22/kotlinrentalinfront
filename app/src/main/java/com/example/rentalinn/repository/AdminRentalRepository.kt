package com.example.rentalinn.repository

import android.content.Context
import com.example.rentalinn.model.Rental
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.TokenManager

class AdminRentalRepository(private val context: Context) {
    private val apiService = RetrofitClient.getInstance(context).apiService
    private val tokenManager = TokenManager(context)

    suspend fun getPendingRentals(): List<Rental> {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getPendingRentals()

            if (response.isSuccessful) {
                return response.body()?.data ?: emptyList()
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    else -> "Failed to fetch pending rentals: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getAllRentals(): List<Rental> {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getAllRentals()

            if (response.isSuccessful) {
                return response.body()?.data ?: emptyList()
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    else -> "Failed to fetch rentals: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun approveRental(rentalId: Int) {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.approveRental(rentalId)

            if (!response.isSuccessful) {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Rental not found"
                    else -> "Failed to approve rental: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun rejectRental(rentalId: Int, reason: String) {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.rejectRental(
                rentalId = rentalId,
                request = RejectRentalRequest(reason)
            )

            if (!response.isSuccessful) {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Rental not found"
                    else -> "Failed to reject rental: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun completeRental(rentalId: Int) {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.completeRental(rentalId)

            if (!response.isSuccessful) {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    403 -> "Access denied"
                    404 -> "Rental not found"
                    else -> "Failed to complete rental: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }
}

data class RejectRentalRequest(
    val reason: String
)
