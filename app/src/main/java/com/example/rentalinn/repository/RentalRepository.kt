package com.example.rentalinn.repository

import android.content.Context
import com.example.rentalinn.model.*
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RentalRepository(private val context: Context) {
    private val apiService = RetrofitClient.getInstance(context).rentalApiService
    private val paymentApiService = RetrofitClient.getInstance(context).apiService
    private val tokenManager = TokenManager(context)

    suspend fun createRental(request: CreateRentalRequest): Flow<CreateRentalResponse> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.createRental(request)
            if (response.isSuccessful) {
                response.body()?.let { emit(it) }
                    ?: throw Exception("Empty response body")
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid rental request"
                    401 -> "Authentication failed"
                    404 -> "Vehicle not found"
                    409 -> "Vehicle not available for selected dates"
                    else -> "Failed to create rental: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getUserRentals(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null
    ): Flow<List<Rental>> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getUserRentals(page, limit, status)
            if (response.isSuccessful) {
                response.body()?.data?.rentals?.let { emit(it) }
                    ?: emit(emptyList())
            } else {
                val errorMessage = when (response.code()) {
                    401 -> {
                        // Clear token on authentication failure
                        tokenManager.clearToken()
                        "Session expired. Please login again."
                    }
                    400 -> "Bad request - please check your connection"
                    else -> "Failed to fetch rentals: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getRentalById(id: Int): Flow<Rental> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getRentalById(id)
            if (response.isSuccessful) {
                response.body()?.data?.let { emit(it) }
                    ?: throw Exception("Rental not found")
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    404 -> "Rental not found"
                    else -> "Failed to fetch rental: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }



    suspend fun getPaymentStatus(orderId: String): Flow<Payment> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getPaymentStatus(orderId)
            if (response.isSuccessful) {
                response.body()?.data?.payment?.let { emit(it) }
                    ?: throw Exception("Payment not found")
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    404 -> "Payment not found"
                    else -> "Failed to fetch payment status: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun retryPayment(rentalId: Int): Flow<PaymentData> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.retryPayment(rentalId)
            if (response.isSuccessful) {
                response.body()?.data?.let { emit(it) }
                    ?: throw Exception("Failed to retry payment")
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Payment cannot be retried"
                    401 -> "Authentication failed"
                    404 -> "Rental not found"
                    else -> "Failed to retry payment: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getUserPayments(
        page: Int = 1,
        limit: Int = 10,
        status: String? = null
    ): Flow<List<Payment>> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getUserPayments(page, limit, status)
            if (response.isSuccessful) {
                response.body()?.data?.payments?.let { emit(it) }
                    ?: emit(emptyList())
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    else -> "Failed to fetch payments: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun autoUpdatePaymentStatus(rentalId: Int) {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            // Use rental ID directly (more reliable)
            val response = paymentApiService.autoUpdatePaymentByRental(rentalId)

            if (!response.isSuccessful) {
                throw Exception("Failed to auto-update payment: ${response.message()}")
            }
        } catch (e: Exception) {
            throw Exception("Auto-update error: ${e.message}")
        }
    }

    suspend fun cancelRental(rentalId: Int) {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.cancelRental(rentalId)

            if (!response.isSuccessful) {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    404 -> "Rental not found"
                    400 -> "Cannot cancel this rental"
                    else -> "Failed to cancel rental: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Cancel rental error: ${e.message}")
        }
    }

    suspend fun getUserStats(): Flow<UserStatsData> = flow {
        try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                throw Exception("Authentication token not found")
            }

            val response = apiService.getUserStats()
            if (response.isSuccessful) {
                response.body()?.data?.let { emit(it) }
                    ?: throw Exception("Empty response body")
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Authentication failed"
                    else -> "Failed to fetch user statistics: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            throw Exception("Network error: ${e.message}")
        }
    }
}
