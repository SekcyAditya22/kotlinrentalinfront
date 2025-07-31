package com.example.rentalinn.model

data class AuthResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val user: User?
) 