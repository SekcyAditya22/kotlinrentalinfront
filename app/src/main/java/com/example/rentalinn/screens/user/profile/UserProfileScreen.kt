
package com.example.rentalinn.screens.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.rentalinn.utils.UserSessionManager
import com.example.rentalinn.model.UserDetails
import com.example.rentalinn.repository.UserDetailsRepository
import com.example.rentalinn.repository.UserProfileRepository
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userSessionManager = remember { UserSessionManager.getInstance(context) }
    val userDetailsRepository = remember { UserDetailsRepository.getInstance(context) }
    val userProfileRepository = remember { UserProfileRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // Dialog states
    var showEditBasicProfileDialog by remember { mutableStateOf(false) }
    var showCompleteProfileDialog by remember { mutableStateOf(false) }
    var showUploadDocumentsDialog by remember { mutableStateOf(false) }

    // User data states
    var userName by remember { mutableStateOf("Loading...") }
    var userEmail by remember { mutableStateOf("Loading...") }
    var userPhone by remember { mutableStateOf("Loading...") }
    var userProfilePicture by remember { mutableStateOf<String?>(null) }
    var userIsVerified by remember { mutableStateOf(false) }

    // User details states
    var userDetails by remember { mutableStateOf<UserDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load user data
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        try {
            // Load basic user info from session
            userName = userSessionManager.getCurrentUserName() ?: "Unknown User"
            userEmail = userSessionManager.getCurrentUserEmail() ?: "No Email"
            userPhone = userSessionManager.getCurrentUserPhone() ?: "No Phone"
            userProfilePicture = userSessionManager.getCurrentUserProfilePicture()
            userIsVerified = userSessionManager.isCurrentUserVerified()

            // Load user details from API
            val result = userDetailsRepository.getUserDetails()
            result.fold(
                onSuccess = { details ->
                    userDetails = details
                    android.util.Log.d("UserProfileScreen", "User details loaded: $details")
                },
                onFailure = { exception ->
                    errorMessage = exception.message
                    android.util.Log.e("UserProfileScreen", "Failed to load user details", exception)
                    userDetails = null
                }
            )
        } catch (e: Exception) {
            errorMessage = e.message
            android.util.Log.e("UserProfileScreen", "Error loading user data", e)
        } finally {
            isLoading = false
        }
    }
    
    if (isLoading) {
        LoadingScreen()
        return
    }

    if (errorMessage != null) {
        ErrorScreen(
            errorMessage = errorMessage ?: "Unknown error",
            onRetry = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    val result = userDetailsRepository.getUserDetails()
                    result.fold(
                        onSuccess = { details -> userDetails = details },
                        onFailure = { exception -> errorMessage = exception.message }
                    )
                    isLoading = false
                }
            }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section with Profile Picture and Basic Info
        ProfileHeaderSection(
            userName = userName,
            userEmail = userEmail,
            userPhone = userPhone,
            userProfilePicture = userProfilePicture,
            userIsVerified = userIsVerified
        )

        // Verification Status Section
        VerificationStatusSection(
            userIsVerified = userIsVerified,
            userDetails = userDetails
        )

        // Quick Actions Section
        QuickActionsSection(
            onEditBasicProfile = { showEditBasicProfileDialog = true },
            onCompleteProfile = { showCompleteProfileDialog = true },
            onUploadDocuments = { showUploadDocumentsDialog = true }
        )

        // Profile Information Section
        ProfileInformationSection(
            userDetails = userDetails,
            userPhone = userPhone,
            userEmail = userEmail
        )

        // Documents Section
        DocumentsSection(userDetails = userDetails)

        // Emergency Contact Section
        EmergencyContactSection(userDetails = userDetails)

        // Logout Section
        LogoutSection(onLogout = onLogout)
    }

    // Edit Basic Profile Dialog
    if (showEditBasicProfileDialog) {
        EditBasicProfileDialog(
            currentName = userName,
            currentPhone = userPhone,
            onDismiss = { showEditBasicProfileDialog = false },
            onSave = { name, phone, profileImageFile ->
                coroutineScope.launch {
                    try {
                        android.util.Log.d("EditBasicProfile", "Starting profile update - name: $name, phone: $phone, hasImage: ${profileImageFile != null}")

                        val result = if (profileImageFile != null) {
                            android.util.Log.d("EditBasicProfile", "Updating profile with picture - file: ${profileImageFile.name}, size: ${profileImageFile.length()}")
                            // Update profile with picture in one call
                            userProfileRepository.updateProfileWithPicture(name, phone, profileImageFile)
                        } else {
                            android.util.Log.d("EditBasicProfile", "Updating profile without picture")
                            // Update profile without picture
                            userProfileRepository.updateUserProfile(name, phone)
                        }

                        result.fold(
                            onSuccess = { user ->
                                userName = user.name
                                userPhone = user.phoneNumber ?: "No Phone"
                                userProfilePicture = user.profilePicture

                                // Update user session with new data
                                userSessionManager.updateUserData(user)

                                showEditBasicProfileDialog = false
                                android.util.Log.d("UserProfileScreen", "Profile updated successfully")
                                android.util.Log.d("UserProfileScreen", "New profile picture: ${user.profilePicture}")

                                Toast.makeText(
                                    context,
                                    if (profileImageFile != null) "Profile and picture updated successfully" else "Profile updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = { exception ->
                                errorMessage = exception.message
                                android.util.Log.e("UserProfileScreen", "Failed to update profile", exception)

                                Toast.makeText(
                                    context,
                                    "Failed to update profile: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } catch (e: Exception) {
                        errorMessage = e.message
                        android.util.Log.e("UserProfileScreen", "Error updating profile", e)
                    }
                }
            }
        )
    }

    // Complete Profile Dialog
    if (showCompleteProfileDialog) {
        CompleteProfileDialog(
            userDetails = userDetails,
            onDismiss = { showCompleteProfileDialog = false },
            onSave = { updatedDetails ->
                coroutineScope.launch {
                    try {
                        val updateRequest = com.example.rentalinn.model.UpdateUserDetailsRequest(
                            ktpNumber = updatedDetails.ktpNumber,
                            simNumber = updatedDetails.simNumber,
                            address = updatedDetails.address,
                            dateOfBirth = updatedDetails.dateOfBirth,
                            placeOfBirth = updatedDetails.placeOfBirth,
                            gender = updatedDetails.gender,
                            emergencyContactName = updatedDetails.emergencyContactName,
                            emergencyContactPhone = updatedDetails.emergencyContactPhone,
                            emergencyContactRelation = updatedDetails.emergencyContactRelation
                        )

                        val result = userDetailsRepository.updateUserDetails(updateRequest)
                        result.fold(
                            onSuccess = { updated ->
                                userDetails = updated
                                showCompleteProfileDialog = false
                                android.util.Log.d("UserProfileScreen", "User details updated successfully")

                                Toast.makeText(
                                    context,
                                    "Profile completed successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = { exception ->
                                errorMessage = exception.message
                                android.util.Log.e("UserProfileScreen", "Failed to update user details", exception)

                                Toast.makeText(
                                    context,
                                    "Failed to update profile: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } catch (e: Exception) {
                        errorMessage = e.message
                        android.util.Log.e("UserProfileScreen", "Error updating user details", e)
                    }
                }
            }
        )
    }

    // Upload Documents Dialog
    if (showUploadDocumentsDialog) {
        UploadDocumentsDialog(
            userDetails = userDetails,
            userDetailsRepository = userDetailsRepository,
            context = context,
            onDismiss = { showUploadDocumentsDialog = false },
            onUploadSuccess = { updatedDetails ->
                userDetails = updatedDetails
                showUploadDocumentsDialog = false

                Toast.makeText(
                    context,
                    "Documents uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onUploadError = { errorMessage ->
                Toast.makeText(
                    context,
                    "Upload failed: $errorMessage",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileItemRow(item)
                    if (index < items.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileItemRow(item: ProfileItem) {
    ListItem(
        headlineContent = { Text(item.title) },
        supportingContent = { Text(item.value) },
        leadingContent = {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = if (item.onClick != null) {
            {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        } else null,
        modifier = if (item.onClick != null) {
            Modifier.clickable { item.onClick.invoke() }
        } else {
            Modifier
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    // Get current user data from context
    val context = androidx.compose.ui.platform.LocalContext.current
    val userSessionManager = remember { UserSessionManager.getInstance(context) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Load current user data
    LaunchedEffect(Unit) {
        name = userSessionManager.getCurrentUserName() ?: ""
        email = userSessionManager.getCurrentUserEmail() ?: ""
        phone = userSessionManager.getCurrentUserPhone() ?: ""
        address = "123 Main Street, City" // TODO: Add address field to user model
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave()
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Interactive Profile Items with onClick support
data class ProfileItem(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBasicProfileDialog(
    currentName: String,
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, profileImageFile: File?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        // Convert URI to File
        uri?.let {
            try {
                val file = createFileFromUri(context, it, "profile_picture_${System.currentTimeMillis()}.jpg")
                selectedImageFile = file
            } catch (e: Exception) {
                android.util.Log.e("EditBasicProfileDialog", "Error creating file from URI", e)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Basic Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Picture Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Profile Picture",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        // Profile Picture Preview
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = selectedImageUri ?: "https://via.placeholder.com/150/CCCCCC/FFFFFF?text=No+Image",
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                            )

                            // Upload overlay
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clickable {
                                        if (!isUploadingImage) {
                                            imagePickerLauncher.launch("image/*")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploadingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = "Change Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (selectedImageUri != null) "Tap to change photo" else "Tap to add photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.trim() },
                    label = { Text("Name") },
                    placeholder = { Text("Enter your name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = name.isNotEmpty() && name.length < 2,
                    supportingText = if (name.isNotEmpty() && name.length < 2) {
                        { Text("Name must be at least 2 characters", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.trim() },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+62812345678") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phone.isNotEmpty() && !phone.matches(Regex("^(\\+62|62|0)[0-9]{9,13}$")),
                    supportingText = if (phone.isNotEmpty() && !phone.matches(Regex("^(\\+62|62|0)[0-9]{9,13}$"))) {
                        { Text("Invalid Indonesian phone number format", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate before saving
                    if (name.isNotEmpty() && name.length < 2) {
                        return@Button
                    }
                    if (phone.isNotEmpty() && !phone.matches(Regex("^(\\+62|62|0)[0-9]{9,13}$"))) {
                        return@Button
                    }

                    onSave(name, phone, selectedImageFile)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDetailsDialog(
    userDetails: UserDetails?,
    onDismiss: () -> Unit,
    onSave: (UserDetails) -> Unit
) {
    var ktpNumber by remember { mutableStateOf(userDetails?.ktpNumber ?: "") }
    var simNumber by remember { mutableStateOf(userDetails?.simNumber ?: "") }
    var address by remember { mutableStateOf(userDetails?.address ?: "") }
    var dateOfBirth by remember { mutableStateOf(userDetails?.dateOfBirth ?: "") }
    var placeOfBirth by remember { mutableStateOf(userDetails?.placeOfBirth ?: "") }
    var gender by remember { mutableStateOf(userDetails?.gender ?: "") }
    var emergencyContactName by remember { mutableStateOf(userDetails?.emergencyContactName ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(userDetails?.emergencyContactPhone ?: "") }
    var emergencyContactRelation by remember { mutableStateOf(userDetails?.emergencyContactRelation ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }

    // Set date picker to only allow dates from 100 years ago to 17 years ago
    val currentCalendar = java.util.Calendar.getInstance()
    val maxYear = currentCalendar.get(java.util.Calendar.YEAR) - 17 // 17 years ago
    val minYear = currentCalendar.get(java.util.Calendar.YEAR) - 100 // 100 years ago

    val datePickerState = rememberDatePickerState(
        yearRange = IntRange(start = minYear, endInclusive = maxYear),
        initialSelectedDateMillis = java.util.Calendar.getInstance().apply {
            set(maxYear, 0, 1) // Set to January 1st of max allowed year
        }.timeInMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Personal Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // KTP Number
                OutlinedTextField(
                    value = ktpNumber,
                    onValueChange = { if (it.length <= 16) ktpNumber = it },
                    label = { Text("KTP Number") },
                    placeholder = { Text("16 digit KTP number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }
                )

                // SIM Number
                OutlinedTextField(
                    value = simNumber,
                    onValueChange = { if (it.length <= 12) simNumber = it },
                    label = { Text("SIM Number") },
                    placeholder = { Text("SIM number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DriveEta, contentDescription = null) }
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.trim() },
                    label = { Text("Address") },
                    placeholder = { Text("Minimum 10 characters") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    isError = address.isNotEmpty() && address.length < 10,
                    supportingText = if (address.isNotEmpty() && address.length < 10) {
                        { Text("Address must be at least 10 characters", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // Date of Birth
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { },
                    label = { Text("Date of Birth") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )

                // Place of Birth
                OutlinedTextField(
                    value = placeOfBirth,
                    onValueChange = { placeOfBirth = it.trim() },
                    label = { Text("Place of Birth") },
                    placeholder = { Text("City of birth") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                )

                // Gender Selection
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = gender == "male",
                                onClick = { gender = "male" }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == "male",
                            onClick = { gender = "male" }
                        )
                        Text("Male", modifier = Modifier.padding(start = 4.dp))
                    }

                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = gender == "female",
                                onClick = { gender = "female" }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == "female",
                            onClick = { gender = "female" }
                        )
                        Text("Female", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                // Emergency Contact Section
                Text(
                    text = "Emergency Contact",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it.trim() },
                    label = { Text("Contact Name") },
                    placeholder = { Text("Emergency contact name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )

                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it.trim() },
                    label = { Text("Contact Phone") },
                    placeholder = { Text("+62812345678") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = emergencyContactRelation,
                    onValueChange = { emergencyContactRelation = it.trim() },
                    label = { Text("Relationship") },
                    placeholder = { Text("e.g., Mother, Father, Spouse") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate before saving
                    if (address.isNotEmpty() && address.length < 10) {
                        return@Button
                    }

                    // Create updated UserDetails object
                    val updatedDetails = userDetails?.copy(
                        ktpNumber = ktpNumber.ifEmpty { null },
                        simNumber = simNumber.ifEmpty { null },
                        address = address.ifEmpty { null },
                        dateOfBirth = dateOfBirth.ifEmpty { null },
                        placeOfBirth = placeOfBirth.ifEmpty { null },
                        gender = gender.ifEmpty { null },
                        emergencyContactName = emergencyContactName.ifEmpty { null },
                        emergencyContactPhone = emergencyContactPhone.ifEmpty { null },
                        emergencyContactRelation = emergencyContactRelation.ifEmpty { null }
                    ) ?: UserDetails(
                        id = 0,
                        userId = 0,
                        ktpNumber = ktpNumber.ifEmpty { null },
                        ktpPhoto = null,
                        simNumber = simNumber.ifEmpty { null },
                        simPhoto = null,
                        address = address.ifEmpty { null },
                        dateOfBirth = dateOfBirth.ifEmpty { null },
                        placeOfBirth = placeOfBirth.ifEmpty { null },
                        gender = gender.ifEmpty { null },
                        emergencyContactName = emergencyContactName.ifEmpty { null },
                        emergencyContactPhone = emergencyContactPhone.ifEmpty { null },
                        emergencyContactRelation = emergencyContactRelation.ifEmpty { null },
                        isKtpVerified = false,
                        isSimVerified = false,
                        verificationNotes = null,
                        verifiedAt = null,
                        verifiedBy = null,
                        createdAt = "",
                        updatedAt = ""
                    )
                    onSave(updatedDetails)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            dateOfBirth = formatter.format(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun UploadDocumentsDialog(
    userDetails: UserDetails?,
    userDetailsRepository: UserDetailsRepository,
    context: android.content.Context,
    onDismiss: () -> Unit,
    onUploadSuccess: (UserDetails) -> Unit,
    onUploadError: (String) -> Unit
) {
    var ktpPhotoStatus by remember { mutableStateOf(userDetails?.ktpPhoto != null) }
    var simPhotoStatus by remember { mutableStateOf(userDetails?.simPhoto != null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    // Image picker launchers
    val ktpImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    isUploading = true
                    uploadProgress = "Uploading KTP photo..."

                    val file = createFileFromUri(context, uri, "ktp_photo.jpg")
                    val result = userDetailsRepository.uploadKtpPhoto(file)

                    result.fold(
                        onSuccess = { photoPath ->
                            ktpPhotoStatus = true
                            uploadProgress = "KTP photo uploaded successfully"
                            android.util.Log.d("UploadDocuments", "KTP uploaded: $photoPath")
                        },
                        onFailure = { exception ->
                            onUploadError("Failed to upload KTP: ${exception.message}")
                            android.util.Log.e("UploadDocuments", "KTP upload failed", exception)
                        }
                    )
                } catch (e: Exception) {
                    onUploadError("Error uploading KTP: ${e.message}")
                    android.util.Log.e("UploadDocuments", "KTP upload error", e)
                } finally {
                    isUploading = false
                    uploadProgress = ""
                }
            }
        }
    }

    val simImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    isUploading = true
                    uploadProgress = "Uploading SIM photo..."

                    val file = createFileFromUri(context, uri, "sim_photo.jpg")
                    val result = userDetailsRepository.uploadSimPhoto(file)

                    result.fold(
                        onSuccess = { photoPath ->
                            simPhotoStatus = true
                            uploadProgress = "SIM photo uploaded successfully"
                            android.util.Log.d("UploadDocuments", "SIM uploaded: $photoPath")
                        },
                        onFailure = { exception ->
                            onUploadError("Failed to upload SIM: ${exception.message}")
                            android.util.Log.e("UploadDocuments", "SIM upload failed", exception)
                        }
                    )
                } catch (e: Exception) {
                    onUploadError("Error uploading SIM: ${e.message}")
                    android.util.Log.e("UploadDocuments", "SIM upload error", e)
                } finally {
                    isUploading = false
                    uploadProgress = ""
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Documents") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Upload your identity documents for verification",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // KTP Photo Upload
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ktpPhotoStatus)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null,
                                tint = if (ktpPhotoStatus)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "KTP Photo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (ktpPhotoStatus) "Uploaded" else "Not uploaded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (ktpPhotoStatus) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Uploaded",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (!isUploading) {
                                    ktpImagePicker.launch("image/*")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading
                        ) {
                            Icon(
                                if (ktpPhotoStatus) Icons.Default.Edit else Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (ktpPhotoStatus) "Change Photo" else "Upload Photo")
                        }
                    }
                }

                // SIM Photo Upload
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (simPhotoStatus)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.DriveEta,
                                contentDescription = null,
                                tint = if (simPhotoStatus)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SIM Photo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (simPhotoStatus) "Uploaded" else "Not uploaded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (simPhotoStatus) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Uploaded",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (!isUploading) {
                                    simImagePicker.launch("image/*")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUploading
                        ) {
                            Icon(
                                if (simPhotoStatus) Icons.Default.Edit else Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (simPhotoStatus) "Change Photo" else "Upload Photo")
                        }
                    }
                }

                // Upload Progress
                if (isUploading) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uploadProgress,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Upload Guidelines
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upload Guidelines",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Photos should be clear and readable\n" +
                                   "• Maximum file size: 5MB\n" +
                                   "• Supported formats: JPG, PNG\n" +
                                   "• Documents will be verified by admin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Create updated UserDetails with photo status
                    val updatedDetails = userDetails?.copy(
                        ktpPhoto = if (ktpPhotoStatus) "/uploads/ktp/temp_ktp.jpg" else null,
                        simPhoto = if (simPhotoStatus) "/uploads/sim/temp_sim.jpg" else null
                    ) ?: UserDetails(
                        id = 0,
                        userId = 0,
                        ktpNumber = null,
                        ktpPhoto = if (ktpPhotoStatus) "/uploads/ktp/temp_ktp.jpg" else null,
                        simNumber = null,
                        simPhoto = if (simPhotoStatus) "/uploads/sim/temp_sim.jpg" else null,
                        address = null,
                        dateOfBirth = null,
                        placeOfBirth = null,
                        gender = null,
                        emergencyContactName = null,
                        emergencyContactPhone = null,
                        emergencyContactRelation = null,
                        isKtpVerified = false,
                        isSimVerified = false,
                        verificationNotes = null,
                        verifiedAt = null,
                        verifiedBy = null,
                        createdAt = "",
                        updatedAt = ""
                    )
                    // Refresh user details from server to get latest data
                    coroutineScope.launch {
                        try {
                            val result = userDetailsRepository.getUserDetails()
                            result.fold(
                                onSuccess = { refreshedDetails ->
                                    onUploadSuccess(refreshedDetails ?: userDetails!!)
                                },
                                onFailure = { exception ->
                                    onUploadError("Failed to refresh data: ${exception.message}")
                                }
                            )
                        } catch (e: Exception) {
                            onUploadError("Error refreshing data: ${e.message}")
                        }
                    }
                },
                enabled = !isUploading
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to create file from URI
private fun createFileFromUri(context: android.content.Context, uri: Uri, fileName: String): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)

    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    return file
}

// Helper function to get profile picture URL
private fun getProfilePictureUrl(profilePicture: String?): String {
    val url = if (!profilePicture.isNullOrEmpty()) {
        "https://beexpress.peachy.icu/uploads/profile_picture/$profilePicture"
    } else {
        "https://via.placeholder.com/150/CCCCCC/FFFFFF?text=No+Image"
    }
    android.util.Log.d("ProfilePicture", "Profile picture URL: $url (original: $profilePicture)")
    return url
}

// New Modern UI Components

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading profile...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Error loading profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    userName: String,
    userEmail: String,
    userPhone: String,
    userProfilePicture: String?,
    userIsVerified: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Picture
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AsyncImage(
                        model = getProfilePictureUrl(userProfilePicture),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_gallery),
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )

                    // Verification badge
                    if (userIsVerified) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // User Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Text(
                        text = userPhone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    // Verification Status
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (userIsVerified)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (userIsVerified) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (userIsVerified)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = if (userIsVerified) "Verified Account" else "Pending Verification",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (userIsVerified)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onEditBasicProfile: () -> Unit,
    onCompleteProfile: () -> Unit,
    onUploadDocuments: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Data Diri untuk Verifikasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Edit Basic Profile
                QuickActionCard(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    onClick = onEditBasicProfile,
                    modifier = Modifier.weight(1f)
                )

                // Complete Profile
                QuickActionCard(
                    title = "Verif",
                    icon = Icons.Default.PersonAdd,
                    onClick = onCompleteProfile,
                    modifier = Modifier.weight(1f)
                )

                // Upload Documents
                QuickActionCard(
                    title = "KTP/SIM",
                    icon = Icons.Default.Upload,
                    onClick = onUploadDocuments,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProfileInformationSection(
    userDetails: UserDetails?,
    userPhone: String,
    userEmail: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ProfileInfoItem(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = userPhone
            )

            ProfileInfoItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = userEmail
            )

            ProfileInfoItem(
                icon = Icons.Default.LocationOn,
                label = "Address",
                value = userDetails?.address ?: "Not provided"
            )

            ProfileInfoItem(
                icon = Icons.Default.DateRange,
                label = "Date of Birth",
                value = userDetails?.getFormattedDateOfBirth() ?: "Not provided"
            )

            ProfileInfoItem(
                icon = Icons.Default.Place,
                label = "Place of Birth",
                value = userDetails?.placeOfBirth ?: "Not provided"
            )

            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = "Gender",
                value = userDetails?.getGenderDisplay() ?: "Not specified"
            )
        }
    }
}

@Composable
fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DocumentsSection(userDetails: UserDetails?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Identity Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            DocumentItem(
                icon = Icons.Default.Badge,
                label = "KTP Number",
                value = userDetails?.getMaskedKtpNumber() ?: "Not provided",
                isVerified = userDetails?.isKtpVerified == true
            )

            DocumentItem(
                icon = Icons.Default.PhotoCamera,
                label = "KTP Photo",
                value = if (userDetails?.ktpPhoto != null) "Uploaded" else "Not uploaded",
                isVerified = userDetails?.isKtpVerified == true
            )

            DocumentItem(
                icon = Icons.Default.DriveEta,
                label = "SIM Number",
                value = userDetails?.getMaskedSimNumber() ?: "Not provided",
                isVerified = userDetails?.isSimVerified == true
            )

            DocumentItem(
                icon = Icons.Default.PhotoCamera,
                label = "SIM Photo",
                value = if (userDetails?.simPhoto != null) "Uploaded" else "Not uploaded",
                isVerified = userDetails?.isSimVerified == true
            )

            // Overall verification status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (userDetails?.getVerificationStatus()) {
                        "verified" -> MaterialTheme.colorScheme.primaryContainer
                        "pending" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (userDetails?.getVerificationStatus()) {
                            "verified" -> Icons.Default.CheckCircle
                            "pending" -> Icons.Default.Schedule
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (userDetails?.getVerificationStatus()) {
                            "verified" -> MaterialTheme.colorScheme.primary
                            "pending" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verification Status: ${userDetails?.getVerificationStatusDisplay() ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isVerified: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        if (isVerified) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmergencyContactSection(userDetails: UserDetails?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Emergency Contact",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ProfileInfoItem(
                icon = Icons.Default.Person,
                label = "Contact Name",
                value = userDetails?.emergencyContactName ?: "Not provided"
            )

            ProfileInfoItem(
                icon = Icons.Default.Phone,
                label = "Contact Phone",
                value = userDetails?.emergencyContactPhone ?: "Not provided"
            )

            ProfileInfoItem(
                icon = Icons.Default.Group,
                label = "Relationship",
                value = userDetails?.emergencyContactRelation ?: "Not provided"
            )
        }
    }
}

@Composable
fun LogoutSection(onLogout: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun VerificationStatusSection(
    userIsVerified: Boolean,
    userDetails: UserDetails?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                userIsVerified -> MaterialTheme.colorScheme.primaryContainer
                userDetails?.getVerificationStatus() == "pending" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            userIsVerified -> MaterialTheme.colorScheme.primary
                            userDetails?.getVerificationStatus() == "pending" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        userIsVerified -> Icons.Default.VerifiedUser
                        userDetails?.getVerificationStatus() == "pending" -> Icons.Default.Schedule
                        else -> Icons.Default.PersonOff
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Status Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        userIsVerified -> "Verified Person"
                        userDetails?.getVerificationStatus() == "pending" -> "Verification Pending"
                        else -> "Verification Required"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        userIsVerified -> "Your account is fully verified"
                        userDetails?.getVerificationStatus() == "pending" -> "Documents under review by admin"
                        else -> "Complete your profile and upload documents"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress Indicator
            if (!userIsVerified) {
                val progress = calculateVerificationProgress(userDetails)
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp,
                        color = when {
                            userDetails?.getVerificationStatus() == "pending" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper function to calculate verification progress
private fun calculateVerificationProgress(userDetails: UserDetails?): Float {
    if (userDetails == null) return 0f

    var completedItems = 0
    val totalItems = 10 // Total verification items

    // Identity documents (4 items)
    if (!userDetails.ktpNumber.isNullOrEmpty() && userDetails.ktpNumber!!.length == 16) completedItems++
    if (userDetails.ktpPhoto != null) completedItems++
    if (!userDetails.simNumber.isNullOrEmpty()) completedItems++ // SIM is optional but counts for progress
    if (userDetails.simPhoto != null) completedItems++

    // Personal info (4 items)
    if (!userDetails.address.isNullOrEmpty() && userDetails.address!!.length >= 10) completedItems++
    if (!userDetails.dateOfBirth.isNullOrEmpty()) completedItems++
    if (!userDetails.placeOfBirth.isNullOrEmpty()) completedItems++
    if (!userDetails.gender.isNullOrEmpty()) completedItems++

    // Emergency contact (2 items)
    if (!userDetails.emergencyContactName.isNullOrEmpty()) completedItems++
    if (!userDetails.emergencyContactPhone.isNullOrEmpty()) completedItems++

    return completedItems.toFloat() / totalItems.toFloat()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileDialog(
    userDetails: UserDetails?,
    onDismiss: () -> Unit,
    onSave: (UserDetails) -> Unit
) {
    var ktpNumber by remember { mutableStateOf(userDetails?.ktpNumber ?: "") }
    var simNumber by remember { mutableStateOf(userDetails?.simNumber ?: "") }
    var address by remember { mutableStateOf(userDetails?.address ?: "") }
    var dateOfBirth by remember { mutableStateOf(userDetails?.dateOfBirth ?: "") }
    var placeOfBirth by remember { mutableStateOf(userDetails?.placeOfBirth ?: "") }
    var gender by remember { mutableStateOf(userDetails?.gender ?: "") }
    var emergencyContactName by remember { mutableStateOf(userDetails?.emergencyContactName ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(userDetails?.emergencyContactPhone ?: "") }
    var emergencyContactRelation by remember { mutableStateOf(userDetails?.emergencyContactRelation ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }

    // Set date picker to only allow dates from 100 years ago to 17 years ago
    val currentCalendar = java.util.Calendar.getInstance()
    val maxYear = currentCalendar.get(java.util.Calendar.YEAR) - 17 // 17 years ago
    val minYear = currentCalendar.get(java.util.Calendar.YEAR) - 100 // 100 years ago

    val datePickerState = rememberDatePickerState(
        yearRange = IntRange(start = minYear, endInclusive = maxYear),
        initialSelectedDateMillis = java.util.Calendar.getInstance().apply {
            set(maxYear, 0, 1) // Set to January 1st of max allowed year
        }.timeInMillis
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Complete Your Profile")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress indicator
                val progress = calculateVerificationProgress(userDetails)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Column {
                            Text(
                                text = "Profile Completion",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Identity Documents Section
                Text(
                    text = "Identity Documents",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // KTP Number
                OutlinedTextField(
                    value = ktpNumber,
                    onValueChange = { if (it.length <= 16) ktpNumber = it },
                    label = { Text("KTP Number *") },
                    placeholder = { Text("16 digit KTP number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = ktpNumber.isNotEmpty() && ktpNumber.length != 16,
                    supportingText = if (ktpNumber.isNotEmpty() && ktpNumber.length != 16) {
                        { Text("KTP number must be exactly 16 digits", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // SIM Number
                OutlinedTextField(
                    value = simNumber,
                    onValueChange = { if (it.length <= 12) simNumber = it },
                    label = { Text("SIM Number") },
                    placeholder = { Text("SIM number (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DriveEta, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Personal Information Section
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it.trim() },
                    label = { Text("Address *") },
                    placeholder = { Text("Complete address (min 10 characters)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    isError = address.isNotEmpty() && address.length < 10,
                    supportingText = if (address.isNotEmpty() && address.length < 10) {
                        { Text("Address must be at least 10 characters", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // Date of Birth
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { },
                    label = { Text("Date of Birth *") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )

                // Place of Birth
                OutlinedTextField(
                    value = placeOfBirth,
                    onValueChange = { placeOfBirth = it.trim() },
                    label = { Text("Place of Birth") },
                    placeholder = { Text("City of birth") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
                )

                // Gender Selection
                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = gender == "male",
                                onClick = { gender = "male" }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == "male",
                            onClick = { gender = "male" }
                        )
                        Text("Male", modifier = Modifier.padding(start = 4.dp))
                    }

                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = gender == "female",
                                onClick = { gender = "female" }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = gender == "female",
                            onClick = { gender = "female" }
                        )
                        Text("Female", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                // Emergency Contact Section
                Text(
                    text = "Emergency Contact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = emergencyContactName,
                    onValueChange = { emergencyContactName = it.trim() },
                    label = { Text("Contact Name") },
                    placeholder = { Text("Emergency contact name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )

                OutlinedTextField(
                    value = emergencyContactPhone,
                    onValueChange = { emergencyContactPhone = it.trim() },
                    label = { Text("Contact Phone") },
                    placeholder = { Text("+62812345678") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = emergencyContactPhone.isNotEmpty() && !emergencyContactPhone.matches(Regex("^(\\+62|62|0)[0-9]{9,13}$")),
                    supportingText = if (emergencyContactPhone.isNotEmpty() && !emergencyContactPhone.matches(Regex("^(\\+62|62|0)[0-9]{9,13}$"))) {
                        { Text("Invalid Indonesian phone number format", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                OutlinedTextField(
                    value = emergencyContactRelation,
                    onValueChange = { emergencyContactRelation = it.trim() },
                    label = { Text("Relationship") },
                    placeholder = { Text("e.g., Mother, Father, Spouse") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate required fields
                    if (ktpNumber.isNotEmpty() && ktpNumber.length != 16) {
                        return@Button
                    }
                    if (address.isNotEmpty() && address.length < 10) {
                        return@Button
                    }
                    if (emergencyContactPhone.isNotEmpty() && !emergencyContactPhone.matches(Regex("^(\\+62|62|0)[0-9]{9,13}$"))) {
                        return@Button
                    }

                    // Create updated UserDetails object
                    val updatedDetails = userDetails?.copy(
                        ktpNumber = ktpNumber.ifEmpty { null },
                        simNumber = simNumber.ifEmpty { null },
                        address = address.ifEmpty { null },
                        dateOfBirth = dateOfBirth.ifEmpty { null },
                        placeOfBirth = placeOfBirth.ifEmpty { null },
                        gender = gender.ifEmpty { null },
                        emergencyContactName = emergencyContactName.ifEmpty { null },
                        emergencyContactPhone = emergencyContactPhone.ifEmpty { null },
                        emergencyContactRelation = emergencyContactRelation.ifEmpty { null }
                    ) ?: UserDetails(
                        id = 0,
                        userId = 0,
                        ktpNumber = ktpNumber.ifEmpty { null },
                        ktpPhoto = null,
                        simNumber = simNumber.ifEmpty { null },
                        simPhoto = null,
                        address = address.ifEmpty { null },
                        dateOfBirth = dateOfBirth.ifEmpty { null },
                        placeOfBirth = placeOfBirth.ifEmpty { null },
                        gender = gender.ifEmpty { null },
                        emergencyContactName = emergencyContactName.ifEmpty { null },
                        emergencyContactPhone = emergencyContactPhone.ifEmpty { null },
                        emergencyContactRelation = emergencyContactRelation.ifEmpty { null },
                        isKtpVerified = false,
                        isSimVerified = false,
                        verificationNotes = null,
                        verifiedAt = null,
                        verifiedBy = null,
                        createdAt = "",
                        updatedAt = ""
                    )
                    onSave(updatedDetails)
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            dateOfBirth = formatter.format(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
