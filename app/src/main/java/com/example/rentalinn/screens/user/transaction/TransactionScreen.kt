package com.example.rentalinn.screens.user.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rentalinn.model.Rental
import com.example.rentalinn.viewmodel.RentalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rentalViewModel: RentalViewModel = viewModel(
        factory = RentalViewModel.Factory(context)
    )

    val rentals by rentalViewModel.rentals.collectAsState()
    val isLoading by rentalViewModel.isLoading.collectAsState()
    val error by rentalViewModel.error.collectAsState()
    val successMessage by rentalViewModel.successMessage.collectAsState()

    var selectedStatus by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load user rentals
    LaunchedEffect(Unit) {
        rentalViewModel.getUserRentals()
    }

    // Handle success messages
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            if (message.contains("Payment retry created successfully")) {
                // Navigate to payment screen after retry success
                snackbarHostState.showSnackbar("Payment retry created. Redirecting to payment...")
                kotlinx.coroutines.delay(1000)
                // Find the rental that was retried and navigate to payment
                val retryRental = rentals.find { it.payment?.paymentStatus == "pending" }
                retryRental?.let { rental ->
                    navController.navigate("payment/${rental.id}")
                }
            } else {
                snackbarHostState.showSnackbar(message)
            }
            rentalViewModel.clearMessages()
        }
    }

    // Handle error messages
    LaunchedEffect(error) {
        error?.let { message ->
            snackbarHostState.showSnackbar(message)
            rentalViewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        // Status Filter
        val statusOptions = listOf("All", "pending", "confirmed", "approved", "active", "completed", "cancelled", "rejected")
        ScrollableTabRow(
            selectedTabIndex = statusOptions.indexOf(selectedStatus ?: "All"),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            statusOptions.forEach { status ->
                Tab(
                    selected = if (status == "All") selectedStatus == null else selectedStatus == status,
                    onClick = { selectedStatus = if (status == "All") null else status }
                ) {
                    Text(
                        text = status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        // Transaction List
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading bookings",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { rentalViewModel.getUserRentals() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            rentals.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bookings yet",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Start by booking your first vehicle!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                val filteredRentals = rentals.filter { rental ->
                    selectedStatus == null || rental.status == selectedStatus
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredRentals) { rental ->
                        RentalTransactionCard(
                            rental = rental,
                            navController = navController,
                            rentalViewModel = rentalViewModel
                        )
                    }
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalTransactionCard(
    rental: Rental,
    navController: NavController,
    rentalViewModel: RentalViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rental.vehicle?.title ?: "Unknown Vehicle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                RentalStatusChip(status = rental.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = rental.startDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = "End Date",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = rental.endDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = rental.getFormattedTotalAmount(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Location Information
            if (!rental.pickupLocation.isNullOrBlank() || !rental.returnLocation.isNullOrBlank()) {
                LocationInfoSection(rental = rental)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action buttons based on status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (rental.status) {
                    "pending" -> {
                        // Check if payment exists and is successful
                        if (rental.payment?.paymentStatus == "settlement" ||
                            rental.payment?.paymentStatus == "capture") {
                            // Payment successful but rental still pending - show waiting message
                            Text(
                                text = "✅ Payment successful - Processing your booking...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF10B981),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Payment not completed - show pay now button
                            OutlinedButton(
                                onClick = {
                                    // Cancel booking
                                    rentalViewModel.cancelRental(rental.id)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    // Check if payment exists and needs retry
                                    if (rental.payment != null &&
                                        (rental.payment.paymentStatus == "pending" ||
                                         rental.payment.paymentStatus == "failed" ||
                                         rental.payment.paymentStatus == "expire")) {
                                        // Use retry payment for existing payment
                                        rentalViewModel.retryPayment(rental.id)
                                    } else {
                                        // Navigate to payment screen for new payment
                                        navController.navigate("payment/${rental.id}")
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (rental.payment != null &&
                                        (rental.payment.paymentStatus == "pending" ||
                                         rental.payment.paymentStatus == "failed" ||
                                         rental.payment.paymentStatus == "expire"))
                                        "Bayar"
                                    else
                                        "Pay Now"
                                )
                            }
                        }
                    }
                    "confirmed" -> {
                        Text(
                            text = "✅ Payment confirmed - Waiting for admin approval",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "approved", "active" -> {
                        Button(
                            onClick = { /* TODO: Show QR Code */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Show QR Code")
                        }
                    }
                    "completed" -> {
                        OutlinedButton(
                            onClick = { /* TODO: Write review */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Oke")
                        }
                        Button(
                            onClick = { /* TODO: Rent again */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Terimakasih")
                        }
                    }
                    else -> { /* No actions for other statuses */ }
                }
            }
        }
    }
}

@Composable
fun RentalStatusChip(status: String) {
    val (displayName, color) = when (status) {
        "pending" -> "Pending" to Color(0xFFFFA000)
        "confirmed" -> "Confirmed" to Color(0xFF3B82F6)
        "approved" -> "Approved" to Color(0xFF10B981)
        "active" -> "Active" to Color(0xFF4CAF50)
        "completed" -> "Completed" to Color(0xFF2196F3)
        "cancelled" -> "Cancelled" to Color(0xFFF44336)
        "rejected" -> "Rejected" to Color(0xFFEF4444)
        else -> status.replaceFirstChar { it.uppercase() } to Color(0xFF9E9E9E)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = displayName,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LocationInfoSection(rental: Rental) {
    val context = LocalContext.current

    Column {
        Text(
            text = "Locations",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pickup Location
        if (!rental.pickupLocation.isNullOrBlank()) {
            LocationRow(
                label = "Pickup",
                address = rental.pickupLocation,
                latitude = rental.pickupLatitude,
                longitude = rental.pickupLongitude,
                context = context
            )
        }

        // Return Location
        if (!rental.returnLocation.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            LocationRow(
                label = "Return",
                address = rental.returnLocation,
                latitude = rental.returnLatitude,
                longitude = rental.returnLongitude,
                context = context
            )
        }
    }
}

@Composable
fun LocationRow(
    label: String,
    address: String,
    latitude: Double?,
    longitude: Double?,
    context: android.content.Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Show coordinates if available
            if (latitude != null && longitude != null) {
                Text(
                    text = "Lat: ${String.format("%.4f", latitude)}, Lng: ${String.format("%.4f", longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Maps button if coordinates available
        if (latitude != null && longitude != null) {
            IconButton(
                onClick = {
                    // Open maps with coordinates
                    val uri = "geo:$latitude,$longitude?q=$latitude,$longitude($label Location)"
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                    intent.setPackage("com.google.android.apps.maps")

                    // Fallback to any maps app if Google Maps not available
                    if (intent.resolveActivity(context.packageManager) == null) {
                        intent.setPackage(null)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback to browser with OpenStreetMap
                        val browserUri = "https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude&zoom=15"
                        val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(browserUri))
                        context.startActivity(browserIntent)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Open in Maps",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

