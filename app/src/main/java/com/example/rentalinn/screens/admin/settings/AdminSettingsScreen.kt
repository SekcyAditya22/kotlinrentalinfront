package com.example.rentalinn.screens.admin.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen() {
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("English") }
    var currency by remember { mutableStateOf("IDR") }
    var dateFormat by remember { mutableStateOf("DD/MM/YYYY") }
    var timeFormat by remember { mutableStateOf("24-hour") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Appearance Settings
        SettingsSection(title = "Appearance") {
            SettingsSwitch(
                title = "Dark Mode",
                description = "Enable dark theme for the app",
                icon = Icons.Default.DarkMode,
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )

            SettingsDropdown(
                title = "Language",
                description = "Choose your preferred language",
                icon = Icons.Default.Language,
                options = listOf("English", "Indonesian"),
                selectedOption = language,
                onOptionSelected = { language = it }
            )
        }

        // Notification Settings
        SettingsSection(title = "Notifications") {
            SettingsSwitch(
                title = "Enable Notifications",
                description = "Receive notifications about important updates",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            if (notificationsEnabled) {
                SettingsSwitch(
                    title = "Email Notifications",
                    description = "Receive notifications via email",
                    icon = Icons.Default.Email,
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it }
                )

                SettingsSwitch(
                    title = "Push Notifications",
                    description = "Receive push notifications on your device",
                    icon = Icons.Default.PhoneAndroid,
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
            }
        }

        // Regional Settings
        SettingsSection(title = "Regional") {
            SettingsDropdown(
                title = "Currency",
                description = "Choose your preferred currency",
                icon = Icons.Default.AttachMoney,
                options = listOf("IDR", "USD", "EUR"),
                selectedOption = currency,
                onOptionSelected = { currency = it }
            )

            SettingsDropdown(
                title = "Date Format",
                description = "Choose your preferred date format",
                icon = Icons.Default.DateRange,
                options = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD"),
                selectedOption = dateFormat,
                onOptionSelected = { dateFormat = it }
            )

            SettingsDropdown(
                title = "Time Format",
                description = "Choose your preferred time format",
                icon = Icons.Default.Schedule,
                options = listOf("12-hour", "24-hour"),
                selectedOption = timeFormat,
                onOptionSelected = { timeFormat = it }
            )
        }

        // Backup & Sync
        SettingsSection(title = "Backup & Sync") {
            SettingsButton(
                title = "Backup Data",
                description = "Create a backup of your data",
                icon = Icons.Default.Backup,
                onClick = { /* TODO: Handle backup */ }
            )

            SettingsButton(
                title = "Restore Data",
                description = "Restore data from a backup",
                icon = Icons.Default.Restore,
                onClick = { /* TODO: Handle restore */ }
            )
        }

        // About & Help
        SettingsSection(title = "About & Help") {
            SettingsButton(
                title = "Help Center",
                description = "Get help and support",
                icon = Icons.Default.Help,
                onClick = { showHelpDialog = true }
            )

            SettingsButton(
                title = "Privacy Policy",
                description = "Read our privacy policy",
                icon = Icons.Default.PrivacyTip,
                onClick = { /* TODO: Open privacy policy */ }
            )

            SettingsButton(
                title = "Terms of Service",
                description = "Read our terms of service",
                icon = Icons.Default.Description,
                onClick = { /* TODO: Open terms of service */ }
            )

            SettingsButton(
                title = "App Version",
                description = "Version 1.0.0",
                icon = Icons.Default.Info,
                onClick = { /* TODO: Show version info */ }
            )
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    listOf("English", "Indonesian", "Japanese", "Korean").forEach { lang ->
                        TextButton(
                            onClick = {
                                language = lang
                                showLanguageDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(lang)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support") },
            text = {
                Text("For assistance, please contact our support team:\n\n" +
                        "Email: support@rentalin.com\n" +
                        "Phone: +62 812-3456-7890\n" +
                        "Available: Monday - Friday, 9 AM - 5 PM")
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

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
                icon,
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
    Divider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedOption,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    modifier = Modifier
                        .menuAnchor()
                        .width(120.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    )
    Divider()
}

@Composable
fun SettingsButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    Divider()
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
} 