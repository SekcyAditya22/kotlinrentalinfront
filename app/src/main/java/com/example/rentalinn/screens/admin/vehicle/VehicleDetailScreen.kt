package com.example.rentalinn.screens.admin.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.ui.components.ErrorSnackbar
import com.example.rentalinn.viewmodel.VehicleViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.FlowColumnScope
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Int,
    navController: NavController,
    viewModel: VehicleViewModel = viewModel(factory = VehicleViewModel.Factory(LocalContext.current))
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Load vehicle details
    LaunchedEffect(vehicleId) {
        viewModel.getVehicleById(vehicleId)
    }
    
    // Handle error
    LaunchedEffect(error) {
        if (error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error ?: "An error occurred",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vehicle?.title ?: "Vehicle Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("edit_vehicle/${vehicleId}") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (vehicle != null) {
                VehicleDetailContent(vehicle!!, navController)
            } else if (error != null) {
                ErrorSnackbar(
                    message = error ?: "Failed to load vehicle details",
                    onDismiss = { viewModel.clearMessages() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDetailContent(vehicle: Vehicle, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Vehicle image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(vehicle.getMainPhoto() ?: "https://via.placeholder.com/600x400?text=No+Image")
                    .crossfade(true)
                    .build(),
                contentDescription = vehicle.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Status chip
            Surface(
                color = if (vehicle.isAvailable()) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(bottomStart = 12.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = if (vehicle.isAvailable()) "Available" else "Rented",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        // Vehicle details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title and price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = vehicle.getFormattedPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Brand, model, year
            Text(
                text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})",
                style = MaterialTheme.typography.bodyLarge
            )
            
            // License plate
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Numbers,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Units: ${vehicle.getTotalUnitsCount()} total, ${vehicle.getAvailableUnits()} available",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Divider()
            
            // Specifications
            Text(
                text = "Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Specs grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecificationRow(
                    icon = Icons.Default.Category,
                    label = "Category",
                    value = vehicle.vehicleCategory
                )
                
                SpecificationRow(
                    icon = Icons.Default.Settings,
                    label = "Transmission",
                    value = vehicle.transmission
                )
                
                SpecificationRow(
                    icon = Icons.Default.LocalGasStation,
                    label = "Fuel Type",
                    value = vehicle.fuelType
                )
                
                SpecificationRow(
                    icon = Icons.Default.Person,
                    label = "Passenger Capacity",
                    value = "${vehicle.passengerCapacity} People"
                )
                
                SpecificationRow(
                    icon = Icons.Default.Inventory,
                    label = "Units Available",
                    value = "${vehicle.getAvailableUnits()}/${vehicle.getTotalUnitsCount()} Units"
                )
            }
            
            Divider()
            
            // Description
            if (!vehicle.description.isNullOrBlank()) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = vehicle.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Divider()
            }
            
            // Features
            if (!vehicle.features.isNullOrEmpty()) {
                Text(
                    text = "Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Use a simple Row with horizontal scroll instead of FlowRow
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vehicle.features.size) { index ->
                        AssistChip(
                            onClick = { },
                            label = { Text(vehicle.features[index]) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("edit_vehicle/${vehicle.id}") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                
                Button(
                    onClick = { /* TODO: Implement rent/return functionality */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vehicle.isAvailable())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = if (vehicle.isAvailable())
                            Icons.Default.DirectionsCar
                        else
                            Icons.Default.AssignmentReturn,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (vehicle.isAvailable())
                            "Rent Now"
                        else
                            "Return"
                    )
                }
            }
        }
    }
}

@Composable
fun SpecificationRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
} 