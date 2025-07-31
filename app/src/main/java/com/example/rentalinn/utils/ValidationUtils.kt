package com.example.rentalinn.utils

object ValidationUtils {
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Nama tidak boleh kosong"
            name.length < 3 -> "Nama minimal 3 karakter"
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email tidak boleh kosong"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email tidak valid"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password tidak boleh kosong"
            password.length < 6 -> "Password minimal 6 karakter"
            !password.any { it.isDigit() } -> "Password harus mengandung angka"
            !password.any { it.isLetter() } -> "Password harus mengandung huruf"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Konfirmasi password tidak boleh kosong"
            confirmPassword != password -> "Password tidak cocok"
            else -> null
        }
    }

    fun validateForm(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return validateName(name) == null &&
                validateEmail(email) == null &&
                validatePassword(password) == null &&
                validateConfirmPassword(password, confirmPassword) == null
    }
} 