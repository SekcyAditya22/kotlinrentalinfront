package com.example.rentalinn.network

import com.example.rentalinn.model.*
import retrofit2.Response
import retrofit2.http.*

interface RentalApiService {
    
    @POST("rentals")
    suspend fun createRental(
        @Body request: CreateRentalRequest
    ): Response<CreateRentalResponse>

    @GET("rentals/stats")
    suspend fun getUserStats(): Response<UserStatsResponse>

    @GET("rentals")
    suspend fun getUserRentals(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null
    ): Response<RentalsResponse>

    @GET("rentals/{id}")
    suspend fun getRentalById(
        @Path("id") id: Int
    ): Response<RentalResponse>

    @PATCH("rentals/{id}/cancel")
    suspend fun cancelRental(
        @Path("id") id: Int
    ): Response<BasicResponse>

    @GET("payments/status/{orderId}")
    suspend fun getPaymentStatus(
        @Path("orderId") orderId: String
    ): Response<PaymentResponse>

    @POST("payments/retry/{rentalId}")
    suspend fun retryPayment(
        @Path("rentalId") rentalId: Int
    ): Response<PaymentRetryResponse>

    @GET("payments")
    suspend fun getUserPayments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null
    ): Response<PaymentsResponse>
}
