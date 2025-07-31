package com.example.rentalinn.repository

import android.content.Context
import com.example.rentalinn.model.AdminStats
import com.example.rentalinn.model.Rental
import com.example.rentalinn.network.RetrofitClient
import com.example.rentalinn.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminStatsRepository(private val context: Context) {
    private val apiService = RetrofitClient.getInstance(context).apiService
    private val tokenManager = TokenManager(context)

    suspend fun getAdminStats(): AdminStats = withContext(Dispatchers.IO) {
        val token = tokenManager.getToken()
        val response = apiService.getAdminRentalStats()
        
        if (response.isSuccessful && response.body() != null) {
            response.body()!!.data
        } else {
            throw Exception(response.errorBody()?.string() ?: "Failed to get admin stats")
        }
    }

    suspend fun getRecentRentals(limit: Int = 5): List<Rental> = withContext(Dispatchers.IO) {
        val token = tokenManager.getToken()
        val response = apiService.getAllRentals()
        
        if (response.isSuccessful && response.body() != null) {
            // Take only the most recent rentals
            response.body()!!.data.take(limit)
        } else {
            throw Exception(response.errorBody()?.string() ?: "Failed to get recent rentals")
        }
    }
}
