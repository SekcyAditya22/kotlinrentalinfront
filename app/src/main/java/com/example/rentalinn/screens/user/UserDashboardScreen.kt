
package com.example.rentalinn.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import com.example.rentalinn.screens.user.chat.ChatScreen
import com.example.rentalinn.screens.user.home.UserHomeScreen
import com.example.rentalinn.screens.user.profile.UserProfileScreen
import com.example.rentalinn.screens.user.rental.UserRentalScreen
import com.example.rentalinn.screens.user.rental.RentalBookingScreen
import com.example.rentalinn.screens.user.rental.LocationPickerScreen
import com.example.rentalinn.screens.user.payment.PaymentScreen
import com.example.rentalinn.screens.user.payment.PaymentSuccessScreen
import com.example.rentalinn.screens.user.payment.PaymentPendingScreen
import com.example.rentalinn.screens.user.payment.PaymentFailedScreen
import com.example.rentalinn.screens.user.settings.UserSettingsScreen
import com.example.rentalinn.screens.user.transaction.TransactionScreen
import com.example.rentalinn.viewmodel.AuthViewModel
import com.example.rentalinn.viewmodel.AuthUiState
import com.example.rentalinn.utils.UserSessionManager
import com.example.rentalinn.R

sealed class UserScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val isHomeIcon: Boolean = false
) {
    object Rental : UserScreen("user_rental", "Rental", Icons.Default.DirectionsCar)
    object Transaction : UserScreen("user_transaction", "Transaksi", Icons.Default.Receipt)
    object Home : UserScreen("user_home", "Home", Icons.Default.Home, isHomeIcon = true)
    object Chat : UserScreen("user_chat", "Chat", Icons.Default.Chat)
    object Settings : UserScreen("user_settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val userNavController = rememberNavController()
    val currentRoute = userNavController.currentBackStackEntryAsState().value?.destination?.route
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Get user session manager
    val context = androidx.compose.ui.platform.LocalContext.current
    val userSessionManager = remember { UserSessionManager.getInstance(context) }
    val currentUser by userSessionManager.currentUser.collectAsState(initial = null)

    // Handle logout state change
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success && (uiState as AuthUiState.Success).token == null) {
            // Only navigate to login if this is a result of explicit logout
            // Check if we're in a logout state by checking if user data is also cleared
            if ((uiState as AuthUiState.Success).user == null) {
                onLogout()
            }
        }
    }

    // Handle back button - prevent going back to login
    BackHandler {
        // Do nothing - stay in dashboard
        // This prevents users from accidentally going back to login screen
    }

    val handleLogout: () -> Unit = {
        viewModel.logout()
    }

    val items = listOf(
        UserScreen.Rental,
        UserScreen.Transaction,
        UserScreen.Home,
        UserScreen.Chat,
        UserScreen.Settings
    )

    // Update gradient colors to use theme colors
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = items.find { it.route == currentRoute }?.title ?: "Home",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        currentUser?.let { user ->
                            Text(
                                text = "Welcome, ${user.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userNavController.navigate("user_settings") {
                                popUpTo(userNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logonjay),
                            contentDescription = "Go to Settings",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(2.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(
                                    if (screen.isHomeIcon) 32.dp else 24.dp
                                )
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                style = if (screen.isHomeIcon)
                                    MaterialTheme.typography.labelMedium
                                else
                                    MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = currentRoute == screen.route,
                        onClick = {
                            userNavController.navigate(screen.route) {
                                popUpTo(userNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = userNavController,
            startDestination = UserScreen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(UserScreen.Home.route) {
                UserHomeScreen(navController = userNavController)
            }
            composable(UserScreen.Rental.route) {
                UserRentalScreen(navController = userNavController)
            }
            composable(UserScreen.Transaction.route) {
                TransactionScreen(navController = userNavController)
            }
            composable(UserScreen.Chat.route) {
                ChatScreen()
            }
            composable(UserScreen.Settings.route) {
                UserSettingsScreen(
                    navController = userNavController,
                    onLogout = handleLogout
                )
            }
            composable("user_profile") {
                UserProfileScreen(
                    onLogout = handleLogout
                )
            }

            // Rental & Payment routes
            composable(
                route = "rental_booking/{vehicleId}",
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                RentalBookingScreen(
                    vehicleId = vehicleId,
                    navController = userNavController
                )
            }

            composable(
                route = "payment/{rentalId}",
                arguments = listOf(
                    navArgument("rentalId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val rentalId = backStackEntry.arguments?.getInt("rentalId") ?: 0
                PaymentScreen(
                    rentalId = rentalId,
                    navController = userNavController
                )
            }

            // Location Picker routes
            composable(
                route = "location_picker/{locationType}",
                arguments = listOf(
                    navArgument("locationType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val locationType = backStackEntry.arguments?.getString("locationType") ?: "pickup"
                LocationPickerScreen(
                    navController = userNavController,
                    locationType = locationType
                )
            }

            // Payment Result Routes
            composable(
                route = "payment_success/{rentalId}",
                arguments = listOf(
                    navArgument("rentalId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val rentalId = backStackEntry.arguments?.getInt("rentalId") ?: 0
                PaymentSuccessScreen(
                    rentalId = rentalId,
                    navController = userNavController
                )
            }

            composable(
                route = "payment_pending/{rentalId}",
                arguments = listOf(
                    navArgument("rentalId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val rentalId = backStackEntry.arguments?.getInt("rentalId") ?: 0
                PaymentPendingScreen(
                    rentalId = rentalId,
                    navController = userNavController
                )
            }

            composable(
                route = "payment_failed/{rentalId}",
                arguments = listOf(
                    navArgument("rentalId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val rentalId = backStackEntry.arguments?.getInt("rentalId") ?: 0
                PaymentFailedScreen(
                    rentalId = rentalId,
                    navController = userNavController
                )
            }
        }
    }
}

// Removed duplicate ModernVehicleCard - using the one from ui.components package

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = vehicleName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val (statusColor, statusTextColor) = when (status.lowercase()) {
                    "active" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                    "upcoming" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
