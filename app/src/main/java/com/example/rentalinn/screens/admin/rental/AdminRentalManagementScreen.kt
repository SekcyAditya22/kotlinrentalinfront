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
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.rentalinn.model.Rental
import com.example.rentalinn.viewmodel.AdminRentalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRentalManagementScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adminRentalViewModel: AdminRentalViewModel = viewModel(
        factory = AdminRentalViewModel.Factory(context)
    )

    val pendingRentals by adminRentalViewModel.pendingRentals.collectAsState()
    val allRentals by adminRentalViewModel.allRentals.collectAsState()
    val isLoading by adminRentalViewModel.isLoading.collectAsState()
    val error by adminRentalViewModel.error.collectAsState()
    val successMessage by adminRentalViewModel.successMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Approval", "All Rentals")

    // Load data
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> adminRentalViewModel.getPendingRentals()
            1 -> adminRentalViewModel.getAllRentals()
        }
    }

    // Show success message
    successMessage?.let { message ->
        LaunchedEffect(message) {
            // Refresh data after action
            when (selectedTab) {
                0 -> adminRentalViewModel.getPendingRentals()
                1 -> adminRentalViewModel.getAllRentals()
            }
            adminRentalViewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        AdminRentalHeader()

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        // Content
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
                AdminRentalErrorState(
                    error = error ?: "Unknown error",
                    onRetry = {
                        when (selectedTab) {
                            0 -> adminRentalViewModel.getPendingRentals()
                            1 -> adminRentalViewModel.getAllRentals()
                        }
                    }
                )
            }
            else -> {
                val rentals = when (selectedTab) {
                    0 -> pendingRentals
                    1 -> allRentals
                    else -> emptyList()
                }

                if (rentals.isEmpty()) {
                    AdminRentalEmptyState(
                        message = when (selectedTab) {
                            0 -> "No pending rentals"
                            1 -> "No rentals found"
                            else -> "No data"
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rentals) { rental ->
                            AdminRentalCard(
                                rental = rental,
                                onApprove = { adminRentalViewModel.approveRental(rental.id) },
                                onReject = { reason -> adminRentalViewModel.rejectRental(rental.id, reason) },
                                onViewDetail = { 
                                    navController.navigate("admin_rental_detail/${rental.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRentalHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Rental Management",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Approve and manage rental bookings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AdminRentalCard(
    rental: Rental,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onViewDetail: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rental #${rental.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = rental.getStatusColor().copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = rental.getStatusDisplayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = rental.getStatusColor(),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle and User Info
            rental.vehicle?.let { vehicle ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = vehicle.getMainPhoto(),
                        contentDescription = vehicle.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = vehicle.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${vehicle.brand} ${vehicle.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Customer: ${rental.user?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rental Details 
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Period:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${rental.startDate} to ${rental.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Amount:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = rental.getFormattedTotalAmount(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action Buttons
            if (rental.status == "confirmed" && rental.adminApprovalStatus == "pending") {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                    
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                }
            }

            // View Detail Button
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onViewDetail,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("View Details")
            }
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        AdminRejectDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                onReject(reason)
                showRejectDialog = false
            }
        )
    }
}

@Composable
fun AdminRejectDialog(
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

@Composable
fun AdminRentalErrorState(
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
                text = "Error Loading Rentals",
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
                Text("Retry")
            }
        }
    }
}

@Composable
fun AdminRentalEmptyState(
    message: String
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
                Icons.Default.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "All caught up!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
