package com.example.rentalinn.repository

import android.content.Context
import android.util.Log
import com.example.rentalinn.api.UserProfileApiService
import com.example.rentalinn.model.User
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.DataStoreManager
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserProfileRepository(
    private val apiService: UserProfileApiService,
    private val context: Context
) {
    private val dataStoreManager = DataStoreManager.getInstance(context)
    private val TAG = "UserProfileRepository"

    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val response = apiService.getCurrentUserProfile()
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse?.status == "success") {
                    Log.d(TAG, "User profile retrieved successfully")
                    Result.success(userResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userResponse?.message}")
                    Result.failure(Exception(userResponse?.message ?: "Unknown error"))
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
            Log.e(TAG, "Network error getting user profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(name: String?, phoneNumber: String?): Result<User> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val request = mutableMapOf<String, String>()
            if (!name.isNullOrBlank()) {
                request["name"] = name.trim()
            }
            if (!phoneNumber.isNullOrBlank()) {
                request["phone_number"] = phoneNumber.trim()
            }

            val response = apiService.updateUserProfile(request)
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse?.status == "success") {
                    Log.d(TAG, "User profile updated successfully")
                    Result.success(userResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userResponse?.message}")
                    Result.failure(Exception(userResponse?.message ?: "Update failed"))
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
            Log.e(TAG, "Network error updating user profile", e)
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(imageFile: File): Result<User> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("profile_picture", imageFile.name, requestFile)

            val response = apiService.uploadProfilePicture(imagePart)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Upload response body: $responseBody")

                if (responseBody != null) {
                    try {
                        val success = responseBody["success"] as? Boolean ?: false
                        if (success) {
                            val userData = responseBody["user"] as? Map<String, Any>
                            if (userData != null) {
                                val gson = com.google.gson.Gson()
                                val userJson = gson.toJson(userData)
                                val user = gson.fromJson(userJson, User::class.java)
                                Log.d(TAG, "Profile picture uploaded successfully")
                                Result.success(user)
                            } else {
                                Log.e(TAG, "User data not found in response")
                                Result.failure(Exception("User data not found in response"))
                            }
                        } else {
                            val message = responseBody["message"] as? String ?: "Upload failed"
                            Log.e(TAG, "API error: $message")
                            Result.failure(Exception(message))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing upload response", e)
                        Result.failure(Exception("Error parsing response: ${e.message}"))
                    }
                } else {
                    Log.e(TAG, "Empty response body")
                    Result.failure(Exception("Empty response body"))
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
            Log.e(TAG, "Network error uploading profile picture", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfileWithPicture(name: String?, phoneNumber: String?, imageFile: File): Result<User> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("profile_picture", imageFile.name, requestFile)

            val nameBody = name?.let { okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
            val phoneBody = phoneNumber?.let { okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), it) }

            val response = apiService.updateProfileWithPicture(nameBody, phoneBody, imagePart)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(TAG, "Response body: $responseBody")

                if (responseBody != null) {
                    try {
                        val success = responseBody["success"] as? Boolean ?: false
                        if (success) {
                            val userData = responseBody["user"] as? Map<String, Any>
                            if (userData != null) {
                                val gson = com.google.gson.Gson()
                                val userJson = gson.toJson(userData)
                                val user = gson.fromJson(userJson, User::class.java)
                                Log.d(TAG, "Profile with picture updated successfully")
                                Result.success(user)
                            } else {
                                Log.e(TAG, "User data not found in response")
                                Result.failure(Exception("User data not found in response"))
                            }
                        } else {
                            val message = responseBody["message"] as? String ?: "Update failed"
                            Log.e(TAG, "API error: $message")
                            Result.failure(Exception(message))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing response", e)
                        Result.failure(Exception("Error parsing response: ${e.message}"))
                    }
                } else {
                    Log.e(TAG, "Empty response body")
                    Result.failure(Exception("Empty response body"))
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
            Log.e(TAG, "Network error updating profile with picture", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProfilePicture(): Result<User> {
        return try {
            val token = dataStoreManager.token.first()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("No authentication token"))
            }

            val response = apiService.deleteProfilePicture()
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse?.status == "success") {
                    Log.d(TAG, "Profile picture deleted successfully")
                    Result.success(userResponse.data)
                } else {
                    Log.e(TAG, "API error: ${userResponse?.message}")
                    Result.failure(Exception(userResponse?.message ?: "Delete failed"))
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
            Log.e(TAG, "Network error deleting profile picture", e)
            Result.failure(e)
        }
    }

    companion object {
        fun getInstance(context: Context): UserProfileRepository {
            val apiService = RetrofitClient.getInstance(context).create(UserProfileApiService::class.java)
            return UserProfileRepository(apiService, context)
        }
    }
}
