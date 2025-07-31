package com.example.rentalinn.api

import com.example.rentalinn.model.User
import com.example.rentalinn.model.UsersResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserProfileApiService {
    
    @GET("profile")
    suspend fun getCurrentUserProfile(): Response<UsersResponse>

    @PUT("profile")
    suspend fun updateUserProfile(
        @Body request: Map<String, String>
    ): Response<UsersResponse>

    @Multipart
    @PUT("profile")
    suspend fun uploadProfilePicture(
        @Part profilePicture: MultipartBody.Part
    ): Response<Map<String, Any>>

    @Multipart
    @PUT("profile")
    suspend fun updateProfileWithPicture(
        @Part("name") name: okhttp3.RequestBody?,
        @Part("phone_number") phoneNumber: okhttp3.RequestBody?,
        @Part profilePicture: MultipartBody.Part
    ): Response<Map<String, Any>>

    @DELETE("profile/delete-picture")
    suspend fun deleteProfilePicture(): Response<UsersResponse>
}
