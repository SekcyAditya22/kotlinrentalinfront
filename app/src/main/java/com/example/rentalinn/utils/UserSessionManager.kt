
package com.example.rentalinn.utils

import android.content.Context
import android.util.Log
import com.example.rentalinn.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserSessionManager private constructor(private val context: Context) {
    private val dataStoreManager = DataStoreManager.getInstance(context)
    private val TAG = "UserSessionManager"

    companion object {
        @Volatile
        private var instance: UserSessionManager? = null

        fun getInstance(context: Context): UserSessionManager {
            return instance ?: synchronized(this) {
                instance ?: UserSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Flow untuk mendapatkan user yang sedang login
    val currentUser: Flow<User?> = dataStoreManager.currentUser

    // Flow untuk mengecek apakah user sudah login
    val isLoggedIn: Flow<Boolean> = dataStoreManager.token.map { token ->
        !token.isNullOrEmpty()
    }

    // Flow untuk mendapatkan informasi user secara terpisah
    val userId: Flow<Int?> = dataStoreManager.userId
    val userName: Flow<String?> = dataStoreManager.userName
    val userEmail: Flow<String?> = dataStoreManager.userEmail
    val userPhone: Flow<String?> = dataStoreManager.userPhone
    val userRole: Flow<String?> = dataStoreManager.userRole
    val userProfilePicture: Flow<String?> = dataStoreManager.userProfilePicture
    val userIsVerified: Flow<Boolean> = dataStoreManager.userIsVerified

    // Suspend functions untuk mendapatkan data user secara langsung
    suspend fun getCurrentUser(): User? {
        return try {
            dataStoreManager.currentUser.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ", e)
            null
        }
    }

    suspend fun getCurrentUserId(): Int? {
        return try {
            dataStoreManager.userId.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID: ", e)
            null
        }
    }

    suspend fun getCurrentUserName(): String? {
        return try {
            dataStoreManager.userName.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user name: ", e)
            null
        }
    }

    suspend fun getCurrentUserEmail(): String? {
        return try {
            dataStoreManager.userEmail.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user email: ", e)
            null
        }
    }

    suspend fun getCurrentUserPhone(): String? {
        return try {
            dataStoreManager.userPhone.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user phone: ", e)
            null
        }
    }

    suspend fun getCurrentUserRole(): String? {
        return try {
            dataStoreManager.userRole.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user role: ", e)
            null
        }
    }

    suspend fun getCurrentUserProfilePicture(): String? {
        return try {
            dataStoreManager.userProfilePicture.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user profile picture: ", e)
            null
        }
    }

    suspend fun isCurrentUserVerified(): Boolean {
        return try {
            dataStoreManager.userIsVerified.first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user verification status: ", e)
            false
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return try {
            val token = dataStoreManager.token.first()
            !token.isNullOrEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status: ", e)
            false
        }
    }

    // Function untuk menyimpan user session setelah login
    suspend fun saveUserSession(token: String, user: User) {
        try {
            dataStoreManager.saveToken(token)
            dataStoreManager.saveUser(user)
            Log.d(TAG, "User session saved successfully for user: ${user.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user session: ", e)
            throw e
        }
    }

    // Function untuk clear user session saat logout
    suspend fun clearUserSession() {
        try {
            dataStoreManager.clearData()
            Log.d(TAG, "User session cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing user session: ", e)
            throw e
        }
    }

    // Function untuk update user data
    suspend fun updateUserData(user: User) {
        try {
            dataStoreManager.saveUser(user)
            Log.d(TAG, "User data updated successfully for user: ${user.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user data: ", e)
            throw e
        }
    }

    // Helper function untuk mendapatkan display name
    suspend fun getDisplayName(): String {
        return getCurrentUserName() ?: "User"
    }

    // Helper function untuk mendapatkan user initials untuk avatar
    suspend fun getUserInitials(): String {
        val name = getCurrentUserName()
        return if (name != null && name.isNotEmpty()) {
            val words = name.split(" ")
            if (words.size >= 2) {
                "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
            } else {
                name.take(2).uppercase()
            }
        } else {
            "U"
        }
    }
}