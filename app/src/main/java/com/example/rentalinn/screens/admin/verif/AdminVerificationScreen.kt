package com.example.rentalinn.screens.admin.verif

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.repository.AdminVerificationRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVerificationScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adminVerificationRepository = remember { AdminVerificationRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // States
    var userDetailsList by remember { mutableStateOf<List<UserDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("all") } // all, pending, verified, incomplete
    var showUserDetailsDialog by remember { mutableStateOf(false) }
    var selectedUserDetails by remember { mutableStateOf<UserDetails?>(null) }
    
    // Load user details
    LaunchedEffect(selectedFilter) {
        isLoading = true
        errorMessage = null
        
        try {
            val result = adminVerificationRepository.getAllUserDetails(
                status = if (selectedFilter == "all") null else selectedFilter,
                page = 1,
                limit = 50
            )
            
            result.fold(
                onSuccess = { response ->
                    userDetailsList = response.data.userDetails
                    Log.d("AdminVerification", "Loaded ${response.data.userDetails.size} user details")
                },
                onFailure = { exception ->
                    errorMessage = exception.message
                    Log.e("AdminVerification", "Failed to load user details", exception)
                }
            )
        } catch (e: Exception) {
            errorMessage = e.message
            Log.e("AdminVerification", "Error loading user details", e)
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("User Verification") },
            actions = {
                IconButton(
                    onClick = {
                        // Refresh data
                        coroutineScope.launch {
                            isLoading = true
                            val result = adminVerificationRepository.getAllUserDetails(
                                status = if (selectedFilter == "all") null else selectedFilter,
                                page = 1,
                                limit = 50
                            )
                            result.fold(
                                onSuccess = { response ->
                                    userDetailsList = response.data.userDetails
                                },
                                onFailure = { exception ->
                                    errorMessage = exception.message
                                }
                            )
                            isLoading = false
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        
        // Filter Chips
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "all" to "All Users",
                "pending" to "Pending",
                "verified" to "Verified", 
                "incomplete" to "Incomplete"
            )
            
            items(filters) { (key, label) ->
                FilterChip(
                    onClick = { selectedFilter = key },
                    label = { Text(label) },
                    selected = selectedFilter == key,
                    leadingIcon = {
                        when (key) {
                            "all" -> Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                            "pending" -> Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                            "verified" -> Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            "incomplete" -> Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading user data...")
                    }
                }
            }
            
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading data",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            userDetailsList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "No data",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No users found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "No users match the selected filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userDetailsList) { userDetails ->
                        UserVerificationCard(
                            userDetails = userDetails,
                            onClick = {
                                selectedUserDetails = userDetails
                                showUserDetailsDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // User Details Dialog
    if (showUserDetailsDialog && selectedUserDetails != null) {
        UserDetailsVerificationDialog(
            userDetails = selectedUserDetails!!,
            adminVerificationRepository = adminVerificationRepository,
            onDismiss = { 
                showUserDetailsDialog = false
                selectedUserDetails = null
            },
            onVerificationComplete = { updatedUserDetails ->
                // Update the list
                userDetailsList = userDetailsList.map { 
                    if (it.id == updatedUserDetails.id) updatedUserDetails else it 
                }
                showUserDetailsDialog = false
                selectedUserDetails = null
                
                Toast.makeText(
                    context,
                    "Verification completed successfully",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onError = { errorMsg ->
                Toast.makeText(
                    context,
                    "Verification failed: $errorMsg",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}

@Composable
fun UserVerificationCard(
    userDetails: UserDetails,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userDetails.user?.name ?: "Unknown User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = userDetails.user?.email ?: "No Email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Overall Status Badge
                val overallStatus = when {
                    userDetails.isKtpVerified && userDetails.isSimVerified -> "verified"
                    userDetails.ktpPhoto != null || userDetails.simPhoto != null -> "pending"
                    else -> "incomplete"
                }

                StatusBadge(status = overallStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Document Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // KTP Status
                DocumentStatusChip(
                    label = "KTP",
                    hasDocument = userDetails.ktpPhoto != null,
                    isVerified = userDetails.isKtpVerified,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // SIM Status
                DocumentStatusChip(
                    label = "SIM",
                    hasDocument = userDetails.simPhoto != null,
                    isVerified = userDetails.isSimVerified,
                    modifier = Modifier.weight(1f)
                )
            }

            // Additional Info
            if (userDetails.verificationNotes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${userDetails.verificationNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, text, icon) = when (status) {
        "verified" -> Triple(
            MaterialTheme.colorScheme.primary,
            "Verified",
            Icons.Default.CheckCircle
        )
        "pending" -> Triple(
            MaterialTheme.colorScheme.tertiary,
            "Pending",
            Icons.Default.Schedule
        )
        else -> Triple(
            MaterialTheme.colorScheme.outline,
            "Incomplete",
            Icons.Default.Warning
        )
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun DocumentStatusChip(
    label: String,
    hasDocument: Boolean,
    isVerified: Boolean,
    modifier: Modifier = Modifier
) {
    val (color, text, icon) = when {
        isVerified -> Triple(
            Color(0xFF4CAF50),
            "Verified",
            Icons.Default.CheckCircle
        )
        hasDocument -> Triple(
            Color(0xFFFF9800),
            "Pending",
            Icons.Default.Schedule
        )
        else -> Triple(
            Color(0xFF757575),
            "Missing",
            Icons.Default.Warning
        )
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $text",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
