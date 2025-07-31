package com.example.rentalinn.screens.user.rental

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log
import coil.compose.AsyncImage
import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.model.VehicleUnit
import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.viewmodel.VehicleViewModel
import com.example.rentalinn.viewmodel.RentalViewModel
import com.example.rentalinn.repository.UserDetailsRepository
import com.example.rentalinn.utils.UserSessionManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalBookingScreen(
    vehicleId: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicleViewModel: VehicleViewModel = viewModel(
        factory = VehicleViewModel.Factory(context)
    )
    val rentalViewModel: RentalViewModel = viewModel(
        factory = RentalViewModel.Factory(context)
    )

    // User session and details
    val userSessionManager = remember { UserSessionManager.getInstance(context) }
    val currentUser by userSessionManager.currentUser.collectAsState(initial = null)
    val userDetailsRepository = remember { UserDetailsRepository.getInstance(context) }
    var userDetails by remember { mutableStateOf<UserDetails?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    val vehicle by vehicleViewModel.vehicle.collectAsState()
    val isLoading by vehicleViewModel.isLoading.collectAsState()
    val error by vehicleViewModel.error.collectAsState()
    
    val isCreatingRental by rentalViewModel.isLoading.collectAsState()
    val rentalError by rentalViewModel.error.collectAsState()
    val createdRental by rentalViewModel.createdRental.collectAsState()

    // Booking form state
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var selectedUnitId by rememberSaveable { mutableStateOf<Int?>(null) }
    var pickupLocation by rememberSaveable { mutableStateOf("") }
    var pickupLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var pickupLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var returnLocation by rememberSaveable { mutableStateOf("") }
    var returnLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var returnLongitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }
    var totalDays by remember { mutableStateOf(0) }
    var totalAmount by remember { mutableStateOf(0.0) }

    // Date picker states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Load vehicle details
    LaunchedEffect(vehicleId) {
        Log.d("RentalBookingScreen", "Loading vehicle details for vehicleId: $vehicleId")
        vehicleViewModel.getVehicleById(vehicleId)
    }

    // Load user details for verification check
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            try {
                val result = userDetailsRepository.getUserDetails()
                result.fold(
                    onSuccess = { details ->
                        userDetails = details
                        Log.d("RentalBookingScreen", "User details loaded: ${details?.getVerificationStatus() ?: "unknown"}")
                    },
                    onFailure = { exception ->
                        Log.e("RentalBookingScreen", "Failed to load user details", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e("RentalBookingScreen", "Error loading user details", e)
            }
        }
    }

    // Calculate total when dates change
    LaunchedEffect(startDate, endDate, vehicle) {
        if (startDate.isNotEmpty() && endDate.isNotEmpty() && vehicle != null) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val start = sdf.parse(startDate)
                val end = sdf.parse(endDate)
                
                if (start != null && end != null && end.after(start)) {
                    val diffInMillis = end.time - start.time
                    val days = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                    totalDays = days
                    totalAmount = days * vehicle!!.pricePerDay
                }
            } catch (e: Exception) {
                totalDays = 0
                totalAmount = 0.0
            }
        }
    }

    // Handle location picker results
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    // Check for pickup location result
    val pickupResult = savedStateHandle?.get<Map<String, Any>>("pickup_location_result")
    LaunchedEffect(pickupResult) {
        pickupResult?.let { result ->
            Log.d("LocationPicker", "Processing pickup result: $result")
            pickupLocation = result["address"] as? String ?: ""
            pickupLatitude = result["latitude"] as? Double
            pickupLongitude = result["longitude"] as? Double
            Log.d("LocationPicker", "Pickup location set to: $pickupLocation")
            savedStateHandle.remove<Map<String, Any>>("pickup_location_result")
        }
    }

    // Check for return location result
    val returnResult = savedStateHandle?.get<Map<String, Any>>("return_location_result")
    LaunchedEffect(returnResult) {
        returnResult?.let { result ->
            Log.d("LocationPicker", "Processing return result: $result")
            returnLocation = result["address"] as? String ?: ""
            returnLatitude = result["latitude"] as? Double
            returnLongitude = result["longitude"] as? Double
            Log.d("LocationPicker", "Return location set to: $returnLocation")
            Log.d("LocationPicker", "Current pickup location: $pickupLocation")
            savedStateHandle.remove<Map<String, Any>>("return_location_result")
        }
    }

    // Handle rental creation success
    LaunchedEffect(createdRental) {
        createdRental?.let { rental ->
            // Navigate to payment screen
            navController.navigate("payment/${rental.rental.id}") {
                popUpTo("rental_booking/$vehicleId") { inclusive = true }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        RentalBookingHeader(
            onBackClick = { navController.popBackStack() }
        )

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
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { vehicleViewModel.getVehicleById(vehicleId) }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            vehicle != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Vehicle Summary Card
                        VehicleSummaryCard(vehicle = vehicle!!)
                    }

                    item {
                        // Unit Selection
                        UnitSelectionSection(
                            vehicle = vehicle!!,
                            selectedUnitId = selectedUnitId,
                            onUnitSelected = { selectedUnitId = it }
                        )
                    }

                    item {
                        // Date Selection
                        DateSelectionSection(
                            startDate = startDate,
                            endDate = endDate,
                            onStartDateClick = { showStartDatePicker = true },
                            onEndDateClick = { showEndDatePicker = true }
                        )
                    }
                    
                    item {
                        // Location Selection
                        LocationSelectionSection(
                            pickupLocation = pickupLocation,
                            returnLocation = returnLocation,
                            onPickupLocationChange = { pickupLocation = it },
                            onReturnLocationChange = { returnLocation = it },
                            onPickupMapClick = {
                                navController.navigate("location_picker/pickup")
                            },
                            onReturnMapClick = {
                                navController.navigate("location_picker/return")
                            }
                        )
                    }
                    
                    item {
                        // Notes Section
                        NotesSection(
                            notes = notes,
                            onNotesChange = { notes = it }
                        )
                    }
                    
                    item {
                        // Price Summary
                        PriceSummaryCard(
                            vehicle = vehicle!!,
                            totalDays = totalDays,
                            totalAmount = totalAmount
                        )
                    }
                    
                    item {
                        // Book Now Button
                        BookNowButton(
                            enabled = startDate.isNotEmpty() && 
                                     endDate.isNotEmpty() && 
                                     totalDays > 0 && 
                                     !isCreatingRental,
                            isLoading = isCreatingRental,
                            onClick = {
                                // Check user verification status before allowing rental
                                val user = currentUser
                                val details = userDetails

                                when {
                                    user == null -> {
                                        Log.e("RentalBooking", "User not logged in")
                                        return@BookNowButton
                                    }
                                    details == null -> {
                                        // User has no details record - needs to complete profile
                                        showVerificationDialog = true
                                    }
                                    !details.isDocumentsComplete() -> {
                                        // Documents incomplete
                                        showVerificationDialog = true
                                    }
                                    !user.isVerified || !details.isDocumentsVerified() -> {
                                        // Documents not verified
                                        showVerificationDialog = true
                                    }
                                    else -> {
                                        // All checks passed, proceed with rental
                                        rentalViewModel.createRental(
                                            vehicleId = vehicleId,
                                            unitId = selectedUnitId,
                                            startDate = startDate,
                                            endDate = endDate,
                                            pickupLocation = pickupLocation.ifEmpty { null },
                                            pickupLatitude = pickupLatitude,
                                            pickupLongitude = pickupLongitude,
                                            returnLocation = returnLocation.ifEmpty { null },
                                            returnLatitude = returnLatitude,
                                            returnLongitude = returnLongitude,
                                            notes = notes.ifEmpty { null }
                                        )
                                    }
                                }
                            }
                        )
                    }
                    
                    // Error message
                    rentalError?.let { error ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            title = "Select Start Date",
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            title = "Select End Date",
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            minDate = startDate
        )
    }

    // Verification Dialog
    if (showVerificationDialog) {
        VerificationRequiredDialog(
            userDetails = userDetails,
            currentUser = currentUser,
            onDismiss = { showVerificationDialog = false },
            onCompleteProfile = {
                showVerificationDialog = false
                navController.navigate("user_profile") {
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
fun RentalBookingHeader(
    onBackClick: () -> Unit
) {
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
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Book Vehicle",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun VehicleSummaryCard(vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Vehicle Image
            AsyncImage(
                model = vehicle.getMainPhoto(),
                contentDescription = vehicle.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Vehicle Info
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = vehicle.getFormattedPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DateSelectionSection(
    startDate: String,
    endDate: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rental Period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Date
                DateSelectionCard(
                    modifier = Modifier.weight(1f),
                    title = "Start Date",
                    date = startDate,
                    onClick = onStartDateClick
                )

                // End Date
                DateSelectionCard(
                    modifier = Modifier.weight(1f),
                    title = "End Date",
                    date = endDate,
                    onClick = onEndDateClick
                )
            }
        }
    }
}

@Composable
fun DateSelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    date: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (date.isNotEmpty()) date else "Select",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (date.isNotEmpty())
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun LocationSelectionSection(
    pickupLocation: String,
    returnLocation: String,
    onPickupLocationChange: (String) -> Unit,
    onReturnLocationChange: (String) -> Unit,
    onPickupMapClick: () -> Unit,
    onReturnMapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pickup & Return Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = onPickupLocationChange,
                    label = { Text("Pickup Location") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedButton(
                    onClick = onPickupMapClick,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Pilih di Maps",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = returnLocation,
                    onValueChange = onReturnLocationChange,
                    label = { Text("Return Location (Optional)") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedButton(
                    onClick = onReturnMapClick,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = "Pilih di Maps",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Additional Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Special requests or notes (Optional)") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

@Composable
fun PriceSummaryCard(
    vehicle: Vehicle,
    totalDays: Int,
    totalAmount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Price Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Price per day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = vehicle.getFormattedPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$totalDays days",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Rp ${totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun BookNowButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Book Now & Pay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    title: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    minDate: String? = null
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = sdf.format(Date(millis))
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

@Composable
fun UnitSelectionSection(
    vehicle: Vehicle,
    selectedUnitId: Int?,
    onUnitSelected: (Int?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Select Vehicle Unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose a specific vehicle unit for your rental",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            val availableUnits = vehicle.getAvailableUnitsList()

            if (availableUnits.isEmpty()) {
                // No units available - show message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No Units Available",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "All units for this vehicle are currently rented or under maintenance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Show available units
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option for "Any Available Unit"
                    UnitSelectionCard(
                        unit = null,
                        isSelected = selectedUnitId == null,
                        onSelected = { onUnitSelected(null) }
                    )

                    availableUnits.forEach { unit ->
                        UnitSelectionCard(
                            unit = unit,
                            isSelected = selectedUnitId == unit.id,
                            onSelected = { onUnitSelected(unit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitSelectionCard(
    unit: VehicleUnit?,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                if (unit == null) {
                    Text(
                        text = "Any Available Unit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "System will automatically assign an available unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = unit.getFormattedPlateNumber(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(unit.getStatusColor())),
                                    CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = unit.getStatusDisplayName(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!unit.currentLocation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = unit.currentLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Show notes if available
                    if (!unit.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Notes,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = unit.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificationRequiredDialog(
    userDetails: UserDetails?,
    currentUser: com.example.rentalinn.model.User?,
    onDismiss: () -> Unit,
    onCompleteProfile: () -> Unit
) {
    val (title, message, actionText) = when {
        userDetails == null -> Triple(
            "Profile Incomplete",
            "You need to complete your profile before renting a vehicle. Please add your personal information and upload required documents.",
            "Complete Profile"
        )
        !userDetails.isDocumentsComplete() -> Triple(
            "Documents Required",
            "Please upload your KTP and SIM documents to verify your identity before renting a vehicle.",
            "Upload Documents"
        )
        currentUser?.isVerified != true || !userDetails.isDocumentsVerified() -> Triple(
            "Verification Pending",
            "Your documents are being reviewed by our admin team. You can rent vehicles once your account is verified.",
            "View Profile"
        )
        else -> Triple(
            "Verification Required",
            "Please complete your profile verification to rent vehicles.",
            "Complete Profile"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show verification status if user has details
                userDetails?.let { details ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (details.getVerificationStatus()) {
                                "verified" -> MaterialTheme.colorScheme.primaryContainer
                                "pending" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                when (details.getVerificationStatus()) {
                                    "verified" -> Icons.Default.CheckCircle
                                    "pending" -> Icons.Default.Schedule
                                    else -> Icons.Default.Warning
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (details.getVerificationStatus()) {
                                    "verified" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "pending" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                            Text(
                                text = details.getVerificationStatusDisplay(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = when (details.getVerificationStatus()) {
                                    "verified" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "pending" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCompleteProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
