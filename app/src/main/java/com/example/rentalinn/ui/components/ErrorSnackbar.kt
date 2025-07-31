package com.example.rentalinn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFBA1A1A), // Material Design Error color
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss"
                )
            }
        }
    }
} 