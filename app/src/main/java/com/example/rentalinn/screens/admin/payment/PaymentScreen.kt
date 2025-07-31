package com.example.rentalinn.screens.admin.payment

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

data class Payment(
    val id: String,
    val rentalId: String,
    val customerName: String,
    val vehicleName: String,
    val amount: Double,
    val paymentMethod: String,
    val status: String,
    val date: String,
    val transactionId: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Sample data
    val payments = remember {
        listOf(
            Payment("1", "R001", "John Doe", "Toyota Avanza", 900000.0, "Bank Transfer", "Success", "2024-03-20", "TRX001"),
            Payment("2", "R002", "Jane Smith", "Honda Civic", 1500000.0, "Cash", "Pending", "2024-03-19"),
            Payment("3", "R003", "Mike Johnson", "Suzuki Ertiga", 1050000.0, "Credit Card", "Success", "2024-03-15", "TRX002"),
            Payment("4", "R004", "Sarah Wilson", "Toyota Rush", 1200000.0, "Bank Transfer", "Failed", "2024-03-10")
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
            placeholder = { Text("Search payments...") },
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
                title = "Total Revenue",
                value = formatCurrency(payments.filter { it.status == "Success" }.sumOf { it.amount }),
                icon = Icons.Default.AttachMoney,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                title = "Pending Payments",
                value = formatCurrency(payments.filter { it.status == "Pending" }.sumOf { it.amount }),
                icon = Icons.Default.Pending,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatisticCard(
                title = "Failed Transactions",
                value = payments.count { it.status == "Failed" }.toString(),
                icon = Icons.Default.Error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All Transactions") },
                icon = { Icon(Icons.Default.Payment, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Pending") },
                icon = { Icon(Icons.Default.Pending, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Failed") },
                icon = { Icon(Icons.Default.Error, contentDescription = null) }
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
                        Text("Amount", modifier = Modifier.weight(0.7f))
                        Text("Method", modifier = Modifier.weight(0.7f))
                        Text("Status", modifier = Modifier.weight(0.5f))
                        Text("Actions", modifier = Modifier.weight(0.5f))
                    }
                }

                // Payments List
                LazyColumn {
                    val filteredPayments = when (selectedTab) {
                        1 -> payments.filter { it.status == "Pending" }
                        2 -> payments.filter { it.status == "Failed" }
                        else -> payments
                    }
                    
                    items(filteredPayments) { payment ->
                        PaymentListItem(payment)
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
fun PaymentListItem(payment: Payment) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(payment.customerName, style = MaterialTheme.typography.bodyLarge)
            Text("ID: ${payment.id}", style = MaterialTheme.typography.bodySmall)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(payment.vehicleName, style = MaterialTheme.typography.bodyLarge)
            Text("Rental ID: ${payment.rentalId}", style = MaterialTheme.typography.bodySmall)
        }
        Text(formatCurrency(payment.amount), modifier = Modifier.weight(0.7f))
        Column(modifier = Modifier.weight(0.7f)) {
            Text(payment.paymentMethod, style = MaterialTheme.typography.bodyMedium)
            payment.transactionId?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
        
        // Status Chip
        Surface(
            color = when (payment.status) {
                "Success" -> MaterialTheme.colorScheme.primary
                "Pending" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.weight(0.5f)
        ) {
            Text(
                text = payment.status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = when (payment.status) {
                    "Success" -> MaterialTheme.colorScheme.primary
                    "Pending" -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
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
                    onClick = { /* TODO: View payment details */ },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                if (payment.status == "Pending") {
                    DropdownMenuItem(
                        text = { Text("Confirm Payment") },
                        onClick = { /* TODO: Confirm payment */ },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Cancel Payment") },
                        onClick = { /* TODO: Cancel payment */ },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Print Receipt") },
                    onClick = { /* TODO: Print receipt */ },
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