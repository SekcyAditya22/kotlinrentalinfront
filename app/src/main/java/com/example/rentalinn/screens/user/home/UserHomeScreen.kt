
package com.example.rentalinn.screens.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.findNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.rentalinn.ui.components.*
import com.example.rentalinn.viewmodel.VehicleViewModel
import com.example.rentalinn.viewmodel.UserStatsViewModel
import com.example.rentalinn.model.UserStatsData
import com.example.rentalinn.model.Rental
import com.example.rentalinn.model.UserDetails
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.rentalinn.utils.UserSessionManager
import com.example.rentalinn.repository.UserDetailsRepository
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vehicleViewModel: VehicleViewModel = viewModel(
        factory = VehicleViewModel.Factory(context)
    )
    val userStatsViewModel: UserStatsViewModel = viewModel(
        factory = UserStatsViewModel.Factory(context)
    )

    val vehicles by vehicleViewModel.vehicles.collectAsState()
    val isLoading by vehicleViewModel.isLoading.collectAsState()
    val isRefreshing by vehicleViewModel.isRefreshing.collectAsState()
    val error by vehicleViewModel.error.collectAsState()
    val errorType by vehicleViewModel.errorType.collectAsState()
    val isNetworkAvailable by vehicleViewModel.isNetworkAvailable.collectAsState()

    // User stats states
    val userStats by userStatsViewModel.userStats.collectAsState()
    val statsLoading by userStatsViewModel.isLoading.collectAsState()

    // User session and details for verification
    val userSessionManager = remember { UserSessionManager.getInstance(context) }
    val currentUser by userSessionManager.currentUser.collectAsState(initial = null)
    val userDetailsRepository = remember { UserDetailsRepository.getInstance(context) }
    var userDetails by remember { mutableStateOf<UserDetails?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }

    val pullToRefreshState = rememberPullToRefreshState()

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
            userStatsViewModel.refreshUserStats()
        }
    }

    // Load user details for verification check
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            try {
                val result = userDetailsRepository.getUserDetails()
                result.fold(
                    onSuccess = { details ->
                        userDetails = details
                    },
                    onFailure = { exception ->
                        android.util.Log.e("UserHomeScreen", "Failed to load user details", exception)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("UserHomeScreen", "Error loading user details", e)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Network status indicator
            OfflineIndicator(isOffline = !isNetworkAvailable)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

            // Welcome Section with Gradient
            item {
                WelcomeSection()
            }

            // Quick Stats Cards
            item {
                QuickStatsSection(
                    userStats = userStats,
                    isLoading = statsLoading
                )
            }

            // Quick Actions
            item {
                QuickActionsSection(navController)
            }

            // Featured Vehicles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Featured Vehicles",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(
                        onClick = {
                            navController.navigate("user_rental") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        Text("View All")
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                FeaturedVehiclesSection(
                    vehicles = vehicles,
                    isLoading = isLoading,
                    error = error,
                    errorType = errorType,
                    onRetry = { vehicleViewModel.retryLastOperation() },
                    onVehicleClick = { _ ->
                        // TODO: Navigate to vehicle detail
                    },
                    onRentClick = { vehicle ->
                        // Check user verification status before allowing rental
                        val user = currentUser
                        val details = userDetails

                        when {
                            user == null -> {
                                android.util.Log.e("UserHomeScreen", "User not logged in")
                                return@FeaturedVehiclesSection
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
                                // All checks passed, proceed to rental booking
                                navController.navigate("rental_booking/${vehicle.id}") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }

            // Active Rentals
            item {
                Text(
                    text = "Your Active Rentals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            item {
                ActiveRentalsSection(
                    activeRentals = userStats?.activeRentalDetails ?: emptyList(),
                    isLoading = statsLoading
                )
            }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }

        // Pull to refresh indicator
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
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
fun WelcomeSection() {
    val context = LocalContext.current
    val userSessionManager = remember { UserSessionManager.getInstance(context) }

    // User data states
    var userName by remember { mutableStateOf("Loading...") }
    var userProfilePicture by remember { mutableStateOf<String?>(null) }
    var userInitials by remember { mutableStateOf("U") }
    var userIsVerified by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(Unit) {
        userName = userSessionManager.getCurrentUserName() ?: "User"
        userProfilePicture = userSessionManager.getCurrentUserProfilePicture()
        userInitials = userSessionManager.getUserInitials()
        userIsVerified = userSessionManager.isCurrentUserVerified()
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        if (userIsVerified) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ready for your next adventure?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                
                // Profile Avatar with verification badge
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (!userProfilePicture.isNullOrEmpty()) {
                        AsyncImage(
                            model = getProfilePictureUrl(userProfilePicture),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_gallery),
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                        )
                    } else {
                        // Fallback to initials
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitials,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    // Small verification badge
                    if (userIsVerified) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsSection(
    userStats: UserStatsData?,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickStatCard(
            title = "Total Trips",
            value = if (isLoading) "..." else (userStats?.totalTrips?.toString() ?: "0"),
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            title = "Active",
            value = if (isLoading) "..." else (userStats?.activeRentals?.toString() ?: "0"),
            icon = Icons.Default.DirectionsCar,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.DirectionsCar,
                label = "Rent Vehicle",
                description = "Find your ride",
                color = MaterialTheme.colorScheme.primary,
                onClick = {
                    navController.navigate("user_rental") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Receipt,
                label = "History",
                description = "View transactions",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    navController.navigate("user_transaction") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                icon = Icons.Default.Settings,
                label = "Settings",
                description = "App settings",
                color = MaterialTheme.colorScheme.error,
                onClick = {
                    navController.navigate("user_settings") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Default.Person,
                label = "Profile",
                description = "Manage account",
                color = MaterialTheme.colorScheme.secondary,
                onClick = {
                    navController.navigate("user_profile") {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f), // Membuat card berbentuk persegi
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeaturedVehiclesSection(
    vehicles: List<com.example.rentalinn.model.Vehicle>,
    isLoading: Boolean,
    error: String?,
    errorType: ErrorType?,
    onRetry: () -> Unit,
    onVehicleClick: (com.example.rentalinn.model.Vehicle) -> Unit,
    onRentClick: (com.example.rentalinn.model.Vehicle) -> Unit
) {
    when {
        isLoading -> {
            VehicleLoadingRow()
        }
        error != null && errorType != null -> {
            ErrorMessage(
                errorType = errorType,
                onRetry = onRetry,
                modifier = Modifier.height(200.dp)
            )
        }
        vehicles.isEmpty() -> {
            VehicleEmptyState(
                title = "No vehicles available",
                message = "Check back later for new vehicles.",
                modifier = Modifier.height(200.dp)
            )
        }
        else -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(vehicles.take(5)) { vehicle ->
                    CompactVehicleCard(
                        vehicle = vehicle,
                        onCardClick = onVehicleClick,
                        onRentClick = onRentClick
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveRentalsSection(
    activeRentals: List<Rental>,
    isLoading: Boolean
) {
    if (isLoading) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    } else if (activeRentals.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Rentals",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Your active rentals will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            activeRentals.forEach { rental ->
                ModernRentalCard(
                    vehicleName = rental.getVehicleTitle(),
                    startDate = rental.startDate,
                    endDate = rental.endDate,
                    totalPrice = rental.getFormattedTotalAmount(),
                    status = rental.getStatusDisplayName(),
                    onCardClick = {
                        // TODO: Navigate to rental detail
                    }
                )
            }
        }
    }
}



// Import these components in your UserDashboardScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernVehicleCard(
    name: String,
    type: String,
    price: String,
    rating: Float,
    @Suppress("UNUSED_PARAMETER") imageRes: Int? = null,
    isAvailable: Boolean = true,
    features: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    onRentClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Rating badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3CD)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = rating.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF856404)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features
            if (features.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    features.take(3).forEach { feature ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE6FFFA)
                        ) {
                            Text(
                                text = feature,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF234E52)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Price and availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAvailable) Color(0xFFD4EDDA) else Color(0xFFF8D7DA)
                ) {
                    Text(
                        text = if (isAvailable) "Available" else "Rented",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isAvailable) Color(0xFF155724) else Color(0xFF721C24)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Rent button
            Button(
                onClick = onRentClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = isAvailable,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = if (isAvailable) "Rent Now" else "Not Available",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernRentalCard(
    vehicleName: String,
    startDate: String,
    endDate: String,
    totalPrice: String,
    status: String = "Active",
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    Card(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicleName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (status.lowercase()) {
                        "active" -> Color(0xFFD4EDDA)
                        "upcoming" -> Color(0xFFD1ECF1)
                        "completed" -> Color(0xFFE2E3E5)
                        else -> Color(0xFFE2E3E5)
                    }
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = when (status.lowercase()) {
                            "active" -> Color(0xFF155724)
                            "upcoming" -> Color(0xFF0C5460)
                            "completed" -> Color(0xFF383D41)
                            else -> Color(0xFF383D41)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Start Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = startDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "End Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "End Date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = endDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Price:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = totalPrice,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper function to get profile picture URL
private fun getProfilePictureUrl(profilePicture: String?): String {
    return if (!profilePicture.isNullOrEmpty()) {
        "https://beexpress.peachy.icu/uploads/profile_picture/$profilePicture"
    } else {
        "https://via.placeholder.com/150/CCCCCC/FFFFFF?text=No+Image"
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