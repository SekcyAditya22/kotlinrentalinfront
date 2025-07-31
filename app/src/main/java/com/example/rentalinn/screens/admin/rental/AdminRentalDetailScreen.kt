package com.example.rentalinn.screens.admin.rental

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.rentalinn.model.Rental
import com.example.rentalinn.viewmodel.AdminRentalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRentalDetailScreen(
    navController: NavController,
    rentalId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adminRentalViewModel: AdminRentalViewModel = viewModel(
        factory = AdminRentalViewModel.Factory(context)
    )

    var rental by remember { mutableStateOf<Rental?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showRejectDialog by remember { mutableStateOf(false) }

    val successMessage by adminRentalViewModel.successMessage.collectAsState()

    // Get rental from viewmodel state
    val pendingRentals by adminRentalViewModel.pendingRentals.collectAsState()
    val allRentals by adminRentalViewModel.allRentals.collectAsState()
    val viewModelLoading by adminRentalViewModel.isLoading.collectAsState()
    val viewModelError by adminRentalViewModel.error.collectAsState()

    // Load rental detail
    LaunchedEffect(rentalId) {
        isLoading = true
        error = null
        try {
            // Load both pending and all rentals using the new method
            adminRentalViewModel.loadAllData()
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    // Update loading state based on ViewModel
    LaunchedEffect(viewModelLoading) {
        if (!viewModelLoading) {
            isLoading = false
        }
    }

    // Handle ViewModel errors
    LaunchedEffect(viewModelError) {
        viewModelError?.let {
            error = it
            isLoading = false
        }
    }

    // Find rental when data is loaded
    LaunchedEffect(pendingRentals, allRentals, viewModelLoading) {
        Log.d("AdminRentalDetail", "Searching for rental ID: $rentalId")
        Log.d("AdminRentalDetail", "Pending rentals count: ${pendingRentals.size}")
        Log.d("AdminRentalDetail", "All rentals count: ${allRentals.size}")
        Log.d("AdminRentalDetail", "ViewModel loading: $viewModelLoading")
        Log.d("AdminRentalDetail", "Local loading: $isLoading")

        // Wait for both ViewModel and local loading to finish
        if (!viewModelLoading && !isLoading) {
            // Small delay to ensure state is fully updated
            kotlinx.coroutines.delay(100)

            // Log all rental IDs for debugging
            pendingRentals.forEach { Log.d("AdminRentalDetail", "Pending rental ID: ${it.id}") }
            allRentals.forEach { Log.d("AdminRentalDetail", "All rental ID: ${it.id}") }

            val foundRental = pendingRentals.find { it.id.toString() == rentalId }
                ?: allRentals.find { it.id.toString() == rentalId }

            Log.d("AdminRentalDetail", "Found rental: ${foundRental?.id}")
            rental = foundRental

            if (foundRental == null && (pendingRentals.isNotEmpty() || allRentals.isNotEmpty())) {
                error = "Rental with ID $rentalId not found"
                Log.e("AdminRentalDetail", "Rental not found for ID: $rentalId")
            } else if (foundRental == null && pendingRentals.isEmpty() && allRentals.isEmpty()) {
                error = "No rental data available"
                Log.e("AdminRentalDetail", "No rental data available")
            }
        }
    }

    // Handle success message
    successMessage?.let { message ->
        LaunchedEffect(message) {
            adminRentalViewModel.clearMessages()
            navController.popBackStack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Rental Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        when {
            isLoading || viewModelLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading rental details...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            error != null -> {
                AdminRentalDetailErrorState(
                    error = error ?: "Unknown error",
                    onRetry = {
                        error = null
                        isLoading = true
                        adminRentalViewModel.loadAllData()
                    }
                )
            }
            rental != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AdminRentalDetailContent(
                            rental = rental!!,
                            onApprove = { adminRentalViewModel.approveRental(rental!!.id) },
                            onReject = { showRejectDialog = true },
                            onComplete = { adminRentalViewModel.completeRental(rental!!.id) }
                        )
                    }
                }
            }
            else -> {
                // Fallback case - no rental found and no error
                AdminRentalDetailErrorState(
                    error = "Rental not found or still loading...",
                    onRetry = {
                        error = null
                        isLoading = true
                        adminRentalViewModel.loadAllData()
                    }
                )
            }
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        AdminRentalDetailRejectDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                adminRentalViewModel.rejectRental(rental!!.id, reason)
                showRejectDialog = false
            }
        )
    }
}

@Composable
fun AdminRentalDetailContent(
    rental: Rental,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        AdminRentalStatusCard(rental = rental)
        
        // Customer Information
        AdminCustomerInfoCard(rental = rental)
        
        // Vehicle Information
        AdminVehicleInfoCard(rental = rental)
        
        // Rental Details
        AdminRentalDetailsCard(rental = rental)
        
        // Location Information
        AdminLocationInfoCard(rental = rental)
        
        // Payment Information
        AdminPaymentInfoCard(rental = rental)
        
        // Notes
        if (!rental.notes.isNullOrBlank()) {
            AdminNotesCard(rental = rental)
        }
        
        // Action Buttons
        when {
            rental.status == "confirmed" && rental.adminApprovalStatus == "pending" -> {
                AdminActionButtons(
                    onApprove = onApprove,
                    onReject = onReject
                )
            }
            rental.status == "active" && rental.adminApprovalStatus == "approved" -> {
                AdminCompleteButton(
                    onComplete = onComplete
                )
            }
        }
    }
}

@Composable
fun AdminRentalStatusCard(rental: Rental) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Rental #${rental.id}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = rental.getStatusColor().copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = rental.getStatusDisplayName(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = rental.getStatusColor(),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Created: ${rental.createdAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (rental.adminApprovalStatus == "approved" && rental.approvedAt != null) {
                Text(
                    text = "Approved: ${rental.approvedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (rental.adminApprovalStatus == "rejected" && !rental.rejectionReason.isNullOrBlank()) {
                Text(
                    text = "Rejection Reason: ${rental.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AdminCustomerInfoCard(rental: Rental) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            rental.user?.let { user ->
                DetailRow(label = "Name", value = user.name)
                DetailRow(label = "Email", value = user.email)
                user.phoneNumber?.let { phone ->
                    DetailRow(label = "Phone", value = phone)
                }
                DetailRow(
                    label = "Verified", 
                    value = if (user.isVerified) "Yes" else "No",
                    valueColor = if (user.isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            } ?: run {
                Text(
                    text = "Customer information not available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AdminVehicleInfoCard(rental: Rental) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vehicle Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            rental.vehicle?.let { vehicle ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = vehicle.getMainPhoto(),
                        contentDescription = vehicle.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = vehicle.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${vehicle.brand} ${vehicle.model}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Category: ${vehicle.vehicleCategory}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Capacity: ${vehicle.passengerCapacity} passengers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "Vehicle information not available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AdminRentalDetailsCard(rental: Rental) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rental Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = "Start Date", value = rental.startDate)
            DetailRow(label = "End Date", value = rental.endDate)
            DetailRow(label = "Total Days", value = "${rental.totalDays} days")
            DetailRow(label = "Price per Day", value = "Rp ${rental.pricePerDay.toInt()}")
            DetailRow(
                label = "Total Amount",
                value = rental.getFormattedTotalAmount(),
                valueColor = MaterialTheme.colorScheme.primary,
                valueFontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminLocationInfoCard(rental: Rental) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup Location with clickable maps
            rental.pickupLocation?.let { pickup ->
                ClickableLocationRow(
                    label = "Pickup Location",
                    location = pickup,
                    latitude = rental.pickupLatitude,
                    longitude = rental.pickupLongitude,
                    context = context
                )
            } ?: run {
                DetailRow(label = "Pickup Location", value = "Not specified", valueColor = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Return Location with clickable maps
            rental.returnLocation?.let { returnLoc ->
                ClickableLocationRow(
                    label = "Return Location",
                    location = returnLoc,
                    latitude = rental.returnLatitude,
                    longitude = rental.returnLongitude,
                    context = context
                )
            } ?: run {
                DetailRow(label = "Return Location", value = "Not specified", valueColor = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun ClickableLocationRow(
    label: String,
    location: String,
    latitude: Double?,
    longitude: Double?,
    context: android.content.Context
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    openLocationInMaps(context, location, latitude, longitude)
                }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = location,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Open in Maps",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Open Maps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun openLocationInMaps(
    context: android.content.Context,
    locationName: String,
    latitude: Double?,
    longitude: Double?
) {
    try {
        val intent = if (latitude != null && longitude != null) {
            // Use coordinates if available
            android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($locationName)")
            )
        } else {
            // Use location name for search
            android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(locationName)}")
            )
        }

        intent.setPackage("com.google.android.apps.maps")

        // Check if Google Maps is installed
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web browser if Google Maps is not installed
            val webIntent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode(locationName)}")
            )
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        // Handle error - could show a toast or log
        android.util.Log.e("AdminRentalDetail", "Error opening maps: ${e.message}")
    }
}

@Composable
fun AdminPaymentInfoCard(rental: Rental) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Payment Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            rental.payment?.let { payment ->
                DetailRow(
                    label = "Payment Status",
                    value = payment.getStatusDisplayName(),
                    valueColor = payment.getStatusColor()
                )
                DetailRow(label = "Amount", value = "Rp ${payment.amount.toInt()}")
                payment.paymentMethod?.let { method ->
                    DetailRow(label = "Payment Method", value = method)
                }
                payment.transactionId?.let { txId ->
                    DetailRow(label = "Transaction ID", value = txId)
                }
                payment.paidAt?.let { paidAt ->
                    DetailRow(label = "Paid At", value = paidAt)
                }
            } ?: run {
                Text(
                    text = "Payment information not available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AdminNotesCard(rental: Rental) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = rental.notes ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AdminActionButtons(
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Admin Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject Rental")
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Approve Rental")
                }
            }
        }
    }
}

@Composable
fun AdminCompleteButton(
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rental Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mark this rental as completed to return the vehicle unit to available status.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Selesai",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = valueFontWeight,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun AdminRentalDetailErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error Loading Rental",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRetry) {
                Text("Go Back")
            }
        }
    }
}

@Composable
fun AdminRentalDetailRejectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var rejectionReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reject Rental",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Please provide a reason for rejecting this rental:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    label = { Text("Rejection Reason") },
                    placeholder = { Text("e.g., Vehicle not available, Invalid documents...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rejectionReason) },
                enabled = rejectionReason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
