package com.example.rentalinn.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val role: String
) 