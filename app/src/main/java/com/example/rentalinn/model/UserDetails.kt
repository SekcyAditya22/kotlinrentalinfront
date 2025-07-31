
package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class UserDetails(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("ktp_number")
    val ktpNumber: String?,
    @SerializedName("ktp_photo")
    val ktpPhoto: String?,
    @SerializedName("sim_number")
    val simNumber: String?,
    @SerializedName("sim_photo")
    val simPhoto: String?,
    val address: String?,
    @SerializedName("date_of_birth")
    val dateOfBirth: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    val gender: String?,
    @SerializedName("emergency_contact_name")
    val emergencyContactName: String?,
    @SerializedName("emergency_contact_phone")
    val emergencyContactPhone: String?,
    @SerializedName("emergency_contact_relation")
    val emergencyContactRelation: String?,
    @SerializedName("is_ktp_verified")
    val isKtpVerified: Boolean,
    @SerializedName("is_sim_verified")
    val isSimVerified: Boolean,
    @SerializedName("verification_notes")
    val verificationNotes: String?,
    @SerializedName("verified_at")
    val verifiedAt: String?,
    @SerializedName("verified_by")
    val verifiedBy: Int?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    val user: User? = null
) {
    // Check if documents are complete
    fun isDocumentsComplete(): Boolean {
        return !ktpNumber.isNullOrEmpty() && 
               !ktpPhoto.isNullOrEmpty() && 
               !simNumber.isNullOrEmpty() && 
               !simPhoto.isNullOrEmpty()
    }

    // Check if documents are verified
    fun isDocumentsVerified(): Boolean {
        return isKtpVerified && isSimVerified
    }

    // Get verification status
    fun getVerificationStatus(): String {
        return when {
            isDocumentsVerified() -> "verified"
            isDocumentsComplete() -> "pending"
            else -> "incomplete"
        }
    }

    // Get verification status display text
    fun getVerificationStatusDisplay(): String {
        return when (getVerificationStatus()) {
            "verified" -> "Verified"
            "pending" -> "Pending Verification"
            else -> "Documents Incomplete"
        }
    }

    // Get formatted date of birth
    fun getFormattedDateOfBirth(): String {
        return dateOfBirth?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val date = inputFormat.parse(it)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    "Not specified"
                }
            } catch (e: Exception) {
                "Invalid date"
            }
        } ?: "Not specified"
    }

    // Get formatted verified date
    fun getFormattedVerifiedAt(): String {
        return verifiedAt?.let {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val date = formatter.parse(it)
                if (date != null) {
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
                } else {
                    "Unknown"
                }
            } catch (e: Exception) {
                "Unknown"
            }
        } ?: "Not verified"
    }

    // Get gender display text
    fun getGenderDisplay(): String {
        return when (gender?.lowercase()) {
            "male" -> "Male"
            "female" -> "Female"
            else -> "Not specified"
        }
    }

    // Get masked KTP number for display
    fun getMaskedKtpNumber(): String {
        return ktpNumber?.let {
            if (it.length >= 8) {
                "${it.take(4)}****${it.takeLast(4)}"
            } else {
                "****"
            }
        } ?: "Not provided"
    }

    // Get masked SIM number for display
    fun getMaskedSimNumber(): String {
        return simNumber?.let {
            if (it.length >= 6) {
                "${it.take(3)}***${it.takeLast(3)}"
            } else {
                "***"
            }
        } ?: "Not provided"
    }

    // Check if emergency contact is complete
    fun isEmergencyContactComplete(): Boolean {
        return !emergencyContactName.isNullOrEmpty() && 
               !emergencyContactPhone.isNullOrEmpty() && 
               !emergencyContactRelation.isNullOrEmpty()
    }

    // Get completion percentage
    fun getCompletionPercentage(): Int {
        var completed = 0
        val total = 8 // Total fields to check

        if (!ktpNumber.isNullOrEmpty()) completed++
        if (!ktpPhoto.isNullOrEmpty()) completed++
        if (!simNumber.isNullOrEmpty()) completed++
        if (!simPhoto.isNullOrEmpty()) completed++
        if (!address.isNullOrEmpty()) completed++
        if (!dateOfBirth.isNullOrEmpty()) completed++
        if (!placeOfBirth.isNullOrEmpty()) completed++
        if (isEmergencyContactComplete()) completed++

        return (completed * 100) / total
    }
}

data class UserDetailsResponse(
    val status: String,
    val message: String,
    val data: UserDetails
)

data class AdminUserDetailsResponse(
    val status: String,
    val message: String,
    val data: AdminUserDetailsData
)

data class AdminUserDetailsData(
    val userDetails: List<UserDetails>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)

data class CreateUserDetailsRequest(
    @SerializedName("ktp_number")
    val ktpNumber: String?,
    @SerializedName("sim_number")
    val simNumber: String?,
    val address: String?,
    @SerializedName("date_of_birth")
    val dateOfBirth: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    val gender: String?,
    @SerializedName("emergency_contact_name")
    val emergencyContactName: String?,
    @SerializedName("emergency_contact_phone")
    val emergencyContactPhone: String?,
    @SerializedName("emergency_contact_relation")
    val emergencyContactRelation: String?
)

data class UpdateUserDetailsRequest(
    @SerializedName("ktp_number")
    val ktpNumber: String?,
    @SerializedName("sim_number")
    val simNumber: String?,
    val address: String?,
    @SerializedName("date_of_birth")
    val dateOfBirth: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    val gender: String?,
    @SerializedName("emergency_contact_name")
    val emergencyContactName: String?,
    @SerializedName("emergency_contact_phone")
    val emergencyContactPhone: String?,
    @SerializedName("emergency_contact_relation")
    val emergencyContactRelation: String?
)

