package com.example.rentalinn.screens.user.rental

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerScreen(
    navController: NavController,
    locationType: String, // "pickup" or "return"
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Map state
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var isLoadingAddress by remember { mutableStateOf(false) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Default location (Jakarta)
    val defaultLocation = GeoPoint(-6.2088, 106.8456)

    // Get current location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        if (locationPermissions.allPermissionsGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentGeoPoint = GeoPoint(it.latitude, it.longitude)
                    mapView?.let { map ->
                        map.controller.setCenter(currentGeoPoint)
                        map.controller.setZoom(17.0)
                    }
                }
            }
        }
    }

    // Get address from coordinates
    fun getAddressFromGeoPoint(geoPoint: GeoPoint) {
        isLoadingAddress = true
        scope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    selectedAddress = addresses[0].getAddressLine(0) ?: "Unknown location"
                } else {
                    selectedAddress = "Lat: ${geoPoint.latitude}, Lng: ${geoPoint.longitude}"
                }
            } catch (e: Exception) {
                selectedAddress = "Lat: ${geoPoint.latitude}, Lng: ${geoPoint.longitude}"
            } finally {
                isLoadingAddress = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        if (!locationPermissions.allPermissionsGranted) {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    // Handle MapView lifecycle
    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        LocationPickerHeader(
            title = if (locationType == "pickup") "Pilih Lokasi Pickup" else "Pilih Lokasi Return",
            onBackClick = { navController.popBackStack() }
        )
        
        // Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(defaultLocation)

                        // Set up map click listener
                        setOnTouchListener { _, event ->
                            if (event.action == android.view.MotionEvent.ACTION_UP) {
                                val projection = projection
                                val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint

                                // Clear previous markers
                                overlays.clear()

                                // Add new marker
                                val marker = Marker(this).apply {
                                    position = geoPoint
                                    title = if (locationType == "pickup") "Pickup Location" else "Return Location"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                overlays.add(marker)

                                selectedLocation = geoPoint
                                getAddressFromGeoPoint(geoPoint)

                                invalidate()
                            }
                            false
                        }

                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Current location button
            FloatingActionButton(
                onClick = { getCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Current Location",
                    tint = Color.White
                )
            }
        }
        
        // Selected location info
        if (selectedLocation != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lokasi Terpilih",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isLoadingAddress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = selectedAddress,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            selectedLocation?.let { location ->
                                // Return result to previous screen
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "${locationType}_location_result",
                                    mapOf(
                                        "address" to selectedAddress,
                                        "latitude" to location.latitude,
                                        "longitude" to location.longitude
                                    )
                                )
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedLocation != null && !isLoadingAddress
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Lokasi Ini")
                    }
                }
            }
        }
        
        // Permission request
        if (!locationPermissions.allPermissionsGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Izin Lokasi Diperlukan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Aplikasi memerlukan izin lokasi untuk menampilkan posisi Anda saat ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { locationPermissions.launchMultiplePermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Berikan Izin Lokasi")
                    }
                }
            }
        }
    }
}

@Composable
fun LocationPickerHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
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
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
