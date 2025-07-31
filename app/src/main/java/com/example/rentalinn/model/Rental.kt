package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Rental(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("vehicle_id")
    val vehicleId: Int?,
    @SerializedName("unit_id")
    val unitId: Int?,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("total_days")
    val totalDays: Int,
    @SerializedName("price_per_day")
    val pricePerDay: Double,
    @SerializedName("total_amount")
    val totalAmount: Double,
    val status: String,
    @SerializedName("admin_approval_status")
    val adminApprovalStatus: String? = "pending",
    @SerializedName("approved_by")
    val approvedBy: Int? = null,
    @SerializedName("approved_at")
    val approvedAt: String? = null,
    @SerializedName("rejection_reason")
    val rejectionReason: String? = null,
    @SerializedName("pickup_location")
    val pickupLocation: String?,
    @SerializedName("pickup_latitude")
    val pickupLatitude: Double?,
    @SerializedName("pickup_longitude")
    val pickupLongitude: Double?,
    @SerializedName("return_location")
    val returnLocation: String?,
    @SerializedName("return_latitude")
    val returnLatitude: Double?,
    @SerializedName("return_longitude")
    val returnLongitude: Double?,
    val notes: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val vehicle: Vehicle? = null,
    val unit: VehicleUnit? = null,
    val user: User? = null,
    val payment: Payment? = null
) {
    // Helper methods for vehicle unit support
    fun getVehicleInfo(): Vehicle? {
        // Prioritize unit.vehicle over direct vehicle relationship
        return unit?.vehicle ?: vehicle
    }

    fun getUnitInfo(): VehicleUnit? {
        return unit
    }

    fun getPlateNumber(): String {
        return unit?.getFormattedPlateNumber() ?: "N/A"
    }

    fun hasVehicleUnit(): Boolean {
        return unitId != null
    }

    fun getVehicleTitle(): String {
        return getVehicleInfo()?.title ?: "Unknown Vehicle"
    }

    fun getVehicleBrand(): String {
        return getVehicleInfo()?.brand ?: "Unknown"
    }

    fun getVehicleModel(): String {
        return getVehicleInfo()?.model ?: "Unknown"
    }

    fun getFormattedTotalAmount(): String = "Rp ${totalAmount.toInt()}"

    fun getStatusColor(): androidx.compose.ui.graphics.Color {
        return when (status) {
            "pending" -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
            "confirmed" -> androidx.compose.ui.graphics.Color(0xFF10B981)
            "active" -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
            "completed" -> androidx.compose.ui.graphics.Color(0xFF6B7280)
            "cancelled" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
            else -> androidx.compose.ui.graphics.Color(0xFF6B7280)
        }
    }

    fun getStatusDisplayName(): String {
        return when (status) {
            "pending" -> "Pending Payment"
            "confirmed" -> "Confirmed"
            "active" -> "Active"
            "completed" -> "Completed"
            "cancelled" -> "Cancelled"
            else -> status.replaceFirstChar { it.uppercase() }
        }
    }

    fun canBeCancelled(): Boolean {
        return status in listOf("pending", "confirmed")
    }

    fun isActive(): Boolean {
        return status == "active"
    }

    fun isPending(): Boolean {
        return status == "pending"
    }
}

data class Payment(
    val id: Int,
    @SerializedName("rental_id")
    val rentalId: Int,
    @SerializedName("user_id")
    val userId: Int,
    val amount: Double,
    @SerializedName("payment_method")
    val paymentMethod: String?,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("transaction_id")
    val transactionId: String?,
    @SerializedName("midtrans_transaction_id")
    val midtransTransactionId: String?,
    @SerializedName("midtrans_order_id")
    val midtransOrderId: String?,
    @SerializedName("snap_token")
    val snapToken: String?,
    @SerializedName("snap_redirect_url")
    val snapRedirectUrl: String?,
    @SerializedName("paid_at")
    val paidAt: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
) {
    fun isPaid(): Boolean {
        return paymentStatus in listOf("settlement", "capture")
    }
    
    fun isPending(): Boolean {
        return paymentStatus == "pending"
    }
    
    fun isFailed(): Boolean {
        return paymentStatus in listOf("deny", "cancel", "expire", "failure")
    }
    
    fun getStatusColor(): androidx.compose.ui.graphics.Color {
        return when (paymentStatus) {
            "pending" -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
            "settlement", "capture" -> androidx.compose.ui.graphics.Color(0xFF10B981)
            "deny", "cancel", "expire", "failure" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
            else -> androidx.compose.ui.graphics.Color(0xFF6B7280)
        }
    }
    
    fun getStatusDisplayName(): String {
        return when (paymentStatus) {
            "pending" -> "Pending"
            "settlement" -> "Paid"
            "capture" -> "Paid"
            "deny" -> "Denied"
            "cancel" -> "Cancelled"
            "expire" -> "Expired"
            "failure" -> "Failed"
            else -> paymentStatus.capitalize()
        }
    }
}

data class CreateRentalRequest(
    @SerializedName("vehicle_id")
    val vehicleId: Int? = null,
    @SerializedName("unit_id")
    val unitId: Int? = null,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("pickup_location")
    val pickupLocation: String?,
    @SerializedName("pickup_latitude")
    val pickupLatitude: Double?,
    @SerializedName("pickup_longitude")
    val pickupLongitude: Double?,
    @SerializedName("return_location")
    val returnLocation: String?,
    @SerializedName("return_latitude")
    val returnLatitude: Double?,
    @SerializedName("return_longitude")
    val returnLongitude: Double?,
    val notes: String?
)

data class CreateRentalResponse(
    val success: Boolean,
    val message: String,
    val data: CreateRentalData?
)

data class CreateRentalData(
    val rental: Rental,
    val payment: PaymentData
)

data class PaymentData(
    @SerializedName("snap_token")
    val snapToken: String,
    @SerializedName("redirect_url")
    val redirectUrl: String
)

data class RentalsResponse(
    val success: Boolean,
    val data: RentalsData
)

data class RentalsData(
    val rentals: List<Rental>,
    val pagination: Pagination
)

data class Pagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    @SerializedName("totalPages")
    val totalPages: Int
)

// Additional Response Models
data class RentalResponse(
    val success: Boolean,
    val data: Rental
)

data class BasicResponse(
    val success: Boolean,
    val message: String
)

data class PaymentResponse(
    val success: Boolean,
    val data: PaymentStatusData
)

data class PaymentStatusData(
    val payment: Payment,
    @SerializedName("midtrans_status")
    val midtransStatus: Any?
)

data class PaymentRetryResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentData
)

data class PaymentsResponse(
    val success: Boolean,
    val data: PaymentsData
)

data class PaymentsData(
    val payments: List<Payment>,
    val pagination: Pagination
)

data class UserStatsResponse(
    val success: Boolean,
    val data: UserStatsData
)

data class UserStatsData(
    @SerializedName("total_trips")
    val totalTrips: Int,
    @SerializedName("active_rentals")
    val activeRentals: Int,
    @SerializedName("active_rental_details")
    val activeRentalDetails: List<Rental>
)
