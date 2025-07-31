package com.example.rentalinn.screens.admin.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalinn.model.Chat
import com.example.rentalinn.model.ChatMessage
import com.example.rentalinn.repository.ChatRepository
import com.example.rentalinn.viewmodel.AdminChatDetailViewModel
import kotlinx.coroutines.delay
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatDetailScreen(
    chat: Chat,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: AdminChatDetailViewModel = viewModel(
        factory = AdminChatDetailViewModel.Factory(context, chat.id)
    )

    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    // Initialize chat when screen loads
    LaunchedEffect(chat.id) {
        viewModel.initializeChatDetail(chat.id)
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle typing
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty()) {
            viewModel.startTyping()
        } else {
            viewModel.stopTyping()
        }
    }

    // Auto-refresh messages periodically for fallback
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (!connectionState) {
                viewModel.refreshMessages()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF667eea).copy(alpha = 0.1f),
                        Color(0xFF764ba2).copy(alpha = 0.05f),
                        Color(0xFF1e3c72).copy(alpha = 0.02f)
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        x = 500f + animatedOffset,
                        y = 500f + animatedOffset
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Modern Header with Glassmorphism
            ModernChatHeader(
                chat = chat,
                connectionState = connectionState,
                onBackClick = onBackClick,
                onRefresh = { viewModel.refreshMessages() }
            )

            // Loading indicator with modern animation
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ModernLoadingIndicator()
            }

            // Error message with slide animation
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                uiState.error?.let { error ->
                    ModernErrorCard(
                        error = error,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }

            // Chat messages with enhanced styling
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally() + fadeIn(),
                        exit = slideOutHorizontally() + fadeOut()
                    ) {
                        EnhancedChatMessageItem(
                            message = message,
                            isFromCurrentUser = currentUser?.let { 
                                message.isFromCurrentUser(it.id) 
                            } ?: false
                        )
                    }
                }

                // Enhanced typing indicator
                if (typingUsers.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            ModernTypingIndicator(typingUsers = typingUsers)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enhanced message input
            ModernMessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = connectionState
            )
        }

        // Floating connection status indicator
        FloatingConnectionStatus(
            connectionState = connectionState,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 16.dp)
        )
    }
}

@Composable
fun ModernChatHeader(
    chat: Chat,
    connectionState: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headerHeight by animateDpAsState(
        targetValue = if (connectionState) 80.dp else 90.dp, // Reduced from 120dp/140dp
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "header_height"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2),
                            Color(0xFF1e3c72)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        ) {
            // Glassmorphism overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = 0.1f)
                    )
                    .blur(1.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), // Reduced from 24.dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated back button
                val backButtonScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "back_scale"
                )

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .scale(backButtonScale)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp)) // Reduced from 16.dp

                // Enhanced user avatar with pulse animation
                val pulseAnimation = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseAnimation.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(
                            3.dp,
                            Color.White.copy(alpha = 0.5f),
                            CircleShape
                        ),
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
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFFD700),
                                                Color(0xFFFF6B6B),
                                                Color(0xFF4ECDC4)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFFD700),
                                                Color(0xFFFF6B6B),
                                                Color(0xFF4ECDC4)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.user?.name?.firstOrNull()?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp)) // Reduced from 16.dp

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = chat.user?.name ?: "Unknown User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Animated connection status
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = if (connectionState) Color(0xFF4CAF50) else Color(0xFFF44336)
                        val statusAnimation = rememberInfiniteTransition(label = "status")
                        val statusAlpha by statusAnimation.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "status_alpha"
                        )

                        Box(
                            modifier = Modifier
                                .size(8.dp) // Reduced from 10.dp
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = statusAlpha))
                        )

                        Spacer(modifier = Modifier.width(6.dp)) // Reduced from 8.dp

                        Text(
                            text = if (connectionState) "🚀 Realtime Active" else "📡 Reconnecting...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Enhanced status badge
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "✨ ${chat.status.uppercase()}",
                            style = MaterialTheme.typography.labelSmall, // Changed from labelMedium
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp) // Reduced padding
                        )
                    }
                }

                // Animated refresh button
                val refreshRotation by animateFloatAsState(
                    targetValue = if (connectionState) 0f else 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing)
                    ),
                    label = "refresh_rotation"
                )

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .rotate(refreshRotation)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = Color(0xFF667eea)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Loading messages...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ModernErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color(0xFFE57373),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                color = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
fun EnhancedChatMessageItem(
    message: ChatMessage,
    isFromCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isFromCurrentUser) 20.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromCurrentUser) {
                    Color(0xFF667eea)
                } else {
                    Color.White
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .graphicsLayer {
                    shadowElevation = 8.dp.toPx()
                }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = message.message,
                    color = if (isFromCurrentUser) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.getFormattedTime(),
                    color = if (isFromCurrentUser)
                        Color.White.copy(alpha = 0.7f)
                    else
                        Color.Gray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun ModernTypingIndicator(typingUsers: List<String>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.widthIn(max = 200.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated dots
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "typing_$index")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_alpha_$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = alpha))
                )
                if (index < 2) Spacer(modifier = Modifier.width(4.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${typingUsers.first()} is typing...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ModernMessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                placeholder = { 
                    Text(
                        "Type your message...",
                        color = Color.Gray.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                enabled = enabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            )
            
            // Animated send button
            val sendButtonScale by animateFloatAsState(
                targetValue = if (messageText.isNotBlank()) 1f else 0.8f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "send_scale"
            )
            
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(48.dp)
                    .scale(sendButtonScale),
                containerColor = Color(0xFF667eea),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingConnectionStatus(
    connectionState: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !connectionState,
        enter = slideInHorizontally() + fadeIn(),
        exit = slideOutHorizontally() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF44336)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.WifiOff,
                    contentDescription = "Offline",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Offline",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
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
