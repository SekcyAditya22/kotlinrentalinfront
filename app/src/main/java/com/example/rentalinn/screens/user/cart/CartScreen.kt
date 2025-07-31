package com.example.rentalinn.screens.user.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var cartItems by remember { mutableStateOf(getDummyCartItems()) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Rp ${cartItems.sumOf { it.totalPrice }}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showCheckoutDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCart(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onBrowseClick = { navController.navigate("user_rental") }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(
                        item = item,
                        onRemove = { cartItems = cartItems.filter { it.id != item.id } }
                    )
                }
            }
        }
    }
    
    if (showCheckoutDialog) {
        CheckoutDialog(
            cartItems = cartItems,
            onDismiss = { showCheckoutDialog = false },
            onCheckout = {
                /* TODO: Process checkout */
                cartItems = emptyList()
                showCheckoutDialog = false
            }
        )
    }
}

@Composable
fun EmptyCart(
    modifier: Modifier = Modifier,
    onBrowseClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.RemoveShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some vehicles to start renting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBrowseClick
        ) {
            Icon(Icons.Default.DirectionsCar, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse Vehicles")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemCard(
    item: CartItem,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.vehicleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.vehicleType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.startDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = "End Date",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.endDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Rp ${item.totalPrice}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    cartItems: List<CartItem>,
    onDismiss: () -> Unit,
    onCheckout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout") },
        text = {
            Column {
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                cartItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.vehicleName)
                        Text("Rp ${item.totalPrice}")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rp ${cartItems.sumOf { it.totalPrice }}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onCheckout) {
                Text("Pay Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class CartItem(
    val id: Int,
    val vehicleName: String,
    val vehicleType: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Int
)

fun getDummyCartItems() = listOf(
    CartItem(
        id = 1,
        vehicleName = "Toyota Avanza",
        vehicleType = "MPV",
        startDate = "2024-03-20",
        endDate = "2024-03-25",
        totalPrice = 1500000
    ),
    CartItem(
        id = 2,
        vehicleName = "Honda PCX",
        vehicleType = "Motorcycle",
        startDate = "2024-03-22",
        endDate = "2024-03-24",
        totalPrice = 300000
    )
) 