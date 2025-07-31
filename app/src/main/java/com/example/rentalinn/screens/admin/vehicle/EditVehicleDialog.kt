package com.example.rentalinn.screens.admin.vehicle

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rentalinn.model.Vehicle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onConfirm: (
        id: Int,
        title: String?,
        brand: String?,
        model: String?,
        vehicleCategory: String?,
        year: Int?,
        pricePerDay: Double?,
        description: String?,
        status: String?,
        transmission: String?,
        fuelType: String?,
        passengerCapacity: Int?,
        features: List<String>?,
        photos: List<File>?
    ) -> Unit,
    onManageUnits: ((Int) -> Unit)? = null,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    
    // Form state - initialize with current values
    var title by remember { mutableStateOf(vehicle.title) }
    var brand by remember { mutableStateOf(vehicle.brand) }
    var model by remember { mutableStateOf(vehicle.model) }
    var vehicleCategory by remember { mutableStateOf(vehicle.vehicleCategory) }
    var year by remember { mutableStateOf(vehicle.year.toString()) }
    var pricePerDay by remember { mutableStateOf(vehicle.pricePerDay.toString()) }
    var description by remember { mutableStateOf(vehicle.description ?: "") }
    var transmission by remember { mutableStateOf(vehicle.transmission) }
    var fuelType by remember { mutableStateOf(vehicle.fuelType) }
    var passengerCapacity by remember { mutableStateOf(vehicle.passengerCapacity.toString()) }
    var features by remember { mutableStateOf(vehicle.features?.joinToString(", ") ?: "") }
    var status by remember { mutableStateOf(vehicle.status) }
    
    // Image picker
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
        }
    }

    // Debug logging
    LaunchedEffect(Unit) {
        println("DEBUG: Editing vehicle: ${vehicle.id}")
        println("DEBUG: Initial title: $title")
        println("DEBUG: Initial brand: $brand")
        println("DEBUG: Initial features: $features")
    }

    // Function to convert URIs to files
    fun urisToFiles(context: Context, uris: List<Uri>): List<File> {
        return uris.mapNotNull { uri ->
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
                println("DEBUG: Error converting URI to file: ${e.message}")
                null
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Vehicle",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(
                        onClick = { if (!isLoading) onDismiss() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Basic information
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Vehicle Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null)
                    }
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
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isLoading
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
                        singleLine = true,
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(Icons.Default.Category, contentDescription = null)
                        }
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                // License plates are now managed through vehicle units
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Vehicle Units",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Units: ${vehicle.getTotalUnitsCount()} total, ${vehicle.getAvailableUnits()} available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (onManageUnits != null) {
                            TextButton(
                                onClick = { onManageUnits(vehicle.id) }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Manage Units")
                            }
                        } else {
                            Text(
                                text = "Use the vehicle management screen to add/edit individual units",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
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
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null)
                        }
                    )
                    // Unit count is managed through vehicle units
                    Text(
                        text = "Units: ${vehicle.getTotalUnitsCount()}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = status == "available",
                            onClick = { status = "available" },
                            enabled = !isLoading
                        )
                        Text(
                            text = "Available",
                            modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = status == "rented",
                            onClick = { status = "rented" },
                            enabled = !isLoading
                        )
                        Text(
                            text = "Rented",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
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
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = fuelType,
                        onValueChange = { fuelType = it },
                        label = { Text("Fuel Type") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }
                
                OutlinedTextField(
                    value = passengerCapacity,
                    onValueChange = { passengerCapacity = it },
                    label = { Text("Passenger Capacity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )
                
                OutlinedTextField(
                    value = features,
                    onValueChange = { features = it },
                    label = { Text("Features (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    singleLine = false,
                    enabled = !isLoading
                )
                
                // Image upload
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Replace Vehicle Images")
                }
                
                // Display selected images
                if (selectedImageUris.isNotEmpty()) {
                    Text(
                        text = "${selectedImageUris.size} new images selected (will replace existing)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // Show current photos info if available
                    vehicle.photos?.let { currentPhotos ->
                        if (currentPhotos.isNotEmpty()) {
                            Text(
                                text = "${currentPhotos.size} existing photo(s)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (!isLoading) onDismiss() },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            // Debug logging
                            println("DEBUG: Update button clicked")
                            println("DEBUG: Title: $title")
                            println("DEBUG: Brand: $brand")
                            println("DEBUG: Model: $model")
                            println("DEBUG: Category: $vehicleCategory")
                            println("DEBUG: Year: $year")
                            println("DEBUG: Price: $pricePerDay")
                            println("DEBUG: Status: $status")
                            println("DEBUG: Features: $features")
                            
                            // Process features - split by comma, trim, and filter out empty values
                            val featuresList = features.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .takeIf { it.isNotEmpty() }
                            
                            // Convert URIs to files
                            val photoFiles = if (selectedImageUris.isNotEmpty()) {
                                urisToFiles(context, selectedImageUris)
                            } else {
                                null
                            }
                            
                            // Always pass the current values, not null
                            onConfirm(
                                vehicle.id,
                                title,
                                brand,
                                model,
                                vehicleCategory,
                                year.toIntOrNull(),
                                pricePerDay.toDoubleOrNull(),
                                description,
                                status,
                                transmission,
                                fuelType,
                                passengerCapacity.toIntOrNull(),
                                featuresList,
                                photoFiles
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
} 