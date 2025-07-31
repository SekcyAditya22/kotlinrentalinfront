package com.example.rentalinn.screens.admin.vehicle

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.example.rentalinn.ui.components.ErrorSnackbar
import com.example.rentalinn.ui.components.SuccessSnackbar
import com.example.rentalinn.viewmodel.VehicleViewModel
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleScreen(
    vehicleId: Int,
    navController: NavController,
    viewModel: VehicleViewModel = viewModel(factory = VehicleViewModel.Factory(LocalContext.current))
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    
    // Load vehicle data
    LaunchedEffect(vehicleId) {
        viewModel.getVehicleById(vehicleId)
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
                    message = successMessage ?: "Vehicle updated successfully",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearMessages()
                navController.popBackStack()
            }
        }
    }
    
    // Show dialog when vehicle data is loaded
    LaunchedEffect(vehicle) {
        if (vehicle != null) {
            showDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Vehicle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (isLoading && vehicle == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
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
    
    // Show edit dialog when vehicle data is loaded
    if (showDialog && vehicle != null) {
        EditVehicleDialog(
            vehicle = vehicle!!,
            isLoading = isLoading,
            onDismiss = {
                showDialog = false
                navController.popBackStack()
            },
            onConfirm = { id, title, brand, model, vehicleCategory, year,
                         pricePerDay, description, status, transmission, fuelType,
                         passengerCapacity, features, photos ->

                viewModel.updateVehicle(
                    id = id,
                    title = title,
                    brand = brand,
                    model = model,
                    vehicleCategory = vehicleCategory,
                    year = year,
                    licensePlate = null, // No longer used
                    pricePerDay = pricePerDay,
                    unit = null, // No longer used
                    description = description,
                    status = status,
                    transmission = transmission,
                    fuelType = fuelType,
                    passengerCapacity = passengerCapacity,
                    features = features,
                    photos = photos
                )
            },
            onManageUnits = { vehicleId ->
                navController.navigate("vehicle_units_management/$vehicleId")
            }
        )
    }
} 