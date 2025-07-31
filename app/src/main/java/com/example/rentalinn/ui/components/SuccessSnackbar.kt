package com.example.rentalinn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rentalinn.ui.theme.SuccessContainer
import com.example.rentalinn.ui.theme.OnSuccessContainer

@Composable
fun SuccessSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        color = SuccessContainer,
        contentColor = OnSuccessContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
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
                    contentColor = OnSuccessContainer
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