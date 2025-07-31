package com.example.rentalinn.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import com.example.rentalinn.viewmodel.AuthViewModel
import com.example.rentalinn.viewmodel.AuthUiState
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import com.example.rentalinn.screens.admin.home.AdminHomeScreen
import com.example.rentalinn.screens.admin.users.UsersScreen
import com.example.rentalinn.screens.admin.vehicle.VehicleLandingScreen
import com.example.rentalinn.screens.admin.verif.AdminVerificationScreen
import com.example.rentalinn.screens.admin.vehicle.AddVehicleScreen
import com.example.rentalinn.screens.admin.vehicle.EditVehicleScreen
import com.example.rentalinn.screens.admin.vehicle.VehicleDetailScreen
import com.example.rentalinn.screens.admin.vehicle.VehicleUnitsManagementScreen
import com.example.rentalinn.screens.admin.rental.AdminRentalManagementScreen
import com.example.rentalinn.screens.admin.rental.AdminRentalDetailScreen
import com.example.rentalinn.screens.admin.chat.AdminChatListScreen
import com.example.rentalinn.screens.admin.chat.AdminChatDetailScreen
import com.example.rentalinn.navigation.Screen

sealed class AdminScreen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Home : AdminScreen(
        route = "admin_home",
        title = "Home",
        icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
    )
    object Users : AdminScreen(
        route = "admin_users",
        title = "Users",
        icon = { Icon(Icons.Default.Group, contentDescription = "Users") }
    )
    object Verification : AdminScreen(
        route = "admin_verification",
        title = "Verification",
        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Verification") }
    )
    object Rental : AdminScreen(
        route = "admin_rental",
        title = "Rental",
        icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Rental") }
    )
    object Chat : AdminScreen(
        route = "admin_chat",
        title = "Chat",
        icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") }
    )
    object Profile : AdminScreen(
        route = "admin_profile",
        title = "Profile",
        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") }
    )
    object AddVehicle : AdminScreen(
        route = "admin_add_vehicle",
        title = "Add Vehicle",
        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add Vehicle") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

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
        // Do nothing - stay in admin dashboard
        // This prevents admin from accidentally going back to login screen
    }

    val handleLogout: () -> Unit = {
        viewModel.logout()
    }

    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home),
        NavigationItem("Users", Icons.Default.Group),
        NavigationItem("Vehicles", Icons.Default.DirectionsCar),
        NavigationItem("Verification", Icons.Default.VerifiedUser),
        NavigationItem("Rentals", Icons.AutoMirrored.Filled.Assignment),
        NavigationItem("Chat", Icons.Default.Chat)
    )

    var showMenu by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rentalin",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )

                // Logout Button
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        handleLogout()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(items[selectedItem].title) },
                    navigationIcon = {
                        IconButton(
                            onClick = { 
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Image(
                                    painter = painterResource(id = com.example.rentalinn.R.drawable.logonjay),
                                    contentDescription = "RentalInn Logo",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = { /* TODO */ },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        showMenu = false
                                        handleLogout()
                                    },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) }
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedItem) {
                    0 -> AdminHomeScreen()
                    1 -> UsersScreen()
                    2 -> {
                        // Create a nested navigation host for the vehicles section
                        NavHost(
                            navController = navController,
                            startDestination = Screen.VehicleLanding.route
                        ) {
                            composable(Screen.VehicleLanding.route) {
                                VehicleLandingScreen(navController = navController)
                            }
                            composable(Screen.AddVehicle.route) {
                                AddVehicleScreen(navController = navController)
                            }
                            composable(
                                route = Screen.EditVehicle.route,
                                arguments = listOf(
                                    navArgument("vehicleId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                                EditVehicleScreen(vehicleId = vehicleId, navController = navController)
                            }
                            composable(
                                route = Screen.VehicleDetail.route,
                                arguments = listOf(
                                    navArgument("vehicleId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                                VehicleDetailScreen(vehicleId = vehicleId, navController = navController)
                            }
                            composable(
                                route = "vehicle_units_management/{vehicleId}",
                                arguments = listOf(
                                    navArgument("vehicleId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: 0
                                VehicleUnitsManagementScreen(vehicleId = vehicleId, navController = navController)
                            }
                        }
                    }
                    3 -> AdminVerificationScreen()
                    4 -> {
                        // Create a nested navigation host for the rentals section
                        NavHost(
                            navController = navController,
                            startDestination = "admin_rental_management"
                        ) {
                            composable("admin_rental_management") {
                                AdminRentalManagementScreen(navController = navController)
                            }
                            composable(
                                route = "admin_rental_detail/{rentalId}",
                                arguments = listOf(
                                    navArgument("rentalId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val rentalId = backStackEntry.arguments?.getString("rentalId") ?: ""
                                AdminRentalDetailScreen(
                                    navController = navController,
                                    rentalId = rentalId
                                )
                            }
                        }
                    }
                    5 -> {
                        // Create a nested navigation host for the chat section
                        NavHost(
                            navController = navController,
                            startDestination = "admin_chat_list"
                        ) {
                            composable("admin_chat_list") {
                                AdminChatListScreen(
                                    onChatClick = { chat ->
                                        navController.navigate("admin_chat_detail/${chat.id}")
                                    }
                                )
                            }
                            composable(
                                route = "admin_chat_detail/{chatId}",
                                arguments = listOf(
                                    navArgument("chatId") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val chatId = backStackEntry.arguments?.getInt("chatId") ?: 0
                                AdminChatDetailWrapper(
                                    chatId = chatId,
                                    onBackClick = {
                                        navController.popBackStack()
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

@Composable
fun AdminChatDetailWrapper(
    chatId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: com.example.rentalinn.viewmodel.AdminChatViewModel = viewModel(
        factory = com.example.rentalinn.viewmodel.AdminChatViewModel.Factory(context)
    )

    val chats by viewModel.chats.collectAsState()

    // Initialize admin chat to load chats
    LaunchedEffect(Unit) {
        viewModel.initializeAdminChat()
    }

    // Find the specific chat
    val chat = chats.find { it.id == chatId }

    if (chat != null) {
        AdminChatDetailScreen(
            chat = chat,
            onBackClick = onBackClick
        )
    } else {
        // Show loading or error state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading chat...")
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RecentActivitiesList() {
    val activities = listOf(
        "New rental request from John Doe" to "2 minutes ago",
        "Payment received for booking #1234" to "15 minutes ago",
        "Vehicle returned by Sarah Smith" to "1 hour ago",
        "New user registration" to "2 hours ago",
        "Maintenance scheduled for Toyota Avanza" to "3 hours ago"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        activities.forEach { (activity, time) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    onMenuClick: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Admin Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            // Notifications
            IconButton(onClick = { /* TODO */ }) {
                BadgedBox(
                    badge = {
                        Badge { Text("3") }
                    }
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
            }
            
            // Profile
            IconButton(onClick = { /* TODO */ }) {
                AsyncImage(
                    model = "https://i.pravatar.cc/100",
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun AdminDashboardContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Quick Stats
        QuickStats()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Revenue Chart
        RevenueChart()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent Activities and Quick Actions in a responsive layout
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 600.dp) {
                // Stack vertically on narrow screens
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RecentActivities(
                        modifier = Modifier.fillMaxWidth()
                    )
                    QuickActions(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Side by side on wider screens
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RecentActivities(
                        modifier = Modifier.weight(1f)
                    )
                    QuickActions(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStats() {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 600.dp) {
            // Stack vertically on narrow screens
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Total Users",
                    value = "1,234",
                    icon = Icons.Outlined.Group,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Active Rentals",
                    value = "56",
                    icon = Icons.Outlined.DirectionsCar,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Revenue",
                    value = "Rp 15.5M",
                    icon = Icons.Outlined.AttachMoney,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    title = "Vehicles",
                    value = "89",
                    icon = Icons.Outlined.CarRental,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Horizontal scrolling on wider screens
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    StatCard(
                        title = "Total Users",
                        value = "1,234",
                        icon = Icons.Outlined.Group,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    StatCard(
                        title = "Active Rentals",
                        value = "56",
                        icon = Icons.Outlined.DirectionsCar,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    StatCard(
                        title = "Revenue",
                        value = "Rp 15.5M",
                        icon = Icons.Outlined.AttachMoney,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    StatCard(
                        title = "Vehicles",
                        value = "89",
                        icon = Icons.Outlined.CarRental,
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

@Composable
fun RevenueChart() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Revenue Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sample data points
            val entries = listOf(
                Entry(0f, 100f),
                Entry(1f, 150f),
                Entry(2f, 120f),
                Entry(3f, 200f),
                Entry(4f, 180f),
                Entry(5f, 250f),
                Entry(6f, 300f)
            )

            val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
            val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
            
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setDrawGridBackground(false)
                        setDrawBorders(false)
                        
                        xAxis.apply {
                            setDrawGridLines(true)
                            setDrawAxisLine(true)
                            textColor = primaryColor
                            gridColor = Color.Gray.copy(alpha = 0.2f).toArgb()
                        }
                        
                        axisLeft.apply {
                            setDrawGridLines(true)
                            setDrawAxisLine(true)
                            textColor = primaryColor
                            gridColor = Color.Gray.copy(alpha = 0.2f).toArgb()
                        }
                        
                        axisRight.isEnabled = false
                        legend.isEnabled = false
                        
                        setBackgroundColor(surfaceColor)
                        
                        val dataSet = LineDataSet(entries, "Revenue").apply {
                            color = primaryColor
                            setCircleColor(primaryColor)
                            lineWidth = 2f
                            circleRadius = 4f
                            setDrawCircleHole(true)
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                        }
                        
                        data = LineData(dataSet)
                        invalidate()
                    }
                }
            )
        }
    }
}

@Composable
fun RecentActivities(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Recent Activities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { index ->
                    ActivityItem(
                        title = when (index) {
                            0 -> "New rental request"
                            1 -> "Payment received"
                            2 -> "Vehicle returned"
                            3 -> "New user registered"
                            else -> "Maintenance scheduled"
                        },
                        time = "5 mins ago",
                        icon = when (index) {
                            0 -> Icons.Outlined.DirectionsCar   
                            1 -> Icons.Outlined.Payment
                            2 -> Icons.Outlined.Assignment
                            3 -> Icons.Outlined.PersonAdd
                            else -> Icons.Outlined.Build
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityItem(
    title: String,
    time: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(40.dp)
                .padding(8.dp)
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun QuickActions(modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = "Add Vehicle",
                    icon = Icons.Outlined.AddCircle,
                    onClick = { /* TODO */ }
                )
                QuickActionButton(
                    text = "Manage Users",
                    icon = Icons.Outlined.Group,
                    onClick = { /* TODO */ }
                )
                QuickActionButton(
                    text = "View Reports",
                    icon = Icons.Outlined.Assessment,
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                
                // Profile Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://i.pravatar.cc/100",
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = "Admin User",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "admin@rentalin.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                
                // Navigation Items
                listOf(
                    AdminScreen.Home,
                    AdminScreen.Users,
                    AdminScreen.Verification,
                    AdminScreen.Rental,
                    AdminScreen.Chat,
                    AdminScreen.Profile,
                    AdminScreen.AddVehicle
                ).forEach { screen ->
                    NavigationDrawerItem(
                        icon = { screen.icon() },
                        label = { Text(screen.title) },
                        selected = false,
                        onClick = { /* TODO */ }
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                
                // Logout Button
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = onLogout,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        content()
    }
} 