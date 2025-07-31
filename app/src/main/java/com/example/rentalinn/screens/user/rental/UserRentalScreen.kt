package com.example.rentalinn.screens.user.rental

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.util.Log
import com.example.rentalinn.ui.components.*
import com.example.rentalinn.viewmodel.VehicleViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

enum class ViewMode {
    GRID, CARD
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRentalScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicleViewModel: VehicleViewModel = viewModel(
        factory = VehicleViewModel.Factory(context)
    )

    val vehicles by vehicleViewModel.vehicles.collectAsState()
    val isLoading by vehicleViewModel.isLoading.collectAsState()
    val isRefreshing by vehicleViewModel.isRefreshing.collectAsState()
    val error by vehicleViewModel.error.collectAsState()
    val errorType by vehicleViewModel.errorType.collectAsState()
    val isNetworkAvailable by vehicleViewModel.isNetworkAvailable.collectAsState()
    val categories by vehicleViewModel.categories.collectAsState()
    val searchQuery by vehicleViewModel.searchQuery.collectAsState()
    val currentFilter by vehicleViewModel.currentFilter.collectAsState()
    val currentSort by vehicleViewModel.currentSort.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.CARD) }

    var showSearchBar by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf<com.example.rentalinn.model.Vehicle?>(null) }
    var showVehicleDetail by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

    // Animation states
    val headerAlpha by animateFloatAsState(
        targetValue = if (showSearchBar) 0.7f else 1f,
        animationSpec = tween(300),
        label = "headerAlpha"
    )



    // Handle pull to refresh
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullToRefreshState.startRefresh()
        } else {
            pullToRefreshState.endRefresh()
        }
    }

    // Trigger refresh when user pulls
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            vehicleViewModel.refreshVehicles()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Header with Gradient Background
            ModernHeader(
                title = "Find Your Ride",
                subtitle = "${vehicles.size} vehicles available",
                onSearchClick = { showSearchBar = !showSearchBar },
                onFilterClick = { showFilterDialog = true },
                alpha = headerAlpha,
                isNetworkAvailable = isNetworkAvailable
            )

            // Animated Search Bar
            AnimatedVisibility(
                visible = showSearchBar,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                ModernSearchBar(
                    query = searchQuery,
                    onQueryChange = { vehicleViewModel.updateSearchQuery(it) },
                    onSearch = { vehicleViewModel.updateSearchQuery(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }



            // View Mode Selector & Stats
            ViewModeSelector(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                vehicleCount = vehicles.size,
                currentSort = currentSort,
                onSortChange = { vehicleViewModel.updateSort(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
            ) {

                // Vehicle Content with Modern Design
                when {
                    isLoading -> {
                        ModernLoadingState()
                    }
                    error != null && errorType != null -> {
                        ModernErrorState(
                            errorType = errorType?.toString() ?: "UNKNOWN",
                            onRetry = { vehicleViewModel.retryLastOperation() }
                        )
                    }
                    vehicles.isEmpty() -> {
                        ModernEmptyState(
                            searchQuery = searchQuery,
                            hasFilters = currentFilter.category != null,
                            onClearFilters = {
                                vehicleViewModel.clearSearch()
                                vehicleViewModel.resetFilter()
                            }
                        )
                    }
                    else -> {
                        VehicleContent(
                            vehicles = vehicles,
                            viewMode = viewMode,
                            onVehicleClick = { vehicle ->
                                selectedVehicle = vehicle
                                showVehicleDetail = true
                            },
                            onRentClick = { vehicle ->
                                Log.d("UserRentalScreen", "Rent Now clicked for vehicle: ${vehicle.id}")
                                navController.navigate("rental_booking/${vehicle.id}")
                            }
                        )
                    }
                }
            }
        }
        
        // Modern Filter Dialog
        if (showFilterDialog) {
            ModernFilterDialog(
                filter = currentFilter,
                onFilterChange = { vehicleViewModel.updateFilter(it) },
                onDismiss = { showFilterDialog = false },
                onApply = { showFilterDialog = false },
                onReset = {
                    vehicleViewModel.resetFilter()
                    showFilterDialog = false
                },
                categories = categories
            )
        }

        // Pull to refresh indicator
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Vehicle Detail Dialog
        if (showVehicleDetail && selectedVehicle != null) {
            VehicleDetailDialog(
                vehicle = selectedVehicle!!,
                onDismiss = {
                    showVehicleDetail = false
                    selectedVehicle = null
                },
                onRentClick = { vehicle ->
                    showVehicleDetail = false
                    selectedVehicle = null
                    Log.d("UserRentalScreen", "Rent Now clicked from dialog for vehicle: ${vehicle.id}")
                    navController.navigate("rental_booking/${vehicle.id}")
                }
            )
        }
    }
}

// ==================== MODERN UI COMPONENTS ====================

@Composable
fun ModernHeader(
    title: String,
    subtitle: String,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    alpha: Float,
    isNetworkAvailable: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .alpha(alpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Network Status
            if (!isNetworkAvailable) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search vehicles, brands, models...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            singleLine = true
        )
    }
}



@Composable
fun ViewModeSelector(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    vehicleCount: Int,
    currentSort: com.example.rentalinn.ui.components.SortOption,
    onSortChange: (com.example.rentalinn.ui.components.SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats and Sort
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "$vehicleCount vehicles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                VehicleSortChip(
                    currentSort = currentSort,
                    onSortChange = onSortChange
                )
            }

            // View Mode Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ViewModeButton(
                    icon = Icons.Default.GridView,
                    isSelected = viewMode == ViewMode.GRID,
                    onClick = { onViewModeChange(ViewMode.GRID) }
                )
                ViewModeButton(
                    icon = Icons.Default.ViewModule,
                    isSelected = viewMode == ViewMode.CARD,
                    onClick = { onViewModeChange(ViewMode.CARD) }
                )
            }
        }
    }
}

@Composable
fun ViewModeButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                CircleShape
            )
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ModernLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated loading indicator
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )
            }

            Text(
                text = "Finding amazing vehicles...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Please wait while we load the best options for you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernErrorState(
    errorType: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Oops! Something went wrong",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when (errorType) {
                        "NETWORK" -> "Please check your internet connection and try again"
                        "SERVER" -> "Our servers are having issues. Please try again later"
                        else -> "An unexpected error occurred. Please try again"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun ModernEmptyState(
    searchQuery: String,
    hasFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated empty state icon
            val infiniteTransition = rememberInfiniteTransition(label = "empty")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            Text(
                text = if (searchQuery.isNotEmpty() || hasFilters) {
                    "No vehicles match your criteria"
                } else {
                    "No vehicles available"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (searchQuery.isNotEmpty() || hasFilters) {
                    "Try adjusting your search terms or filters to find more options"
                } else {
                    "Check back later for new vehicles or contact support"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (searchQuery.isNotEmpty() || hasFilters) {
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear All Filters")
                }
            }
        }
    }
}

@Composable
fun VehicleContent(
    vehicles: List<com.example.rentalinn.model.Vehicle>,
    viewMode: ViewMode,
    onVehicleClick: (com.example.rentalinn.model.Vehicle) -> Unit,
    onRentClick: (com.example.rentalinn.model.Vehicle) -> Unit
) {
    when (viewMode) {
        ViewMode.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(vehicles) { vehicle ->
                    CompactVehicleCard(
                        vehicle = vehicle,
                        onCardClick = { onVehicleClick(vehicle) },
                        onRentClick = { onRentClick(vehicle) }
                    )
                }
            }
        }
        ViewMode.CARD -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(vehicles) { vehicle ->
                    ModernVehicleCard(
                        vehicle = vehicle,
                        onCardClick = { onVehicleClick(vehicle) },
                        onRentClick = { onRentClick(vehicle) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernFilterDialog(
    filter: com.example.rentalinn.ui.components.VehicleFilter,
    onFilterChange: (com.example.rentalinn.ui.components.VehicleFilter) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    categories: List<String>
) {
    VehicleFilterDialog(
        filter = filter,
        onFilterChange = onFilterChange,
        onDismiss = onDismiss,
        onApply = onApply,
        onReset = onReset,
        categories = categories
    )
}

@Composable
fun ModernVehicleCard(
    vehicle: com.example.rentalinn.model.Vehicle,
    onCardClick: () -> Unit,
    onRentClick: () -> Unit
) {
    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Vehicle Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = vehicle.getMainPhoto() ?: "",
                    contentDescription = vehicle.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Availability Badge
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = vehicle.getStatusColor()
                    )
                ) {
                    Text(
                        text = vehicle.getAvailabilityText(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Button
                IconButton(
                    onClick = { /* TODO: Add to favorites */ },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = Color.White
                    )
                }
            }

            // Vehicle Info
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vehicle.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${vehicle.brand} • ${vehicle.year}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = vehicle.getFormattedPrice(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Vehicle Features
                val features = vehicle.features ?: emptyList()
                if (features.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(features.take(3)) { feature ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    text = feature,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Action Button
                Button(
                    onClick = onRentClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vehicle.isAvailable(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vehicle.isAvailable())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        if (vehicle.isAvailable()) Icons.Default.DirectionsCar else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            !vehicle.isAvailable() -> "Not Available"
                            vehicle.getAvailableUnits() <= 2 -> "Limited Stock - Rent Now"
                            else -> "Rent Now"
                        }
                    )
                }
            }
        }
    }
}

// ==================== VEHICLE DETAIL DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailDialog(
    vehicle: com.example.rentalinn.model.Vehicle,
    onDismiss: () -> Unit,
    onRentClick: (com.example.rentalinn.model.Vehicle) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    // Header with Image and Close Button
                    VehicleDetailHeader(
                        vehicle = vehicle,
                        onClose = onDismiss
                    )
                }

                item {
                    // Vehicle Title and Basic Info
                    VehicleDetailTitle(vehicle = vehicle)
                }

                item {
                    // Specifications Grid
                    VehicleSpecifications(vehicle = vehicle)
                }

                item {
                    // Features Section
                    VehicleFeatures(vehicle = vehicle)
                }

                item {
                    // Description
                    VehicleDescription(vehicle = vehicle)
                }

                item {
                    // Action Buttons
                    VehicleDetailActions(
                        vehicle = vehicle,
                        onRentClick = { onRentClick(vehicle) },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleDetailHeader(
    vehicle: com.example.rentalinn.model.Vehicle,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Vehicle Image with Parallax Effect
        AsyncImage(
            model = vehicle.getMainPhoto() ?: "",
            contentDescription = vehicle.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Close Button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Availability Badge
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = vehicle.getStatusColor()
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = vehicle.getAvailabilityText(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Favorite Button
        IconButton(
            onClick = { /* TODO: Add to favorites */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = "Add to favorites",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun VehicleDetailTitle(
    vehicle: com.example.rentalinn.model.Vehicle
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Vehicle Title
        Text(
            text = vehicle.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Brand and Model
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${vehicle.brand} ${vehicle.model}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Year Badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = vehicle.year.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Price Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Price per day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = vehicle.getFormattedPrice(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // License Plate
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Multiple Units Available",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun VehicleSpecifications(
    vehicle: com.example.rentalinn.model.Vehicle
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Specifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Specifications Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(250.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SpecificationCard(
                    icon = Icons.Default.Category,
                    title = "Category",
                    value = vehicle.vehicleCategory,
                    color = Color(0xFF6366F1)
                )
            }

            item {
                SpecificationCard(
                    icon = Icons.Default.People,
                    title = "Passengers",
                    value = "${vehicle.passengerCapacity} seats",
                    color = Color(0xFF10B981)
                )
            }

            item {
                SpecificationCard(
                    icon = Icons.Default.Settings,
                    title = "Transmission",
                    value = vehicle.transmission,
                    color = Color(0xFFF59E0B)
                )
            }

            item {
                SpecificationCard(
                    icon = Icons.Default.LocalGasStation,
                    title = "Fuel Type",
                    value = vehicle.fuelType,
                    color = Color(0xFFEF4444)
                )
            }

            item {
                SpecificationCard(
                    icon = Icons.Default.Inventory,
                    title = "Availability",
                    value = vehicle.getAvailabilityText(),
                    color = vehicle.getStatusColor()
                )
            }

            item {
                SpecificationCard(
                    icon = Icons.Default.DirectionsCar,
                    title = "Total Units",
                    value = "${vehicle.getTotalUnitsCount()} units",
                    color = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
fun SpecificationCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VehicleFeatures(
    vehicle: com.example.rentalinn.model.Vehicle
) {
    val features = vehicle.features ?: emptyList()

    if (features.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Features & Amenities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(features) { feature ->
                    FeatureChip(feature = feature)
                }
            }
        }
    }
}

@Composable
fun FeatureChip(feature: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = feature,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun VehicleDescription(
    vehicle: com.example.rentalinn.model.Vehicle
) {
    if (!vehicle.description.isNullOrBlank()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = vehicle.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun VehicleDetailActions(
    vehicle: com.example.rentalinn.model.Vehicle,
    onRentClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Rent Button
        Button(
            onClick = onRentClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = vehicle.isAvailable(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (vehicle.isAvailable())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    if (vehicle.isAvailable()) Icons.Default.DirectionsCar else Icons.Default.Block,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (vehicle.isAvailable()) "Rent Now - ${vehicle.getFormattedPrice()}" else "Not Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Contact Button
            OutlinedButton(
                onClick = { /* TODO: Contact owner */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact")
            }

            // Share Button
            OutlinedButton(
                onClick = { /* TODO: Share vehicle */ },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Close Button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Close",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



