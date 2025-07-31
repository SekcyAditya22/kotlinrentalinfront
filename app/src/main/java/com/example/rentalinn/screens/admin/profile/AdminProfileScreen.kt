package com.example.rentalinn.screens.admin.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class AdminProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val joinDate: String,
    val photoUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen() {
    var isEditing by remember { mutableStateOf(false) }
    
    // Sample data
    val profile = remember {
        AdminProfile(
            id = "ADM001",
            name = "Admin Rentalin",
            email = "admin@rentalin.com",
            phone = "+62812345678",
            role = "Super Admin",
            joinDate = "2024-01-01",
            photoUrl = null
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    if (profile.photoUrl != null) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = profile.role,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Member since ${profile.joinDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* TODO: Change profile picture */ }
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Picture")
                }
            }
        }

        // Profile Information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(
                        onClick = { isEditing = !isEditing }
                    ) {
                        Icon(
                            if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEditing) "Save" else "Edit")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ProfileField(
                    label = "ID",
                    value = profile.id,
                    icon = Icons.Default.Badge,
                    isEditing = false
                )
                ProfileField(
                    label = "Name",
                    value = profile.name,
                    icon = Icons.Default.Person,
                    isEditing = isEditing
                )
                ProfileField(
                    label = "Email",
                    value = profile.email,
                    icon = Icons.Default.Email,
                    isEditing = isEditing
                )
                ProfileField(
                    label = "Phone",
                    value = profile.phone,
                    icon = Icons.Default.Phone,
                    isEditing = isEditing
                )
            }
        }

        // Security Settings
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* TODO: Change password */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Password")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { /* TODO: Enable 2FA */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enable Two-Factor Authentication")
                }
            }
        }

        // Activity Log
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                ActivityLogItem(
                    action = "Profile updated",
                    time = "2 hours ago",
                    icon = Icons.Default.Edit
                )
                ActivityLogItem(
                    action = "Password changed",
                    time = "2 days ago",
                    icon = Icons.Default.Lock
                )
                ActivityLogItem(
                    action = "Login from new device",
                    time = "5 days ago",
                    icon = Icons.Default.PhoneAndroid
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEditing: Boolean
) {
    var fieldValue by remember { mutableStateOf(value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (isEditing) {
            OutlinedTextField(
                value = fieldValue,
                onValueChange = { fieldValue = it },
                leadingIcon = { Icon(icon, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            ListItem(
                headlineContent = { Text(fieldValue) },
                leadingContent = {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

@Composable
fun ActivityLogItem(
    action: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(action) },
        supportingContent = { Text(time) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp)
    )
} 