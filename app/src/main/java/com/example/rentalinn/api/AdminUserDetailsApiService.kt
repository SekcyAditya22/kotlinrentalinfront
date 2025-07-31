package com.example.rentalinn.api

import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.model.UserDetailsResponse
import com.example.rentalinn.model.AdminUserDetailsResponse
import retrofit2.Response
import retrofit2.http.*

data class VerificationRequest(
    val is_verified: Boolean,
    val notes: String
)

data class BulkVerificationRequest(
    val user_ids: List<Int>,
    val action: String,
    val notes: String
)

interface AdminUserDetailsApiService {

    @GET("admin/user-details")
    suspend fun getAllUserDetails(
        @QueryMap options: Map<String, String>
    ): Response<AdminUserDetailsResponse>

    @GET("admin/user-details/{userId}")
    suspend fun getUserDetailsById(
        @Path("userId") userId: Int
    ): Response<UserDetailsResponse>

    @PUT("admin/user-details/{userId}/verify-ktp")
    suspend fun verifyKtp(
        @Path("userId") userId: Int,
        @Body request: VerificationRequest
    ): Response<UserDetailsResponse>

    @PUT("admin/user-details/{userId}/verify-sim")
    suspend fun verifySim(
        @Path("userId") userId: Int,
        @Body request: VerificationRequest
    ): Response<UserDetailsResponse>

    @PUT("admin/user-details/{userId}/verify-user")
    suspend fun verifyUser(
        @Path("userId") userId: Int,
        @Body request: VerificationRequest
    ): Response<UserDetailsResponse>

    @POST("admin/user-details/bulk-verify")
    suspend fun bulkVerify(
        @Body request: BulkVerificationRequest
    ): Response<Map<String, String>>
}
