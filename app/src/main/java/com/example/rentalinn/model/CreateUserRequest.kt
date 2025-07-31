package com.example.rentalinn.model

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    val role: String = "user",
    @SerializedName("is_verified")
    val isVerified: Boolean = false
) 