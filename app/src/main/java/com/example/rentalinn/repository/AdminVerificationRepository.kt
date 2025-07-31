package com.example.rentalinn.repository

import android.content.Context
import android.util.Log
import com.example.rentalinn.api.AdminUserDetailsApiService
import com.example.rentalinn.api.VerificationRequest
import com.example.rentalinn.api.BulkVerificationRequest
import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.model.AdminUserDetailsResponse
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.DataStoreManager
import kotlinx.coroutines.flow.first

class AdminVerificationRepository(
    private val apiService: AdminUserDetailsApiService,
    private val context: Context
) {
    private val dataStoreManager = DataStoreManager.getInstance(context)
    private val TAG = "AdminVerificationRepository"

    suspend fun getAllUserDetails(
        status: String? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<AdminUserDetailsResponse> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val queryMap = mutableMapOf<String, String>()
            status?.let { queryMap["status"] = it }
            search?.let { queryMap["search"] = it }
            queryMap["page"] = page.toString()
            queryMap["limit"] = limit.toString()

            val response = apiService.getAllUserDetails(queryMap)
            if (response.isSuccessful) {
                val adminResponse = response.body()
                if (adminResponse?.status == "success") {
                    Log.d(TAG, "User details retrieved successfully")
                    Result.success(adminResponse)
                } else {
                    Log.e(TAG, "API error: ${adminResponse?.message}")
                    Result.failure(Exception(adminResponse?.message ?: "Unknown error"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Error body: $errorBody")
                
                val errorMessage = try {
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"]?.toString() ?: "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error getting user details", e)
            Result.failure(e)
        }
    }

    suspend fun getUserDetailsById(userId: Int): Result<UserDetails> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val response = apiService.getUserDetailsById(userId)
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "User details by ID retrieved successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "Unknown error"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                
                val errorMessage = try {
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"]?.toString() ?: "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error getting user details by ID", e)
            Result.failure(e)
        }
    }

    suspend fun verifyKtp(
        userId: Int,
        isVerified: Boolean,
        notes: String? = null
    ): Result<UserDetails> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val request = VerificationRequest(
                is_verified = isVerified,
                notes = notes ?: ""
            )

            val response = apiService.verifyKtp(userId, request)
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "KTP verification completed successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "KTP verification failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                
                val errorMessage = try {
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"]?.toString() ?: "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error verifying KTP", e)
            Result.failure(e)
        }
    }

    suspend fun verifySim(
        userId: Int,
        isVerified: Boolean,
        notes: String? = null
    ): Result<UserDetails> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val request = VerificationRequest(
                is_verified = isVerified,
                notes = notes ?: ""
            )

            val response = apiService.verifySim(userId, request)
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "SIM verification completed successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "SIM verification failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                
                val errorMessage = try {
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"]?.toString() ?: "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error verifying SIM", e)
            Result.failure(e)
        }
    }

    suspend fun verifyUser(
        userId: Int,
        isVerified: Boolean,
        notes: String? = null
    ): Result<UserDetails> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val request = VerificationRequest(
                is_verified = isVerified,
                notes = notes ?: ""
            )

            val response = apiService.verifyUser(userId, request)
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "User verification completed successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "User verification failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")

                val errorMessage = try {
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"]?.toString() ?: "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error verifying user", e)
            Result.failure(e)
        }
    }

    suspend fun bulkVerify(
        userIds: List<Int>,
        action: String, // "approve" or "reject"
        notes: String? = null
    ): Result<String> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val request = BulkVerificationRequest(
                user_ids = userIds,
                action = action,
                notes = notes ?: ""
            )

            val response = apiService.bulkVerify(request)
            if (response.isSuccessful) {
                val bulkResponse = response.body()
                if (bulkResponse?.get("status") == "success") {
                    Log.d(TAG, "Bulk verification completed successfully")
                    Result.success(bulkResponse["message"]?.toString() ?: "Bulk verification completed")
                } else {
                    Log.e(TAG, "API error: ${bulkResponse?.get("message")}")
                    Result.failure(Exception(bulkResponse?.get("message")?.toString() ?: "Bulk verification failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                
                val errorMessage = try {
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(errorBody, Map::class.java)
                        errorResponse["message"]?.toString() ?: "HTTP ${response.code()}: ${response.message()}"
                    } else {
                        "HTTP ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "HTTP ${response.code()}: ${response.message()}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error bulk verifying", e)
            Result.failure(e)
        }
    }

    companion object {
        fun getInstance(context: Context): AdminVerificationRepository {
            val apiService = RetrofitClient.getInstance(context).create(AdminUserDetailsApiService::class.java)
            return AdminVerificationRepository(apiService, context)
        }
    }
}
