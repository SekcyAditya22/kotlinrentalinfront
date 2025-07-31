package com.example.rentalinn.network

import com.example.rentalinn.model.*
import retrofit2.Response
import retrofit2.http.*

interface VehicleUnitApiService {
    
    @GET("vehicle-units")
    suspend fun getAllVehicleUnits(
        @Query("vehicle_id") vehicleId: Int? = null,
        @Query("status") status: String? = null
    ): Response<VehicleUnitsResponse>
    
    @GET("vehicle-units/{id}")
    suspend fun getVehicleUnitById(@Path("id") id: Int): Response<VehicleUnitResponse>
    
    @GET("vehicles/{vehicle_id}/available-units")
    suspend fun getAvailableUnits(@Path("vehicle_id") vehicleId: Int): Response<VehicleUnitsResponse>
    
    @POST("vehicle-units")
    suspend fun createVehicleUnit(@Body request: CreateVehicleUnitRequest): Response<VehicleUnitResponse>
    
    @PUT("vehicle-units/{id}")
    suspend fun updateVehicleUnit(
        @Path("id") id: Int,
        @Body request: UpdateVehicleUnitRequest
    ): Response<VehicleUnitResponse>
    
    @DELETE("vehicle-units/{id}")
    suspend fun deleteVehicleUnit(@Path("id") id: Int): Response<VehicleUnitResponse>
}
