package com.example.rentalinn.screens.user.payment

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rentalinn.viewmodel.RentalViewModel

@Composable
fun PaymentSuccessScreen(
    rentalId: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rentalViewModel: RentalViewModel = viewModel(
        factory = RentalViewModel.Factory(context)
    )

    val rental by rentalViewModel.rental.collectAsState()

    var countdown by remember { mutableStateOf(8) } // 8 seconds countdown
    var isAutoNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(rentalId) {
        // Initial load
        rentalViewModel.getRentalById(rentalId)

        // Multiple refreshes to ensure we get the latest status
        repeat(5) {
            kotlinx.coroutines.delay(2000)
            rentalViewModel.getRentalById(rentalId)

            // If payment is still pending after some attempts, try auto-update
            if (it == 2) {
                try {
                    rentalViewModel.autoUpdatePaymentStatus(rentalId)
                } catch (e: Exception) {
                    // Ignore auto-update errors
                }
            }
        }
    }

    // Auto-navigation countdown
    LaunchedEffect(Unit) {
        while (countdown > 0 && !isAutoNavigating) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }

        if (countdown == 0 && !isAutoNavigating) {
            isAutoNavigating = true
            navController.navigate("user_transaction") {
                popUpTo("payment_success/$rentalId") { inclusive = true }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Icon - Made smaller
        Card(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF10B981)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Success Title - Reduced size
        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your rental booking has been confirmed.\nWaiting for admin approval.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Payment Status Indicator - Made more compact
        rental?.payment?.let { payment ->
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (payment.isPaid())
                        Color(0xFF10B981).copy(alpha = 0.1f)
                    else
                        Color(0xFFF59E0B).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (payment.isPaid()) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (payment.isPaid()) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Status: ${payment.getStatusDisplayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (payment.isPaid()) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rental Info Card - Made more compact
        rental?.let { rentalData ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Booking Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ID:", style = MaterialTheme.typography.bodySmall)
                        Text("#${rentalData.id}", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vehicle:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = rentalData.vehicle?.title ?: "Unknown",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Amount:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = rentalData.getFormattedTotalAmount(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Use weight to push buttons to bottom
        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons - More compact layout
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primary Action - View Transaksi (Most prominent)
            Button(
                onClick = {
                    isAutoNavigating = true
                    navController.navigate("user_transaction") {
                        popUpTo("payment_success/$rentalId") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "View Transaksi",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (countdown > 0 && !isAutoNavigating) {
                        Text(
                            text = "Auto-redirect in ${countdown}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Secondary Actions Row - More compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        isAutoNavigating = true
                        navController.navigate("user_rental") {
                            popUpTo("payment_success/$rentalId") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Book Again",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(
                    onClick = {
                        isAutoNavigating = true
                        navController.navigate("user_home") {
                            popUpTo("payment_success/$rentalId") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Home",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Quick Info - More compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Booking confirmed • Waiting for admin approval",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PaymentPendingScreen(
    rentalId: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pending Icon - Made smaller
        Card(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF59E0B)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Payment Pending",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your payment is being processed.\nPlease wait for confirmation.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("user_transaction") {
                    popUpTo("payment_pending/$rentalId") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Transaksi")
        }
    }
}

@Composable
fun PaymentFailedScreen(
    rentalId: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Failed Icon - Made smaller
        Card(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEF4444)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Payment Failed",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your payment could not be processed.\nPlease try again.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    navController.navigate("payment/$rentalId") {
                        popUpTo("payment_failed/$rentalId") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        navController.navigate("user_transaction") {
                            popUpTo("payment_failed/$rentalId") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Transaksi", style = MaterialTheme.typography.bodySmall)
                }

                TextButton(
                    onClick = {
                        navController.navigate("user_rental") {
                            popUpTo("payment_failed/$rentalId") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to Rentals", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
