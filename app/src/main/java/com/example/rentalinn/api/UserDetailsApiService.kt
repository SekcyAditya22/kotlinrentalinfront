package com.example.rentalinn.api

import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.model.UserDetailsResponse
import com.example.rentalinn.model.CreateUserDetailsRequest
import com.example.rentalinn.model.UpdateUserDetailsRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserDetailsApiService {
    
    @GET("user-details")
    suspend fun getUserDetails(): Response<UserDetailsResponse>
    
    @PUT("user-details")
    suspend fun updateUserDetails(
        @Body request: UpdateUserDetailsRequest
    ): Response<UserDetailsResponse>
    
    @POST("user-details/complete")
    suspend fun completeProfile(
        @Body request: CreateUserDetailsRequest
    ): Response<UserDetailsResponse>
    
    @PUT("user-details/ktp")
    suspend fun updateKtpNumber(
        @Body request: Map<String, String>
    ): Response<UserDetailsResponse>
    
    @PUT("user-details/sim")
    suspend fun updateSimNumber(
        @Body request: Map<String, String>
    ): Response<UserDetailsResponse>
    
    @PUT("user-details/emergency-contact")
    suspend fun updateEmergencyContact(
        @Body request: Map<String, String>
    ): Response<UserDetailsResponse>
    
    @Multipart
    @POST("user-details/upload/ktp")
    suspend fun uploadKtpPhoto(
        @Part ktpPhoto: MultipartBody.Part
    ): Response<Map<String, Any>>
    
    @Multipart
    @POST("user-details/upload/sim")
    suspend fun uploadSimPhoto(
        @Part simPhoto: MultipartBody.Part
    ): Response<Map<String, Any>>
}
