package com.example.rentalinn.screens.user.chat

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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalinn.model.ChatMessage
import com.example.rentalinn.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(context))
    
    val uiState by viewModel.uiState.collectAsState()
    val currentChat by viewModel.currentChat.collectAsState()
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
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    // Initialize chat when screen loads
    LaunchedEffect(Unit) {
        viewModel.initializeChat()
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                        Color(0xFF2196F3).copy(alpha = 0.05f),
                        Color(0xFF9C27B0).copy(alpha = 0.02f)
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        x = 400f + animatedOffset,
                        y = 600f + animatedOffset
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Modern Header
            ModernChatHeader(
                connectionState = connectionState,
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
                        ModernChatMessageItem(
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
                enabled = connectionState && currentChat != null
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
    connectionState: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val headerHeight by animateDpAsState(
        targetValue = if (connectionState) 80.dp else 90.dp,
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
                            Color(0xFF4CAF50),
                            Color(0xFF2196F3),
                            Color(0xFF9C27B0)
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced support agent avatar with pulse animation
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
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF00E676),
                                        Color(0xFF00BCD4),
                                        Color(0xFF3F51B5)
                                    )
                                )
                            )
                            .border(
                                3.dp,
                                Color.White.copy(alpha = 0.5f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SupportAgent,
                            contentDescription = "Support",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "🎧 Customer Support",
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = statusAlpha))
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = if (connectionState) "🚀 Online • Ready to help" else "📡 Reconnecting...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Connecting to support...",
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
fun ModernChatMessageItem(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .graphicsLayer {
                    shadowElevation = 8.dp.toPx()
                },
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isFromCurrentUser) 20.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromCurrentUser) {
                    Color(0xFF4CAF50)
                } else {
                    Color.White
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Sender name (for admin messages)
                if (!isFromCurrentUser && message.sender != null) {
                    Text(
                        text = "👨‍💼 ${message.sender.name}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromCurrentUser) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.getFormattedTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromCurrentUser)
                            Color.White.copy(alpha = 0.7f)
                        else
                            Color.Gray.copy(alpha = 0.7f)
                    )

                    // Enhanced read indicator for sent messages
                    if (isFromCurrentUser && message.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun ModernTypingIndicator(
    typingUsers: List<String>,
    modifier: Modifier = Modifier
) {
    if (typingUsers.isNotEmpty()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Card(
                modifier = Modifier.widthIn(max = 200.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enhanced animated dots
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
                                .background(Color(0xFF4CAF50).copy(alpha = alpha))
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Support is typing...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                        if (enabled) "💬 Type your message..." else "🔄 Connecting...",
                        color = Color.Gray.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                enabled = enabled,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            )
            
            // Enhanced animated send button
            val sendButtonScale by animateFloatAsState(
                targetValue = if (messageText.isNotBlank() && enabled) 1f else 0.8f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "send_scale"
            )
            
            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(48.dp)
                    .scale(sendButtonScale),
                containerColor = if (enabled && messageText.isNotBlank())
                    Color(0xFF4CAF50)
                else
                    Color(0xFF4CAF50).copy(alpha = 0.5f),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
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
