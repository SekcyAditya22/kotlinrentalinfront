package com.example.rentalinn.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rentalinn.model.User
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "rentalinkedua_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class DataStoreManager private constructor(private val context: Context) {
    private val TAG = "DataStoreManager"
    private val gson = Gson()

    companion object {
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_PROFILE_PICTURE_KEY = stringPreferencesKey("user_profile_picture")
        private val USER_IS_VERIFIED_KEY = booleanPreferencesKey("user_is_verified")
        private val USER_DATA_KEY = stringPreferencesKey("user_data")

        @Volatile
        private var instance: DataStoreManager? = null

        fun getInstance(context: Context): DataStoreManager {
            return instance ?: synchronized(this) {
                instance ?: DataStoreManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            Log.e(TAG, "Error reading preferences: ", exception)
        }
        .map { preferences ->
            val hasSeenOnboarding = preferences[HAS_SEEN_ONBOARDING] ?: false
            Log.d(TAG, "Reading hasSeenOnboarding: $hasSeenOnboarding")
            hasSeenOnboarding
        }

    suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        try {
            context.dataStore.edit { preferences ->
                preferences[HAS_SEEN_ONBOARDING] = hasSeen
                Log.d(TAG, "Setting hasSeenOnboarding to: $hasSeen")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving hasSeenOnboarding: ", e)
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ROLE_KEY] = role
        }
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_NAME_KEY] = user.name
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_ROLE_KEY] = user.role
            preferences[USER_PHONE_KEY] = user.phoneNumber ?: ""
            preferences[USER_PROFILE_PICTURE_KEY] = user.profilePicture ?: ""
            preferences[USER_IS_VERIFIED_KEY] = user.isVerified
            preferences[USER_DATA_KEY] = gson.toJson(user)
        }
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    val currentUser: Flow<User?> = context.dataStore.data.map { preferences ->
        val userDataJson = preferences[USER_DATA_KEY]
        if (userDataJson != null) {
            try {
                gson.fromJson(userDataJson, User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing user data: ", e)
                null
            }
        } else {
            null
        }
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    val userPhone: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PHONE_KEY]
    }

    val userProfilePicture: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PROFILE_PICTURE_KEY]
    }

    val userIsVerified: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_IS_VERIFIED_KEY] ?: false
    }

    suspend fun clearData() {
        try {
            context.dataStore.edit { preferences ->
                preferences.clear()
                // Explicitly clear specific keys to ensure they are removed
                preferences.remove(TOKEN_KEY)
                preferences.remove(USER_ROLE_KEY)
                preferences.remove(USER_ID_KEY)
                preferences.remove(USER_NAME_KEY)
                preferences.remove(USER_EMAIL_KEY)
                preferences.remove(USER_PHONE_KEY)
                preferences.remove(USER_PROFILE_PICTURE_KEY)
                preferences.remove(USER_IS_VERIFIED_KEY)
                preferences.remove(USER_DATA_KEY)
                preferences.remove(HAS_SEEN_ONBOARDING)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data: ", e)
            throw e
        }
    }
} 