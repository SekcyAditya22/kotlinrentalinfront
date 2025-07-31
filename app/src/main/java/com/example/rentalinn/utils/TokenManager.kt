package com.example.rentalinn.utils

import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TokenManager(private val context: Context) {
    private val dataStoreManager = DataStoreManager.getInstance(context)
    
    fun getToken(): String? {
        return runBlocking {
            dataStoreManager.token.first()
        }
    }
    
    suspend fun saveToken(token: String) {
        dataStoreManager.saveToken(token)
    }
    
    suspend fun clearToken() {
        dataStoreManager.clearData()
    }
}
