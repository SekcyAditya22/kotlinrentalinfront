package com.example.rentalinn.screens.user.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rentalinn.utils.UserSessionManager
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalinn.viewmodel.AuthViewModel
import com.example.rentalinn.viewmodel.AuthUiState
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.rentalinn.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Handle auth state changes
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                if (state.message?.contains("Password changed") == true) {
                    showChangePasswordDialog = false
                    authViewModel.resetState()
                } else if (state.message?.contains("Account deleted") == true) {
                    showDeleteAccountDialog = false
                    onLogout() // Logout after successful account deletion
                }
            }
            else -> {}
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Section at the top
        ProfileHeaderSection(
            onProfileClick = {
                navController?.navigate("user_profile")
            }
        )

        // Privacy & Security
        SettingsSection(title = "Privacy & Security") {
            SettingsItem(
                title = "Change Password",
                description = "Update your password",
                icon = Icons.Default.Lock,
                onClick = { showChangePasswordDialog = true }
            )
            
            SettingsItem(
                title = "Privacy Policy",
                description = "Read our privacy policy",
                icon = Icons.Default.Security,
                onClick = { /* TODO: Open privacy policy */ }
            )
            
            SettingsItem(
                title = "Terms of Service",
                description = "Read our terms of service",
                icon = Icons.Default.Description,
                onClick = { /* TODO: Open terms of service */ }
            )
        }
        
        // Support
        SettingsSection(title = "Support") {
            SettingsItem(
                title = "Help Center",
                description = "Get help with Rentalin",
                icon = Icons.AutoMirrored.Filled.Help,
                onClick = {
                    // Navigate to chat screen
                    navController?.navigate("user_chat")
                }
            )

            SettingsItem(
                title = "Contact Us",
                description = "Get in touch with our team",
                icon = Icons.Default.Email,
                onClick = {
                    // Open email intent
                    openEmailIntent(context, "xeroxaviera@gmail.com")
                }
            )
            
            SettingsItem(
                title = "About",
                description = "Learn more about Rentalin",
                icon = Icons.Default.Info,
                onClick = { showAboutDialog = true }
            )
        }
        
        // Account
        SettingsSection(title = "Account") {
            SettingsItem(
                title = "Delete Account",
                description = "Permanently delete your account",
                icon = Icons.Default.DeleteForever,
                onClick = { showDeleteAccountDialog = true },
                textColor = MaterialTheme.colorScheme.error
            )
            
            SettingsItem(
                title = "Logout",
                description = "Sign out of your account",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                onClick = { showLogoutDialog = true },
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = {
                showChangePasswordDialog = false
                authViewModel.resetState()
            },
            onPasswordChanged = { currentPassword, newPassword ->
                authViewModel.changePassword(currentPassword, newPassword)
            },
            isLoading = authUiState is AuthUiState.Loading,
            errorMessage = (authUiState as? AuthUiState.Error)?.message ?: ""
        )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = {
                showDeleteAccountDialog = false
                authViewModel.resetState()
            },
            onAccountDeleted = {
                authViewModel.deleteAccount()
            },
            isLoading = authUiState is AuthUiState.Loading,
            errorMessage = (authUiState as? AuthUiState.Error)?.message ?: ""
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    ListItem(
        headlineContent = { 
            Text(
                text = title,
                color = textColor
            )
        },
        supportingContent = { 
            Text(
                text = description,
                color = textColor.copy(alpha = 0.7f)
            )
        },
        leadingContent = { 
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor
            )
        },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.7f)
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { 
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onPasswordChanged: (String, String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String = ""
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage.isNotEmpty() || localErrorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage.ifEmpty { localErrorMessage },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Current Password
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                            Icon(
                                imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCurrentPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                // New Password
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNewPassword = !showNewPassword }) {
                            Icon(
                                imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showNewPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )

                if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPassword.isEmpty() -> localErrorMessage = "Please enter current password"
                        newPassword.isEmpty() -> localErrorMessage = "Please enter new password"
                        newPassword.length < 6 -> localErrorMessage = "New password must be at least 6 characters"
                        newPassword != confirmPassword -> localErrorMessage = "Passwords do not match"
                        else -> {
                            localErrorMessage = ""
                            onPasswordChanged(currentPassword, newPassword)
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Change Password")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onAccountDeleted: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String = ""
) {
    var confirmationText by remember { mutableStateOf("") }
    var localErrorMessage by remember { mutableStateOf("") }
    val requiredText = "Delete Yakin Sekali"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Account",
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Apakah benar akan menghapus akun?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "This action cannot be undone. All your data will be permanently deleted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Type \"$requiredText\" to confirm:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    label = { Text("Confirmation") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmationText.isNotEmpty() && confirmationText != requiredText
                )

                if (errorMessage.isNotEmpty() || localErrorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage.ifEmpty { localErrorMessage },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (confirmationText == requiredText) {
                        localErrorMessage = ""
                        onAccountDeleted()
                    } else {
                        localErrorMessage = "Please type the exact confirmation text"
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Delete Account")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileHeaderSection(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Get user session manager
    val context = androidx.compose.ui.platform.LocalContext.current
    val userSessionManager = remember { UserSessionManager.getInstance(context) }

    // User data states
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userProfilePicture by remember { mutableStateOf<String?>(null) }
    var userInitials by remember { mutableStateOf("U") }
    var userIsVerified by remember { mutableStateOf(false) }

    // Load user data
    LaunchedEffect(Unit) {
        userName = userSessionManager.getCurrentUserName() ?: "Unknown User"
        userEmail = userSessionManager.getCurrentUserEmail() ?: "No Email"
        userProfilePicture = userSessionManager.getCurrentUserProfilePicture()
        userInitials = userSessionManager.getUserInitials()
        userIsVerified = userSessionManager.isCurrentUserVerified()
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onProfileClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Picture with verification badge
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
                                    .background(MaterialTheme.colorScheme.surface),
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
                                    .background(MaterialTheme.colorScheme.primary),
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

                        // Verification badge
                        if (userIsVerified) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Tap to view profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Go to Profile",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Helper function to open email intent
fun openEmailIntent(context: Context, emailAddress: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            putExtra(Intent.EXTRA_SUBJECT, "Rentalin Support")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to general email intent
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, "Rentalin Support")
            }
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            // Handle error - could show a toast or snackbar
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logonjay),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "About Rentalin",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Rentalin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Your trusted vehicle rental companion",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Features
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Features:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val features = listOf(
                        "🚗 Easy vehicle rental booking",
                        "💳 Secure payment integration",
                        "📱 Fast",
                        "💬 24/7 customer support",
                        "📍 Share Location",
                        "⭐ Startup New"
                    )

                    features.forEach { feature ->
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Developer Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Developed by:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Mahasiswa Amikom",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "© 2024 Rentalin. All rights reserved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("OK")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// Helper function to get profile picture URL
private fun getProfilePictureUrl(profilePicture: String?): String {
    return if (!profilePicture.isNullOrEmpty()) {
        "https://beexpress.peachy.icu/uploads/profile_picture/$profilePicture"
    } else {
        "https://via.placeholder.com/150/CCCCCC/FFFFFF?text=No+Image"
    }
}