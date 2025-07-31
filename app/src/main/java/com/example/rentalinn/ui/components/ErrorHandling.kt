package com.example.rentalinn.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

sealed class ErrorType {
    object Network : ErrorType()
    object Server : ErrorType()
    object NotFound : ErrorType()
    object Unauthorized : ErrorType()
    object Unknown : ErrorType()
    data class Custom(val message: String) : ErrorType()
}

fun getErrorType(throwable: Throwable?): ErrorType {
    return when {
        throwable?.message?.contains("Unable to resolve host") == true ||
        throwable?.message?.contains("timeout") == true ||
        throwable?.message?.contains("No address associated with hostname") == true -> ErrorType.Network
        
        throwable?.message?.contains("HTTP 404") == true ||
        throwable?.message?.contains("not found") == true -> ErrorType.NotFound
        
        throwable?.message?.contains("HTTP 401") == true ||
        throwable?.message?.contains("Unauthorized") == true -> ErrorType.Unauthorized
        
        throwable?.message?.contains("HTTP 5") == true ||
        throwable?.message?.contains("Internal Server Error") == true -> ErrorType.Server
        
        else -> ErrorType.Unknown
    }
}

@Composable
fun ErrorMessage(
    errorType: ErrorType,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (icon, title, message, actionText) = when (errorType) {
        ErrorType.Network -> {
            Quadruple(
                Icons.Default.WifiOff,
                "No Internet Connection",
                "Please check your internet connection and try again.",
                "Retry"
            )
        }
        ErrorType.Server -> {
            Quadruple(
                Icons.Default.Error,
                "Server Error",
                "Something went wrong on our end. Please try again later.",
                "Retry"
            )
        }
        ErrorType.NotFound -> {
            Quadruple(
                Icons.Default.SearchOff,
                "Not Found",
                "The requested resource could not be found.",
                "Go Back"
            )
        }
        ErrorType.Unauthorized -> {
            Quadruple(
                Icons.Default.Lock,
                "Access Denied",
                "You don't have permission to access this resource.",
                "Login"
            )
        }
        ErrorType.Unknown -> {
            Quadruple(
                Icons.Default.ErrorOutline,
                "Something went wrong",
                "An unexpected error occurred. Please try again.",
                "Retry"
            )
        }
        is ErrorType.Custom -> {
            Quadruple(
                Icons.Default.ErrorOutline,
                "Error",
                errorType.message,
                "Retry"
            )
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onDismiss != null) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Dismiss")
                    }
                }
                
                if (onRetry != null) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(actionText)
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkErrorBanner(
    isVisible: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No internet connection",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun OfflineIndicator(
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    if (isOffline) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You're offline",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper data class for multiple return values
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
