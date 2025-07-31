package com.example.rentalinn.screens.admin.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalinn.model.Chat
import com.example.rentalinn.viewmodel.AdminChatViewModel
import kotlinx.coroutines.delay
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatListScreen(
    onChatClick: (Chat) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AdminChatViewModel = viewModel(factory = AdminChatViewModel.Factory(context))
    
    val uiState by viewModel.uiState.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    
    // Initialize when screen loads
    LaunchedEffect(Unit) {
        viewModel.initializeAdminChat()
    }

    // Auto-refresh chats periodically for realtime updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Wait 10 seconds
            if (!connectionState) {
                // If not connected via socket, refresh chats more frequently
                viewModel.refreshChats()
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header
        AdminChatHeader(
            connectionState = connectionState,
            onRefresh = { viewModel.refreshChats() }
        )
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
        
        // Chat list
        if (chats.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No chats available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Users will appear here when they start chatting",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chats) { chat ->
                    AdminChatItem(
                        chat = chat,
                        onClick = { onChatClick(chat) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminChatHeader(
    connectionState: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Admin icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Chat Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (connectionState) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (connectionState) "Realtime • Monitoring" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun AdminChatItem(
    chat: Chat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar with profile picture
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = getProfilePictureUrl(chat.user?.profilePicture),
                    contentDescription = "Profile picture of ${chat.user?.name}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.user?.name?.firstOrNull()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Chat info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.user?.name ?: "Unknown User",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    chat.lastMessage?.let { lastMessage ->
                        Text(
                            text = lastMessage.getFormattedTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage?.message ?: "No messages yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Unread count badge with animation
                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5722)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                
                // Chat status
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Status: ${chat.status.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (chat.status == "active") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
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
