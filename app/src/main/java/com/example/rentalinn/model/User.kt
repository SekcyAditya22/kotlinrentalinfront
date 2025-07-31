package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("last_login")
    val lastLogin: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
) {
    fun isOnline(): Boolean {
        return lastLogin?.let {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val lastLoginDate = formatter.parse(it)
                val currentTime = Date()
                if (lastLoginDate != null) {
                    val diffInMinutes = (currentTime.time - lastLoginDate.time) / (1000 * 60)
                    diffInMinutes <= 5 // Consider online if last login was within 5 minutes
                } else false
            } catch (e: Exception) {
                false
            }
        } ?: false
    }

    fun getFormattedLastActive(): String {
        return lastLogin?.let {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val lastLoginDate = formatter.parse(it)
                val currentTime = Date()
                if (lastLoginDate != null) {
                    val diffInMinutes = (currentTime.time - lastLoginDate.time) / (1000 * 60)
                    when {
                        diffInMinutes < 1 -> "Just now"
                        diffInMinutes < 60 -> "$diffInMinutes min ago"
                        diffInMinutes < 1440 -> "${diffInMinutes / 60} hours ago"
                        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(lastLoginDate)
                    }
                } else {
                    "Unknown"
                }
            } catch (e: Exception) {
                "Unknown"
            }
        } ?: "Never"
    }

    fun getFormattedJoinDate(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            val date = formatter.parse(createdAt)
            if (date != null) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

data class UsersResponse(
    val status: String,
    val message: String,
    val data: User
)

data class UsersListResponse(
    val status: String,
    val message: String,
    val data: List<User>
) 