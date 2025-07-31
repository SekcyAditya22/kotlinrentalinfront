package com.example.rentalinn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentalinn.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactVehicleCard(
    vehicle: Vehicle,
    modifier: Modifier = Modifier,
    onRentClick: (Vehicle) -> Unit = {},
    onCardClick: (Vehicle) -> Unit = {}
) {
    Card(
        onClick = { onCardClick(vehicle) },
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title and availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = vehicle.vehicleCategory,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Availability badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = vehicle.getStatusColor()
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = vehicle.getAvailableUnits().toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle image
            vehicle.getMainPhoto()?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Vehicle image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Quick specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompactSpec(
                    icon = Icons.Default.Person,
                    value = "${vehicle.passengerCapacity}"
                )
                CompactSpec(
                    icon = Icons.Default.Settings,
                    value = vehicle.transmission.take(4)
                )
                CompactSpec(
                    icon = Icons.Default.LocalGasStation,
                    value = vehicle.fuelType.take(4)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price and action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rp ${(vehicle.pricePerDay.toInt() / 1000)}K",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "per day",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = vehicle.getAvailabilityText(),
                        style = MaterialTheme.typography.labelSmall,
                        color = vehicle.getStatusColor(),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                FilledTonalButton(
                    onClick = { onRentClick(vehicle) },
                    enabled = vehicle.isAvailable(),
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (vehicle.isAvailable()) Icons.Default.Add else Icons.Default.Block,
                        contentDescription = if (vehicle.isAvailable()) "Rent" else "Not available",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSpec(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
