package com.example.rentalinn.screens.admin.vehicle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.rentalinn.ui.components.ErrorSnackbar
import com.example.rentalinn.ui.components.SuccessSnackbar
import com.example.rentalinn.viewmodel.VehicleViewModel
import java.io.File
import android.net.Uri
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

data class Vehicle(
    val id: String,
    val name: String,
    val type: String,
    val brand: String,
    val model: String,
    val year: String,
    val licensePlate: String,
    val price: Double,
    val status: String,
    val imageUrl: String? = null
)

// Data class for vehicle unit input
data class VehicleUnitInput(
    var plateNumber: String = "",
    var currentLocation: String = "",
    var notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    navController: NavController = rememberNavController(),
    viewModel: VehicleViewModel = viewModel(factory = VehicleViewModel.Factory(LocalContext.current))
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Form state
    var title by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var vehicleCategory by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var transmission by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var passengerCapacity by remember { mutableStateOf("") }
    var features by remember { mutableStateOf("") }

    // Vehicle units state
    var vehicleUnits by remember { mutableStateOf(listOf(VehicleUnitInput())) }
    
    // Image picker
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
        }
    }
    
    // Handle messages
    LaunchedEffect(error, successMessage) {
        if (error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error ?: "An error occurred",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
            }
        } else if (successMessage != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = successMessage ?: "Vehicle added successfully",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
                navController.popBackStack()
            }
        }
    }

    // Function to validate form
    fun validateForm(): Boolean {
        val hasValidVehicleInfo = title.isNotBlank() &&
                brand.isNotBlank() &&
                model.isNotBlank() &&
                vehicleCategory.isNotBlank() &&
                year.isNotBlank() && year.toIntOrNull() != null &&
                pricePerDay.isNotBlank() && pricePerDay.toDoubleOrNull() != null &&
                transmission.isNotBlank() &&
                fuelType.isNotBlank() &&
                passengerCapacity.isNotBlank() && passengerCapacity.toIntOrNull() != null

        val hasValidUnits = vehicleUnits.isNotEmpty() &&
                vehicleUnits.all { it.plateNumber.isNotBlank() }

        return hasValidVehicleInfo && hasValidUnits
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Vehicle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic information
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Vehicle Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = brand,
                            onValueChange = { brand = it },
                            label = { Text("Brand") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("Model") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = vehicleCategory,
                            onValueChange = { vehicleCategory = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = year,
                            onValueChange = { year = it },
                            label = { Text("Year") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // Vehicle Units Section
                    VehicleUnitsSection(
                        units = vehicleUnits,
                        onUnitsChange = { vehicleUnits = it }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = pricePerDay,
                            onValueChange = { pricePerDay = it },
                            label = { Text("Price per Day") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        // Unit count is now determined by the number of vehicle units added
                        Text(
                            text = "Units: ${vehicleUnits.size}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Additional details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = transmission,
                            onValueChange = { transmission = it },
                            label = { Text("Transmission") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fuelType,
                            onValueChange = { fuelType = it },
                            label = { Text("Fuel Type") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = passengerCapacity,
                        onValueChange = { passengerCapacity = it },
                        label = { Text("Passenger Capacity") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = features,
                        onValueChange = { features = it },
                        label = { Text("Features (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        singleLine = false
                    )

                    // Image upload
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload Vehicle Images")
                    }

                    // Display selected images
                    if (selectedImageUris.isNotEmpty()) {
                        Text(
                            text = "${selectedImageUris.size} images selected",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Submit button
                    Button(
                        onClick = {
                            if (validateForm()) {
                                // Convert URIs to files
                                val photoFiles = selectedImageUris.mapNotNull { uri ->
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        val file = File(context.cacheDir, "vehicle_image_${System.currentTimeMillis()}.jpg")
                                        inputStream?.use { input ->
                                            file.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                        file
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                viewModel.createVehicleWithUnits(
                                    title = title,
                                    brand = brand,
                                    model = model,
                                    vehicleCategory = vehicleCategory,
                                    year = year.toIntOrNull() ?: 0,
                                    pricePerDay = pricePerDay.toDoubleOrNull() ?: 0.0,
                                    description = description.takeIf { it.isNotBlank() },
                                    transmission = transmission,
                                    fuelType = fuelType,
                                    passengerCapacity = passengerCapacity.toIntOrNull() ?: 0,
                                    features = features.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                    photos = photoFiles.takeIf { it.isNotEmpty() },
                                    units = vehicleUnits
                                )
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Please fill all required fields",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Vehicle")
                    }
                }
            }
            
            // Error and success messages
            error?.let {
                ErrorSnackbar(
                    message = it,
                    onDismiss = { viewModel.clearMessages() }
                )
            }
            
            successMessage?.let {
                SuccessSnackbar(
                    message = it,
                    onDismiss = { viewModel.clearMessages() }
                )
            }
        }
    }
}

@Composable
fun VehicleListItem(vehicle: Vehicle) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(vehicle.name, style = MaterialTheme.typography.bodyLarge)
            Text("${vehicle.brand} ${vehicle.model} ${vehicle.year}", style = MaterialTheme.typography.bodySmall)
        }
        Text(vehicle.type, modifier = Modifier.weight(0.5f))
        Text(vehicle.licensePlate, modifier = Modifier.weight(0.5f))
        Text("Rp ${vehicle.price}/day", modifier = Modifier.weight(0.5f))
        
        // Status Chip
        Surface(
            color = when (vehicle.status) {
                "Available" -> MaterialTheme.colorScheme.primary
                "Rented" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.secondary
            }.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(0.5f)
        ) {
            Text(
                text = vehicle.status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = when (vehicle.status) {
                    "Available" -> MaterialTheme.colorScheme.primary
                    "Rented" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
        }

        // Actions
        Box(modifier = Modifier.weight(0.5f)) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { /* TODO: Edit vehicle */ },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { /* TODO: Delete vehicle */ },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                )
                if (vehicle.status == "Available") {
                    DropdownMenuItem(
                        text = { Text("Mark as Rented") },
                        onClick = { /* TODO: Mark as rented */ },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Mark as Available") },
                        onClick = { /* TODO: Mark as available */ },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleUnitsSection(
    units: List<VehicleUnitInput>,
    onUnitsChange: (List<VehicleUnitInput>) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicle Units",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        onUnitsChange(units + VehicleUnitInput())
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Unit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add individual vehicle units with their plate numbers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            units.forEachIndexed { index, unit ->
                VehicleUnitCard(
                    unit = unit,
                    index = index,
                    onUnitChange = { updatedUnit ->
                        val newUnits = units.toMutableList()
                        newUnits[index] = updatedUnit
                        onUnitsChange(newUnits)
                    },
                    onRemove = if (units.size > 1) {
                        {
                            val newUnits = units.toMutableList()
                            newUnits.removeAt(index)
                            onUnitsChange(newUnits)
                        }
                    } else null
                )

                if (index < units.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun VehicleUnitCard(
    unit: VehicleUnitInput,
    index: Int,
    onUnitChange: (VehicleUnitInput) -> Unit,
    onRemove: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = "Unit ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (onRemove != null) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove Unit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = unit.plateNumber,
                onValueChange = { onUnitChange(unit.copy(plateNumber = it)) },
                label = { Text("Plate Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., AB1234CD") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = unit.currentLocation,
                onValueChange = { onUnitChange(unit.copy(currentLocation = it)) },
                label = { Text("Current Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g., Yogyakarta") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = unit.notes,
                onValueChange = { onUnitChange(unit.copy(notes = it)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                placeholder = { Text("Additional notes about this unit") }
            )
        }
    }
}