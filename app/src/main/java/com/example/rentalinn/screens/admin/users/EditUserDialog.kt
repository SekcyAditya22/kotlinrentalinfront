package com.example.rentalinn.screens.admin.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rentalinn.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber ?: "") }
    var role by remember { mutableStateOf(user.role) }
    var isVerified by remember { mutableStateOf(user.isVerified) }
    var roleExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit User",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(
                        onClick = { if (!isLoading) onDismiss() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Form fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    }
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null
                        )
                    }
                )

                // Role selection
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it },
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Admin") },
                            onClick = { 
                                role = "Admin"
                                roleExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("User") },
                            onClick = { 
                                role = "User"
                                roleExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Verification toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Verified User",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = isVerified,
                        onCheckedChange = { isVerified = it },
                        enabled = !isLoading
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (!isLoading) onDismiss() },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val updatedUser = user.copy(
                                name = name,
                                email = email,
                                phoneNumber = if (phoneNumber.isNotEmpty()) phoneNumber else null,
                                role = role,
                                isVerified = isVerified
                            )
                            onConfirm(updatedUser)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && name.isNotEmpty() && email.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
} 