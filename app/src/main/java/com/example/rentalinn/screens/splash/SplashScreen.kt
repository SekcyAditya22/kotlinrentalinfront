package com.example.rentalinn.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rentalinn.R
import com.example.rentalinn.utils.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToUserDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "Splash Alpha Animation"
    )

    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager.getInstance(context) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)

        // Check login state
        val token = dataStoreManager.token.first()
        val userRole = dataStoreManager.userRole.first()

        when {
            token == null -> {
                val hasSeenOnboarding = dataStoreManager.hasSeenOnboarding.first()
                if (hasSeenOnboarding) {
                    onNavigateToLogin()
                } else {
                    onNavigateToOnboarding()
                }
            }
            userRole == "admin" -> onNavigateToAdminDashboard()
            userRole == "user" -> onNavigateToUserDashboard()
            else -> onNavigateToLogin()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logonjay),
                contentDescription = "Rentalin Logo",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(alphaAnim.value)
            )
        }
    }
} 