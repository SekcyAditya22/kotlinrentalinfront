package com.example.rentalinn.screens.admin.users

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalinn.model.User
import com.example.rentalinn.model.CreateUserRequest
import com.example.rentalinn.viewmodel.UserViewModel
import android.widget.Toast
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UserViewModel = viewModel(factory = UserViewModel.Factory(LocalContext.current))
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var isRefreshing by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf<User?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }
    val filters = listOf("All", "Admin", "User")
    val context = LocalContext.current

    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    // Calculate filtered users only when necessary
    val filteredUsers by remember(users, searchQuery, selectedFilter) {
        derivedStateOf {
            users.filter { user ->
                val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
                    user.email.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (selectedFilter) {
                    "Admin" -> user.role == "Admin"
                    "User" -> user.role == "User"
                    else -> true
                }
                matchesSearch && matchesFilter
            }
        }
    }

    // Error handling
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    // Success message handling & close delete dialog after delete
    LaunchedEffect(successMessage, isLoading, pendingDelete) {
        if (pendingDelete && !isLoading && successMessage?.contains("deleted", ignoreCase = true) == true) {
            showDeleteDialog = null
            pendingDelete = false
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        } else if (!pendingDelete && successMessage != null) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    // Refresh handling with debounce
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.loadUsers(forceRefresh = true)
            delay(500) // Reduced minimum refresh time
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = rememberLazyListState()
        ) {
            item(key = "header") {
                Spacer(modifier = Modifier.height(16.dp))
                EnhancedHeaderSection()
            }
            
            item(key = "stats") { 
                EnhancedUserStatistics(
                    isRefreshing = isRefreshing,
                    onRefresh = { isRefreshing = true },
                    onAddUser = { showAddDialog = true },
                    adminCount = users.count { it.role == "Admin" }.toString(),
                    userCount = users.count { it.role == "User" }.toString()
                )
            }
            
            item(key = "search") {
                EnhancedSearchAndFilterSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    filters = filters,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            
            // Show loading indicator
            if (isLoading && users.isEmpty()) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Display users with keys for better performance
            items(
                items = filteredUsers,
                key = { it.id }
            ) { user ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    EnhancedUserListItem(
                        user = user,
                        onEditClick = { showEditDialog = user },
                        onDeleteClick = { showDeleteDialog = user }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Show loading overlay for edit/delete operations
        if (isLoading && users.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Processing...")
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.createUser(request) {
                    showAddDialog = false
                }
            },
            isLoading = isLoading
        )
    }

    // Edit Dialog
    showEditDialog?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { showEditDialog = null },
            onConfirm = { updatedUser ->
                viewModel.updateUser(updatedUser) {
                    showEditDialog = null
                }
            },
            isLoading = isLoading
        )
    }

    // Delete Dialog
    showDeleteDialog?.let { user ->
        DeleteConfirmationDialog(
            user = user,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                pendingDelete = true
                viewModel.deleteUser(user.id)
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun EnhancedHeaderSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title and Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with gradient background
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title and Subtitle
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Users Management",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Manage and monitor user activities",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedUserStatistics(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onAddUser: () -> Unit,
    adminCount: String,
    userCount: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnhancedStatCard(
                title = "Total Admin",
                value = adminCount,
                change = "+2",
                icon = Icons.Default.AdminPanelSettings,
                gradient = listOf(
                    Color(0xFF667eea),
                    Color(0xFF764ba2)
                ),
                modifier = Modifier.weight(1f)
            )
            EnhancedStatCard(
                title = "Total Users",
                value = userCount,
                change = "+12%",
                icon = Icons.Default.Group,
                gradient = listOf(
                    Color(0xFF11998e),
                    Color(0xFF38ef7d)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add User Button (Takes more space)
            Surface(
                onClick = onAddUser,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(5.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add New User",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Refresh Button
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                onClick = onRefresh
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = if (isRefreshing) {
                            Modifier
                                .size(24.dp)
                                .animateContentSize()
                        } else Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    filters: List<String>,
    onFilterSelected: (String) -> Unit
) {
        Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
        ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search users by name or email...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { onFilterSelected(filter) },
                        label = {
                            Text(
                                text = filter,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedStatCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(animationSpec = tween(600, easing = EaseOutBack)) + fadeIn()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(gradient)
                            )
                    )

                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(gradient.map { it.copy(alpha = 0.2f) })
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = gradient.first(),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = change,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedUserListItem(
    user: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
                // Avatar with verification badge
                Box {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = getProfilePictureUrl(user.profilePicture),
                            contentDescription = "Profile picture of ${user.name}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            error = {
                                // Show initials when image fails to load
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.split(" ")
                                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                            .take(2)
                                            .joinToString(""),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },

                        )
                    }

                    // Online indicator
                    if (user.isOnline()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    // Verification badge
                    if (user.isVerified) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(12.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }

                // User Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = user.getFormattedLastActive(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

                // Role Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (user.role) {
                        "Admin" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else -> Color(0xFF2196F3).copy(alpha = 0.1f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (user.role) {
                                        "Admin" -> Color(0xFF4CAF50)
                                        else -> Color(0xFF2196F3)
                                    }
                                )
                        )
                        Text(
                            text = user.role.capitalize(),
                            style = MaterialTheme.typography.labelMedium,
                            color = when (user.role) {
                                "Admin" -> Color(0xFF4CAF50)
                                else -> Color(0xFF2196F3)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action Buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF44336).copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expandable details section
            AnimatedContent(
                targetState = isExpanded,
                label = "expand_details"
            ) { expanded ->
                if (expanded) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Joined",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = user.getFormattedJoinDate(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Status",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (user.isVerified) "Verified" else "Unverified",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (user.isVerified) Color(0xFF4CAF50) else Color(0xFFFFA000)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Phone",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = user.phoneNumber ?: "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
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