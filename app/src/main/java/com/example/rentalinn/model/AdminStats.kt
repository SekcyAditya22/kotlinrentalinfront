package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName

data class AdminStatsResponse(
    val success: Boolean,
    val data: AdminStats
)

data class AdminStats(
    @SerializedName("total_rentals")
    val totalRentals: Int,
    @SerializedName("pending_approvals")
    val pendingApprovals: Int,
    @SerializedName("active_rentals")
    val activeRentals: Int,
    @SerializedName("completed_rentals")
    val completedRentals: Int,
    @SerializedName("total_revenue")
    val totalRevenue: Double
)

data class RecentRentalsResponse(
    val success: Boolean,
    val data: List<Rental>
)

data class VehicleStatsResponse(
    val success: Boolean,
    val data: VehicleStats
)

data class VehicleStats(
    @SerializedName("total_vehicles")
    val totalVehicles: Int,
    @SerializedName("available_vehicles")
    val availableVehicles: Int,
    @SerializedName("rented_vehicles")
    val rentedVehicles: Int
)

data class AdminUserStatsResponse(
    val success: Boolean,
    val data: AdminUserStats
)

data class AdminUserStats(
    @SerializedName("total_users")
    val totalUsers: Int,
    @SerializedName("verified_users")
    val verifiedUsers: Int,
    @SerializedName("unverified_users")
    val unverifiedUsers: Int
)
