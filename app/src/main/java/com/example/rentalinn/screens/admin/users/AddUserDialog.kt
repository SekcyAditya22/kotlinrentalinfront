package com.example.rentalinn.screens.admin.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rentalinn.model.CreateUserRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (CreateUserRequest) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
    var isVerified by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Validation states
    val isNameValid = name.length >= 2
    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 8 && 
        password.any { it.isLowerCase() } && 
        password.any { it.isUpperCase() } && 
        password.any { it.isDigit() }
    val isConfirmPasswordValid = password == confirmPassword && confirmPassword.isNotEmpty()
    val isFormValid = isNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid

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
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header - Fixed at top
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New User",
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

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Form fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        isError = name.isNotEmpty() && !isNameValid,
                        supportingText = {
                            if (name.isNotEmpty() && !isNameValid) {
                                Text("Name must be at least 2 characters")
                            }
                        },
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
                        label = { Text("Email *") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        isError = email.isNotEmpty() && !isEmailValid,
                        supportingText = {
                            if (email.isNotEmpty() && !isEmailValid) {
                                Text("Please enter a valid email address")
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        }
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password *") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = password.isNotEmpty() && !isPasswordValid,
                        supportingText = {
                            if (password.isNotEmpty() && !isPasswordValid) {
                                Text("Password must be at least 8 characters with uppercase, lowercase, and number")
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password *") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        singleLine = true,
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = confirmPassword.isNotEmpty() && !isConfirmPasswordValid,
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && !isConfirmPasswordValid) {
                                Text("Passwords do not match")
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                                )
                            }
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
                            value = role.capitalize(),
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
                                    role = "admin"
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
                                    role = "user"
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
                }

                // Action buttons - Fixed at bottom
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = { if (!isLoading) onDismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    
                    // Create Button
                    Button(
                        onClick = {
                            val request = CreateUserRequest(
                                name = name,
                                email = email,
                                password = password,
                                phoneNumber = if (phoneNumber.isNotEmpty()) phoneNumber else null,
                                role = role,
                                isVerified = isVerified
                            )
                            onConfirm(request)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && isFormValid,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Create User",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 