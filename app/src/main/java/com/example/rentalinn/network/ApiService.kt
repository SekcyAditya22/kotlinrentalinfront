package com.example.rentalinn.network

import com.example.rentalinn.model.AuthResponse
import com.example.rentalinn.model.LoginRequest
import com.example.rentalinn.model.RegisterRequest
import com.example.rentalinn.model.UsersResponse
import com.example.rentalinn.model.UsersListResponse
import com.example.rentalinn.model.User
import com.example.rentalinn.model.CreateUserRequest
import com.example.rentalinn.model.ChangePasswordRequest
import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.model.AdminStatsResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // User endpoints
    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("users")
    suspend fun getUsers(): UsersListResponse

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): UsersResponse

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UsersResponse

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): UsersResponse

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): AuthResponse

    @PUT("change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): AuthResponse

    @DELETE("profile")
    suspend fun deleteCurrentUser(): AuthResponse
    
    // Vehicle endpoints
    @GET("vehicles")
    suspend fun getAllVehicles(): Response<VehicleListResponse>
    
    @GET("vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: Int): Response<VehicleResponse>
    
    @GET("vehicles/available")
    suspend fun getAvailableVehicles(): Response<VehicleListResponse>

    @GET("vehicles/with-availability")
    suspend fun getVehiclesWithAvailability(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<VehicleListResponse>

    @GET("vehicles/{id}/availability")
    suspend fun checkVehicleAvailability(
        @Path("id") id: Int,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<VehicleAvailabilityResponse>

    @GET("vehicles/category/{category}")
    suspend fun getVehiclesByCategory(@Path("category") category: String): Response<VehicleListResponse>
    
    @Multipart
    @POST("vehicles")
    suspend fun createVehicle(
        @Part("title") title: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("model") model: RequestBody,
        @Part("vehicle_category") vehicleCategory: RequestBody,
        @Part("year") year: RequestBody,
        @Part("license_plate") licensePlate: RequestBody,
        @Part("price_per_day") pricePerDay: RequestBody,
        @Part("unit") unit: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("transmission") transmission: RequestBody,
        @Part("fuel_type") fuelType: RequestBody,
        @Part("passenger_capacity") passengerCapacity: RequestBody,
        @Part("features") features: RequestBody?,
        @Part photos: List<MultipartBody.Part>
    ): Response<VehicleResponse>

    @Multipart
    @POST("vehicles")
    suspend fun createVehicleWithUnits(
        @Part("title") title: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("model") model: RequestBody,
        @Part("vehicle_category") vehicleCategory: RequestBody,
        @Part("year") year: RequestBody,
        @Part("price_per_day") pricePerDay: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("transmission") transmission: RequestBody,
        @Part("fuel_type") fuelType: RequestBody,
        @Part("passenger_capacity") passengerCapacity: RequestBody,
        @Part("features") features: RequestBody?,
        @Part("units") units: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): Response<VehicleResponse>
    
    @Multipart
    @PUT("vehicles/{id}")
    suspend fun updateVehicle(
        @Path("id") id: Int,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part photos: List<MultipartBody.Part>
    ): Response<VehicleResponse>
    
    @DELETE("vehicles/{id}")
    suspend fun deleteVehicle(@Path("id") id: Int): Response<MessageResponse>

    // Auto-update payment status
    @POST("payments/auto-update/{orderId}")
    suspend fun autoUpdatePayment(
        @Path("orderId") orderId: String
    ): Response<MessageResponse>

    // Auto-update payment by rental ID (more reliable)
    @POST("payments/auto-update-rental/{rentalId}")
    suspend fun autoUpdatePaymentByRental(
        @Path("rentalId") rentalId: Int
    ): Response<MessageResponse>

    // Admin rental management
    @GET("admin/rentals/pending")
    suspend fun getPendingRentals(): Response<RentalListResponse>

    @GET("admin/rentals")
    suspend fun getAllRentals(): Response<RentalListResponse>

    @PATCH("admin/rentals/{rentalId}/approve")
    suspend fun approveRental(
        @Path("rentalId") rentalId: Int
    ): Response<MessageResponse>

    // Admin statistics
    @GET("admin/rentals/stats")
    suspend fun getAdminRentalStats(): Response<AdminStatsResponse>

    @PATCH("admin/rentals/{rentalId}/reject")
    suspend fun rejectRental(
        @Path("rentalId") rentalId: Int,
        @Body request: com.example.rentalinn.repository.RejectRentalRequest
    ): Response<MessageResponse>

    @PATCH("admin/rentals/{rentalId}/complete")
    suspend fun completeRental(
        @Path("rentalId") rentalId: Int
    ): Response<MessageResponse>
}

data class VehicleResponse(
    val status: String,
    val message: String?,
    val data: Vehicle
)

data class VehicleListResponse(
    val status: String,
    val data: List<Vehicle>
)

data class VehicleAvailabilityResponse(
    val status: String,
    val data: VehicleAvailabilityData
)

data class VehicleAvailabilityData(
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("total_units")
    val totalUnits: Int,
    @SerializedName("available_units")
    val availableUnits: Int,
    @SerializedName("is_available")
    val isAvailable: Boolean,
    @SerializedName("vehicle_status")
    val vehicleStatus: String,
    @SerializedName("overlapping_rentals")
    val overlappingRentals: Int
)

data class MessageResponse(
    val status: String,
    val message: String
)

data class RentalListResponse(
    val success: Boolean,
    val data: List<com.example.rentalinn.model.Rental>
)