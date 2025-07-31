package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName

data class VehicleUnit(
    val id: Int,
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("plate_number")
    val plateNumber: String,
    val status: String, // available, rented, maintenance, out_of_service
    @SerializedName("current_location")
    val currentLocation: String?,
    @SerializedName("current_latitude")
    val currentLatitude: Double?,
    @SerializedName("current_longitude")
    val currentLongitude: Double?,
    val mileage: Int?,
    @SerializedName("last_maintenance_date")
    val lastMaintenanceDate: String?,
    @SerializedName("next_maintenance_date")
    val nextMaintenanceDate: String?,
    val notes: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val vehicle: Vehicle? = null
) {
    fun isAvailable(): Boolean = status == "available"
    
    fun isRented(): Boolean = status == "rented"
    
    fun isInMaintenance(): Boolean = status == "maintenance"
    
    fun isOutOfService(): Boolean = status == "out_of_service"
    
    fun getFormattedPlateNumber(): String = plateNumber.uppercase()
    
    fun getStatusDisplayName(): String {
        return when (status) {
            "available" -> "Available"
            "rented" -> "Rented"
            "maintenance" -> "In Maintenance"
            "out_of_service" -> "Out of Service"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }
    
    fun getStatusColor(): String {
        return when (status) {
            "available" -> "#10B981" // Green
            "rented" -> "#F59E0B"    // Orange
            "maintenance" -> "#EF4444" // Red
            "out_of_service" -> "#6B7280" // Gray
            else -> "#6B7280"
        }
    }
    
    fun needsMaintenance(): Boolean {
        if (nextMaintenanceDate.isNullOrBlank()) return false
        // Simple date comparison - in real app you'd use proper date parsing
        return false // Simplified for now
    }
    
    fun getMaintenanceStatus(): String {
        return if (needsMaintenance()) "overdue" else "ok"
    }
}

data class CreateVehicleUnitRequest(
    @SerializedName("vehicle_id")
    val vehicleId: Int,
    @SerializedName("plate_number")
    val plateNumber: String,
    val status: String = "available",
    @SerializedName("current_location")
    val currentLocation: String? = null,
    @SerializedName("current_latitude")
    val currentLatitude: Double? = null,
    @SerializedName("current_longitude")
    val currentLongitude: Double? = null,
    val mileage: Int = 0,
    val notes: String? = null
)

data class UpdateVehicleUnitRequest(
    @SerializedName("plate_number")
    val plateNumber: String? = null,
    val status: String? = null,
    @SerializedName("current_location")
    val currentLocation: String? = null,
    @SerializedName("current_latitude")
    val currentLatitude: Double? = null,
    @SerializedName("current_longitude")
    val currentLongitude: Double? = null,
    val mileage: Int? = null,
    @SerializedName("last_maintenance_date")
    val lastMaintenanceDate: String? = null,
    @SerializedName("next_maintenance_date")
    val nextMaintenanceDate: String? = null,
    val notes: String? = null
)

data class VehicleUnitResponse(
    val success: Boolean,
    val message: String? = null,
    val data: VehicleUnit? = null
)

data class VehicleUnitsResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<VehicleUnit>? = null
)
