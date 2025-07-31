package com.example.rentalinn.screens.admin.verif

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.util.Log
import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.repository.AdminVerificationRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsVerificationDialog(
    userDetails: UserDetails,
    adminVerificationRepository: AdminVerificationRepository,
    onDismiss: () -> Unit,
    onVerificationComplete: (UserDetails) -> Unit,
    onError: (String) -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var ktpVerificationNotes by remember { mutableStateOf(userDetails.verificationNotes ?: "") }
    var simVerificationNotes by remember { mutableStateOf(userDetails.verificationNotes ?: "") }
    
    val coroutineScope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // User Information
                    UserInfoSection(userDetails = userDetails)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // KTP Verification Section
                    DocumentVerificationSection(
                        title = "KTP Verification",
                        documentType = "KTP",
                        hasDocument = userDetails.ktpPhoto != null,
                        isVerified = userDetails.isKtpVerified,
                        documentNumber = userDetails.ktpNumber,
                        documentPhotoUrl = userDetails.ktpPhoto,
                        notes = ktpVerificationNotes,
                        onNotesChange = { ktpVerificationNotes = it },
                        onVerify = { isVerified, notes ->
                            coroutineScope.launch {
                                isProcessing = true
                                try {
                                    val result = adminVerificationRepository.verifyKtp(
                                        userId = userDetails.userId,
                                        isVerified = isVerified,
                                        notes = notes
                                    )
                                    result.fold(
                                        onSuccess = { updatedUserDetails ->
                                            onVerificationComplete(updatedUserDetails)
                                        },
                                        onFailure = { exception ->
                                            onError(exception.message ?: "KTP verification failed")
                                        }
                                    )
                                } catch (e: Exception) {
                                    onError("Error verifying KTP: ${e.message}")
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        isProcessing = isProcessing
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // SIM Verification Section
                    DocumentVerificationSection(
                        title = "SIM Verification",
                        documentType = "SIM",
                        hasDocument = userDetails.simPhoto != null,
                        isVerified = userDetails.isSimVerified,
                        documentNumber = userDetails.simNumber,
                        documentPhotoUrl = userDetails.simPhoto,
                        notes = simVerificationNotes,
                        onNotesChange = { simVerificationNotes = it },
                        onVerify = { isVerified, notes ->
                            coroutineScope.launch {
                                isProcessing = true
                                try {
                                    val result = adminVerificationRepository.verifySim(
                                        userId = userDetails.userId,
                                        isVerified = isVerified,
                                        notes = notes
                                    )
                                    result.fold(
                                        onSuccess = { updatedUserDetails ->
                                            onVerificationComplete(updatedUserDetails)
                                        },
                                        onFailure = { exception ->
                                            onError(exception.message ?: "SIM verification failed")
                                        }
                                    )
                                } catch (e: Exception) {
                                    onError("Error verifying SIM: ${e.message}")
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        isProcessing = isProcessing
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Overall User Verification
                    OverallVerificationSection(
                        userDetails = userDetails,
                        onVerifyUser = { isVerified, notes ->
                            coroutineScope.launch {
                                isProcessing = true
                                try {
                                    val result = adminVerificationRepository.verifyUser(
                                        userId = userDetails.userId,
                                        isVerified = isVerified,
                                        notes = notes
                                    )
                                    result.fold(
                                        onSuccess = { updatedUserDetails ->
                                            onVerificationComplete(updatedUserDetails)
                                        },
                                        onFailure = { exception ->
                                            onError(exception.message ?: "User verification failed")
                                        }
                                    )
                                } catch (e: Exception) {
                                    onError("Error verifying user: ${e.message}")
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        isProcessing = isProcessing
                    )
                }
                
                // Processing Indicator
                if (isProcessing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing verification...")
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoSection(userDetails: UserDetails) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "User Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("Name", userDetails.user?.name ?: "N/A")
            InfoRow("Email", userDetails.user?.email ?: "N/A")
            InfoRow("Phone", userDetails.user?.phoneNumber ?: "N/A")
            InfoRow("Role", userDetails.user?.role ?: "N/A")
            InfoRow("Account Status", if (userDetails.user?.isVerified == true) "Verified" else "Unverified")
            
            if (userDetails.address != null) {
                InfoRow("Address", userDetails.address)
            }
            if (userDetails.dateOfBirth != null) {
                InfoRow("Date of Birth", userDetails.dateOfBirth)
            }
            if (userDetails.placeOfBirth != null) {
                InfoRow("Place of Birth", userDetails.placeOfBirth)
            }
            if (userDetails.gender != null) {
                InfoRow("Gender", userDetails.gender.replaceFirstChar { it.uppercase() })
            }
            
            // Emergency Contact
            if (userDetails.emergencyContactName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Emergency Contact",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                InfoRow("Name", userDetails.emergencyContactName)
                if (userDetails.emergencyContactPhone != null) {
                    InfoRow("Phone", userDetails.emergencyContactPhone)
                }
                if (userDetails.emergencyContactRelation != null) {
                    InfoRow("Relation", userDetails.emergencyContactRelation)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
fun DocumentVerificationSection(
    title: String,
    documentType: String,
    hasDocument: Boolean,
    isVerified: Boolean,
    documentNumber: String?,
    documentPhotoUrl: String?,
    notes: String,
    onNotesChange: (String) -> Unit,
    onVerify: (Boolean, String) -> Unit,
    isProcessing: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Document Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Document Status:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (color, text, icon) = when {
                            isVerified -> Triple(Color(0xFF4CAF50), "Verified", Icons.Default.CheckCircle)
                            hasDocument -> Triple(Color(0xFFFF9800), "Pending Review", Icons.Default.Schedule)
                            else -> Triple(Color(0xFF757575), "Not Uploaded", Icons.Default.Warning)
                        }

                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = color
                        )
                    }
                }
            }

            // Document Number
            if (documentNumber != null) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("$documentType Number", documentNumber)
            }

            // Document Photo
            if (documentPhotoUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$documentType Photo:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable {
                            // TODO: Open full screen image viewer
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://beexpress.peachy.icu$documentPhotoUrl",
                            contentDescription = "$documentType Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // Fallback untuk jika gambar tidak bisa dimuat
                        // AsyncImage akan menangani loading state secara otomatis
                    }
                }
            }

            // Notes Input
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Verification Notes") },
                placeholder = { Text("Add notes about the verification...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !isProcessing
            )

            // Action Buttons
            if (hasDocument && !isVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onVerify(false, notes) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }

                    Button(
                        onClick = { onVerify(true, notes) },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                }
            } else if (isVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onVerify(false, notes) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revoke Verification")
                }
            }
        }
    }
}

@Composable
fun OverallVerificationSection(
    userDetails: UserDetails,
    onVerifyUser: (Boolean, String) -> Unit,
    isProcessing: Boolean
) {
    var userVerificationNotes by remember { mutableStateOf(userDetails.verificationNotes ?: "") }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overall User Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isUserVerified = userDetails.user?.isVerified == true
                val (color, text, icon) = if (isUserVerified) {
                    Triple(Color(0xFF4CAF50), "User is Verified", Icons.Default.CheckCircle)
                } else {
                    Triple(Color(0xFF757575), "User is Not Verified", Icons.Default.Warning)
                }

                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Requirements Check
            val ktpOk = userDetails.isKtpVerified
            val simOk = userDetails.isSimVerified
            val allDocumentsVerified = ktpOk && simOk

            Text(
                text = "Requirements:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            RequirementRow("KTP Verified", ktpOk)
            RequirementRow("SIM Verified", simOk)

            Spacer(modifier = Modifier.height(16.dp))

            // User Verification Notes
            OutlinedTextField(
                value = userVerificationNotes,
                onValueChange = { userVerificationNotes = it },
                label = { Text("User Verification Notes") },
                placeholder = { Text("Add notes about the user verification...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            val isUserVerified = userDetails.user?.isVerified == true

            if (!isUserVerified && allDocumentsVerified) {
                Button(
                    onClick = { onVerifyUser(true, userVerificationNotes) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify User Account")
                }
            } else if (isUserVerified) {
                Button(
                    onClick = { onVerifyUser(false, userVerificationNotes) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Revoke User Verification")
                }
            } else {
                Text(
                    text = "Complete all document verifications to verify user account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun RequirementRow(label: String, isMet: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isMet) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4CAF50) else Color(0xFFFF9800),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) Color(0xFF4CAF50) else Color(0xFFFF9800)
        )
    }
}
