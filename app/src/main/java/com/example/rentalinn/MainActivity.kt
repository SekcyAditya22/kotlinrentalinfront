package com.example.rentalinn

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.rentalinn.navigation.AppNavGraph
import com.example.rentalinn.navigation.Screen
import com.example.rentalinn.ui.theme.RentalinKeduaTheme
import com.example.rentalinn.utils.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager.getInstance(applicationContext)

        setContent {
            MainScreen()
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    
    RentalinKeduaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavGraph(
                navController = navController,
                startDestination = Screen.Splash.route
            )
        }
    }
}
