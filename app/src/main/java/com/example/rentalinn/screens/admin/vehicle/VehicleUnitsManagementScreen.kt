package com.example.rentalinn.screens.admin.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.model.VehicleUnit
import com.example.rentalinn.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleUnitsManagementScreen(
    vehicleId: Int,
    navController: NavController,
    viewModel: VehicleViewModel = viewModel(factory = VehicleViewModel.Factory(LocalContext.current))
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var editingUnit by remember { mutableStateOf<VehicleUnit?>(null) }

    LaunchedEffect(vehicleId) {
        viewModel.getVehicleById(vehicleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Vehicle Units")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddUnitDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Unit")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUnitDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Unit")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                                onClick = { viewModel.getVehicleById(vehicleId) }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                vehicle != null -> {
                    VehicleUnitsContent(
                        vehicle = vehicle!!,
                        onEditUnit = { editingUnit = it },
                        onDeleteUnit = { unit ->
                        viewModel.deleteVehicleUnit(
                            unitId = unit.id,
                            vehicleId = vehicleId
                        )
                    }
                    )
                }
            }
        }
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        AddVehicleUnitDialog(
            vehicleId = vehicleId,
            onDismiss = { showAddUnitDialog = false },
            onConfirm = { plateNumber, location, notes ->
                viewModel.createVehicleUnit(
                    vehicleId = vehicleId,
                    plateNumber = plateNumber,
                    location = location.takeIf { it.isNotBlank() },
                    notes = notes.takeIf { it.isNotBlank() }
                ) {
                    showAddUnitDialog = false
                }
            }
        )
    }

    // Edit Unit Dialog
    editingUnit?.let { unit ->
        EditVehicleUnitDialog(
            unit = unit,
            onDismiss = { editingUnit = null },
            onConfirm = { updatedUnit ->
                viewModel.updateVehicleUnit(updatedUnit) {
                    editingUnit = null
                }
            }
        )
    }
}

@Composable
fun VehicleUnitsContent(
    vehicle: Vehicle,
    onEditUnit: (VehicleUnit) -> Unit,
    onDeleteUnit: (VehicleUnit) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Summary Card
            VehicleUnitsSummaryCard(vehicle = vehicle)
        }

        val units = vehicle.units ?: emptyList()
        
        if (units.isEmpty()) {
            item {
                EmptyUnitsCard()
            }
        } else {
            items(units) { unit ->
                VehicleUnitCard(
                    unit = unit,
                    onEdit = { onEditUnit(unit) },
                    onDelete = { onDeleteUnit(unit) }
                )
            }
        }
    }
}

@Composable
fun VehicleUnitsSummaryCard(vehicle: Vehicle) {
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
                text = "Units Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Total",
                    value = vehicle.getTotalUnitsCount().toString(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SummaryItem(
                    label = "Available",
                    value = vehicle.getAvailableUnits().toString(),
                    color = Color(0xFF10B981)
                )
                SummaryItem(
                    label = "Rented",
                    value = vehicle.getRentedUnitsCount().toString(),
                    color = Color(0xFFEF4444)
                )
                SummaryItem(
                    label = "Maintenance",
                    value = vehicle.getMaintenanceUnitsCount().toString(),
                    color = Color(0xFFF59E0B)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun EmptyUnitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Units Added",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Add vehicle units to start managing individual vehicles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VehicleUnitCard(
    unit: VehicleUnit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    text = unit.getFormattedPlateNumber(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Unit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Unit",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(unit.getStatusColor())),
                            RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = unit.getStatusDisplayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location
            if (!unit.currentLocation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
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

            // Mileage
            if (unit.mileage != null && unit.mileage > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${unit.mileage} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notes
            if (!unit.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = unit.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun AddVehicleUnitDialog(
    vehicleId: Int,
    onDismiss: () -> Unit,
    onConfirm: (plateNumber: String, location: String, notes: String) -> Unit
) {
    var plateNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Vehicle Unit") },
        text = {
            Column {
                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it },
                    label = { Text("Plate Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., AB1234CD") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Current Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Yogyakarta") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { Text("Additional notes") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (plateNumber.isNotBlank()) {
                        onConfirm(plateNumber, location, notes)
                    }
                },
                enabled = plateNumber.isNotBlank()
            ) {
                Text("Add")
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
fun EditVehicleUnitDialog(
    unit: VehicleUnit,
    onDismiss: () -> Unit,
    onConfirm: (VehicleUnit) -> Unit
) {
    var plateNumber by remember { mutableStateOf(unit.plateNumber) }
    var location by remember { mutableStateOf(unit.currentLocation ?: "") }
    var notes by remember { mutableStateOf(unit.notes ?: "") }
    var status by remember { mutableStateOf(unit.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Vehicle Unit") },
        text = {
            Column {
                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it },
                    label = { Text("Plate Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown
                var expanded by remember { mutableStateOf(false) }
                val statusOptions = listOf("available", "rented", "maintenance", "out_of_service")

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = status.replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    status = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Current Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (plateNumber.isNotBlank()) {
                        val updatedUnit = unit.copy(
                            plateNumber = plateNumber,
                            status = status,
                            currentLocation = location.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                        onConfirm(updatedUnit)
                    }
                },
                enabled = plateNumber.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
