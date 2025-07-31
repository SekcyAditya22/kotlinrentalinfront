package com.example.rentalinn

import android.app.Application
import com.example.rentalinn.utils.DataStoreManager

class RentalinApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize DataStoreManager
        DataStoreManager.getInstance(this)
    }
} 