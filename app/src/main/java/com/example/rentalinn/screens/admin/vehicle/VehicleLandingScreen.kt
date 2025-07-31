package com.example.rentalinn.screens.admin.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentalinn.model.Vehicle
import com.example.rentalinn.ui.components.ErrorSnackbar
import com.example.rentalinn.ui.components.SuccessSnackbar
import com.example.rentalinn.viewmodel.VehicleViewModel
import com.example.rentalinn.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleLandingScreen(
    navController: NavController,
    viewModel: VehicleViewModel = viewModel(factory = VehicleViewModel.Factory(LocalContext.current))
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadVehicles(true)
    }
    
    LaunchedEffect(error, successMessage) {
        if (error != null || successMessage != null) {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = error ?: successMessage ?: "",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            // Sheet content would go here if needed
        },
        topBar = {
            TopAppBar(
                title = { Text("Vehicle Management") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddVehicle.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search vehicles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // Categories horizontal list
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { 
                            selectedCategory = null
                            viewModel.loadVehicles(true)
                        },
                        label = { Text("All") }
                    )
                }
                
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { 
                            selectedCategory = category
                            viewModel.getVehiclesByCategory(category)
                        },
                        label = { Text(category) }
                    )
                }
            }
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (vehicles.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No vehicles found",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.navigate(Screen.AddVehicle.route) }) {
                            Text("Add Vehicle")
                        }
                    }
                }
            } else {
                // Vehicles grid
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filter vehicles by search query
                    val filteredVehicles = if (searchQuery.isEmpty()) {
                        vehicles
                    } else {
                        vehicles.filter { vehicle ->
                            vehicle.title.contains(searchQuery, ignoreCase = true) ||
                            vehicle.brand.contains(searchQuery, ignoreCase = true) ||
                            vehicle.model.contains(searchQuery, ignoreCase = true) ||
                            vehicle.vehicleCategory.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    
                    items(filteredVehicles) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onClick = { navController.navigate("${Screen.VehicleDetail.route.replace("{vehicleId}", vehicle.id.toString())}") },
                            onEditClick = { navController.navigate("${Screen.EditVehicle.route.replace("{vehicleId}", vehicle.id.toString())}") },
                            onDeleteClick = { viewModel.deleteVehicle(vehicle.id) }
                        )
                    }
                }
            }
            
            // Show error if any
            error?.let {
                ErrorSnackbar(
                    message = it,
                    onDismiss = { viewModel.clearMessages() }
                )
            }
            
            // Show success message if any
            successMessage?.let {
                SuccessSnackbar(
                    message = it,
                    onDismiss = { viewModel.clearMessages() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Vehicle image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(vehicle.getMainPhoto() ?: "https://via.placeholder.com/300x200?text=No+Image")
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
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = vehicle.vehicleCategory,
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${vehicle.passengerCapacity} Seats",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = vehicle.getFormattedPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    onEditClick()
                                    showMenu = false
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    onDeleteClick()
                                    showMenu = false
                                }
                            )
                            
                            if (vehicle.isAvailable()) {
                                DropdownMenuItem(
                                    text = { Text("Mark as Rented") },
                                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                                    onClick = {
                                        // TODO: Implement status change
                                        showMenu = false
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Mark as Available") },
                                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                                    onClick = {
                                        // TODO: Implement status change
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 