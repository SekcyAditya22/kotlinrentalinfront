package com.example.rentalinn.screens.admin.rental

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

data class Rental(
    val id: String,
    val customerName: String,
    val vehicleName: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String,
    val paymentStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Sample data
    val rentals = remember {
        listOf(
            Rental("1", "John Doe", "Toyota Avanza", "2024-03-20", "2024-03-23", 900000.0, "Active", "Paid"),
            Rental("2", "Jane Smith", "Honda Civic", "2024-03-19", "2024-03-22", 1500000.0, "Active", "Pending"),
            Rental("3", "Mike Johnson", "Suzuki Ertiga", "2024-03-15", "2024-03-18", 1050000.0, "Completed", "Paid"),
            Rental("4", "Sarah Wilson", "Toyota Rush", "2024-03-10", "2024-03-13", 1200000.0, "Completed", "Paid")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search rentals...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Statistics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatisticCard(
                title = "Active Rentals",
                value = rentals.count { it.status == "Active" }.toString(),
                icon = Icons.Default.DirectionsCar,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                title = "Total Revenue",
                value = formatCurrency(rentals.sumOf { it.totalPrice }),
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                title = "Pending Payments",
                value = rentals.count { it.paymentStatus == "Pending" }.toString(),
                icon = Icons.Default.Payment,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active Rentals") },
                icon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Rental History") },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        // Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp)
        ) {
            Column {
                // Table Header
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Customer", modifier = Modifier.weight(1f))
                        Text("Vehicle", modifier = Modifier.weight(1f))
                        Text("Duration", modifier = Modifier.weight(1f))
                        Text("Total", modifier = Modifier.weight(0.5f))
                        Text("Status", modifier = Modifier.weight(0.5f))
                        Text("Actions", modifier = Modifier.weight(0.5f))
                    }
                }

                // Rentals List
                LazyColumn {
                    val filteredRentals = if (selectedTab == 0) {
                        rentals.filter { it.status == "Active" }
                    } else {
                        rentals.filter { it.status == "Completed" }
                    }
                    
                    items(filteredRentals) { rental ->
                        RentalListItem(rental)
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
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
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun RentalListItem(rental: Rental) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(rental.customerName, style = MaterialTheme.typography.bodyLarge)
            Text("ID: ${rental.id}", style = MaterialTheme.typography.bodySmall)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(rental.vehicleName, style = MaterialTheme.typography.bodyLarge)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("${rental.startDate} to", style = MaterialTheme.typography.bodyMedium)
            Text(rental.endDate, style = MaterialTheme.typography.bodyMedium)
        }
        Text(formatCurrency(rental.totalPrice), modifier = Modifier.weight(0.5f))
        
        // Status Chip
        Surface(
            color = when (rental.status) {
                "Active" -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            }.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(0.5f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = rental.status,
                    color = when (rental.status) {
                        "Active" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
                Text(
                    text = rental.paymentStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (rental.paymentStatus) {
                        "Paid" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        // Actions
        Box(modifier = Modifier.weight(0.5f)) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("View Details") },
                    onClick = { /* TODO: View rental details */ },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                if (rental.status == "Active") {
                    DropdownMenuItem(
                        text = { Text("Mark as Completed") },
                        onClick = { /* TODO: Complete rental */ },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                    )
                }
                if (rental.paymentStatus == "Pending") {
                    DropdownMenuItem(
                        text = { Text("Mark as Paid") },
                        onClick = { /* TODO: Mark as paid */ },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Print Invoice") },
                    onClick = { /* TODO: Print invoice */ },
                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) }
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
} 