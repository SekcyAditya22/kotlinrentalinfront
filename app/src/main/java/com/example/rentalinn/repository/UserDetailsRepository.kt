package com.example.rentalinn.repository

import android.content.Context
import android.util.Log
import com.example.rentalinn.api.UserDetailsApiService
import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.model.CreateUserDetailsRequest
import com.example.rentalinn.model.UpdateUserDetailsRequest
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.DataStoreManager
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserDetailsRepository(
    private val apiService: UserDetailsApiService,
    private val context: Context
) {
    private val dataStoreManager = DataStoreManager.getInstance(context)
    private val TAG = "UserDetailsRepository"

    suspend fun getUserDetails(): Result<UserDetails?> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val response = apiService.getUserDetails()
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "User details retrieved successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "Unknown error"))
                }
            } else {
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error getting user details", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserDetails(request: UpdateUserDetailsRequest): Result<UserDetails> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val response = apiService.updateUserDetails(request)
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "User details updated successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "Update failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                Log.e(TAG, "Error body: $errorBody")

                // Try to parse error message from response
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
            Log.e(TAG, "Network error updating user details", e)
            Result.failure(e)
        }
    }

    suspend fun completeProfile(request: CreateUserDetailsRequest): Result<UserDetails> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val response = apiService.completeProfile(request)
            if (response.isSuccessful) {
                val userDetailsResponse = response.body()
                if (userDetailsResponse?.status == "success") {
                    Log.d(TAG, "Profile completed successfully")
                    Result.success(userDetailsResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userDetailsResponse?.message}")
                    Result.failure(Exception(userDetailsResponse?.message ?: "Complete profile failed"))
                }
            } else {
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error completing profile", e)
            Result.failure(e)
        }
    }

    suspend fun uploadKtpPhoto(photoFile: File): Result<String> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val requestFile = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("ktp_photo", photoFile.name, requestFile)

            val response = apiService.uploadKtpPhoto(photoPart)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody?.get("status") == "success") {
                    val data = responseBody["data"] as? Map<String, Any>
                    val ktpPhotoPath = data?.get("ktp_photo") as? String
                    Log.d(TAG, "KTP photo uploaded successfully: $ktpPhotoPath")
                    Result.success(ktpPhotoPath ?: "")
                } else {
                    Log.e(TAG, "API error: ${responseBody?.get("message")}")
                    Result.failure(Exception(responseBody?.get("message")?.toString() ?: "Upload failed"))
                }
            } else {
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error uploading KTP photo", e)
            Result.failure(e)
        }
    }

    suspend fun uploadSimPhoto(photoFile: File): Result<String> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val requestFile = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData("sim_photo", photoFile.name, requestFile)

            val response = apiService.uploadSimPhoto(photoPart)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody?.get("status") == "success") {
                    val data = responseBody["data"] as? Map<String, Any>
                    val simPhotoPath = data?.get("sim_photo") as? String
                    Log.d(TAG, "SIM photo uploaded successfully: $simPhotoPath")
                    Result.success(simPhotoPath ?: "")
                } else {
                    Log.e(TAG, "API error: ${responseBody?.get("message")}")
                    Result.failure(Exception(responseBody?.get("message")?.toString() ?: "Upload failed"))
                }
            } else {
                Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error uploading SIM photo", e)
            Result.failure(e)
        }
    }

    companion object {
        fun getInstance(context: Context): UserDetailsRepository {
            val apiService = RetrofitClient.getInstance(context).create(UserDetailsApiService::class.java)
            return UserDetailsRepository(apiService, context)
        }
    }
}
