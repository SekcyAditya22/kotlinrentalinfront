package com.example.rentalinn.screens.user.payment

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rentalinn.viewmodel.RentalViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    rentalId: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rentalViewModel: RentalViewModel = viewModel(
        factory = RentalViewModel.Factory(context)
    )

    val rental by rentalViewModel.rental.collectAsState()
    val isLoading by rentalViewModel.isLoading.collectAsState()
    val error by rentalViewModel.error.collectAsState()

    var showWebView by remember { mutableStateOf(false) }
    var snapToken by remember { mutableStateOf<String?>(null) }

    // Load rental details
    LaunchedEffect(rentalId) {
        rentalViewModel.getRentalById(rentalId)
    }

    // Extract snap token from rental payment
    LaunchedEffect(rental) {
        rental?.payment?.snapToken?.let { token ->
            snapToken = token
            showWebView = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        PaymentHeader(
            onBackClick = { navController.popBackStack() }
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                PaymentErrorState(
                    error = error ?: "Unknown error",
                    onRetry = { rentalViewModel.getRentalById(rentalId) }
                )
            }
            rental != null && showWebView && snapToken != null -> {
                // Midtrans WebView
                MidtransWebView(
                    snapToken = snapToken!!,
                    rentalId = rentalId,
                    onPaymentFinish = { result ->
                        // Handle payment result
                        when (result) {
                            "success" -> {
                                // Auto-update payment status with delay
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                    try {
                                        // Wait a bit for Midtrans to process
                                        kotlinx.coroutines.delay(1000)
                                        rentalViewModel.autoUpdatePaymentStatus(rentalId)

                                        // Wait for update to complete
                                        kotlinx.coroutines.delay(2000)

                                        navController.navigate("payment_success/$rentalId") {
                                            popUpTo("payment/$rentalId") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        // If auto-update fails, still go to success page
                                        navController.navigate("payment_success/$rentalId") {
                                            popUpTo("payment/$rentalId") { inclusive = true }
                                        }
                                    }
                                }
                            }
                            "pending" -> {
                                navController.navigate("payment_pending/$rentalId") {
                                    popUpTo("payment/$rentalId") { inclusive = true }
                                }
                            }
                            "error", "failure" -> {
                                navController.navigate("payment_failed/$rentalId") {
                                    popUpTo("payment/$rentalId") { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
            rental != null -> {
                // Show rental summary and payment options
                PaymentSummary(
                    rental = rental!!,
                    onPayNow = {
                        rental!!.payment?.snapToken?.let { token ->
                            snapToken = token
                            showWebView = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentHeader(
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun PaymentErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Payment Error",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun PaymentSummary(
    rental: com.example.rentalinn.model.Rental,
    onPayNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rental Summary Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Rental Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                rental.vehicle?.let { vehicle ->
                    Text(
                        text = vehicle.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${vehicle.brand} ${vehicle.model}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rental Period:")
                    Text("${rental.startDate} to ${rental.endDate}")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Days:")
                    Text("${rental.totalDays} days")
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Amount:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rental.getFormattedTotalAmount(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }


            }
        }



        Spacer(modifier = Modifier.weight(1f))
        
        // Pay Now Button
        Button(
            onClick = onPayNow,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null
                )
                Text(
                    text = "Pay Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MidtransWebView(
    snapToken: String,
    rentalId: Int,
    onPaymentFinish: (String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                // Add JavaScript interface for payment callbacks
                val mainHandler = Handler(Looper.getMainLooper())
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onPaymentSuccess() {
                        // Run on main thread using Handler
                        mainHandler.post {
                            onPaymentFinish("success")
                        }
                    }

                    @JavascriptInterface
                    fun onPaymentPending() {
                        // Run on main thread using Handler
                        mainHandler.post {
                            onPaymentFinish("pending")
                        }
                    }

                    @JavascriptInterface
                    fun onPaymentError() {
                        // Run on main thread using Handler
                        mainHandler.post {
                            onPaymentFinish("error")
                        }
                    }
                }, "PaymentInterface")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        // Check for payment result in URL as fallback
                        url?.let { currentUrl ->
                            when {
                                currentUrl.contains("finish") ||
                                currentUrl.contains("success") ||
                                currentUrl.contains("settlement") ||
                                currentUrl.contains("capture") -> {
                                    onPaymentFinish("success")
                                }
                                currentUrl.contains("pending") && !currentUrl.contains("settlement") -> {
                                    onPaymentFinish("pending")
                                }
                                currentUrl.contains("error") ||
                                currentUrl.contains("failure") ||
                                currentUrl.contains("deny") ||
                                currentUrl.contains("cancel") -> {
                                    onPaymentFinish("error")
                                }
                            }
                        }
                    }
                }

                // Load Midtrans Snap
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <script type="text/javascript"
                                src="https://app.sandbox.midtrans.com/snap/snap.js"
                                data-client-key="SB-Mid-client-JKTpXCMAPXYE_pwA"></script>
                    </head>
                    <body>
                        <script type="text/javascript">
                            snap.pay('$snapToken', {
                                onSuccess: function(result) {
                                    console.log('Payment success:', result);
                                    console.log('Transaction status:', result.transaction_status);
                                    // Force success callback regardless of transaction_status
                                    if (window.PaymentInterface) {
                                        window.PaymentInterface.onPaymentSuccess();
                                    }
                                },
                                onPending: function(result) {
                                    console.log('Payment pending:', result);
                                    console.log('Transaction status:', result.transaction_status);
                                    // Only call pending if it's actually pending
                                    if (result.transaction_status === 'pending') {
                                        if (window.PaymentInterface) {
                                            window.PaymentInterface.onPaymentPending();
                                        }
                                    } else {
                                        // If not pending, treat as success
                                        if (window.PaymentInterface) {
                                            window.PaymentInterface.onPaymentSuccess();
                                        }
                                    }
                                },
                                onError: function(result) {
                                    console.log('Payment error:', result);
                                    if (window.PaymentInterface) {
                                        window.PaymentInterface.onPaymentError();
                                    }
                                },
                                onClose: function() {
                                    console.log('Payment closed by user');
                                    // Don't automatically call error on close
                                    // Let user decide what to do
                                }
                            });
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
