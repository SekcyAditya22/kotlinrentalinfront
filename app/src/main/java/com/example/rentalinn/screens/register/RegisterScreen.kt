package com.example.rentalinn.screens.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rentalinn.R
import com.example.rentalinn.ui.components.ErrorSnackbar
import com.example.rentalinn.ui.components.SuccessSnackbar
import com.example.rentalinn.utils.ValidationUtils
import com.example.rentalinn.viewmodel.AuthUiState
import com.example.rentalinn.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.rentalinn.ui.theme.SuccessContainer

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    // Validation states
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Focus requesters
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    
    val viewModel = viewModel<AuthViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    // Local validation functions
    fun validateName() {
        nameError = ValidationUtils.validateName(name) ?: ""
    }

    fun validateEmail() {
        emailError = ValidationUtils.validateEmail(email) ?: ""
    }

    fun validatePassword() {
        passwordError = ValidationUtils.validatePassword(password) ?: ""
    }

    fun validateConfirmPassword() {
        confirmPasswordError = ValidationUtils.validateConfirmPassword(password, confirmPassword) ?: ""
    }

    fun validateForm(): Boolean {
        validateName()
        validateEmail()
        validatePassword()
        validateConfirmPassword()
        return ValidationUtils.validateForm(name, email, password, confirmPassword)
    }

    // Handle UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                showSuccessDialog = true
                showSuccessSnackbar = true
                showErrorSnackbar = false
                successMessage = "Akun berhasil dibuat!"
                delay(2000) // Show success snackbar for 2 seconds
                showSuccessSnackbar = false
                onRegisterSuccess()
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                showErrorSnackbar = true
                showSuccessSnackbar = false
                showSuccessDialog = false
                errorMessage = when {
                    (uiState as AuthUiState.Error).message.contains("email", ignoreCase = true) -> 
                        "Email sudah terdaftar"
                    else -> (uiState as AuthUiState.Error).message
                }
                delay(2000) // Show error snackbar for 2 seconds
                showErrorSnackbar = false
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content
        Scaffold(
            snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667eea),
                                Color(0xFF764ba2),
                                Color(0xFF667eea).copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Logo Container
                    var logoVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(300)
                        logoVisible = true
                    }

                    AnimatedVisibility(
                        visible = logoVisible,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(animationSpec = tween(800))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    ambientColor = Color.Black.copy(alpha = 0.3f)
                                )
                                .background(
                                    Color.White,
                                    shape = CircleShape
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logonjay),
                                contentDescription = "RentalInn Logo",
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Welcome Text with Animation
                    var textVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(logoVisible) {
                        if (logoVisible) {
                            delay(400)
                            textVisible = true
                        }
                    }

                    AnimatedVisibility(
                        visible = textVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(600)
                        ) + fadeIn(animationSpec = tween(600))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Create Account",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Join Rentalin",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 16.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Form Container with Glass Effect
                    var formVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(textVisible) {
                        if (textVisible) {
                            delay(300)
                            formVisible = true
                        }
                    }

                    AnimatedVisibility(
                        visible = formVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = tween(700)
                        ) + fadeIn(animationSpec = tween(700))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 25.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.2f)
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Name Field
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        if (nameError.isNotEmpty()) validateName()
                                    },
                                    label = {
                                        Text(
                                            "Full Name",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Name",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    isError = nameError.isNotEmpty(),
                                    supportingText = {
                                        if (nameError.isNotEmpty()) {
                                            Text(nameError, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { emailFocusRequester.requestFocus() }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Email Field
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = {
                                        email = it
                                        if (emailError.isNotEmpty()) validateEmail()
                                    },
                                    label = {
                                        Text(
                                            "Email Address",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    isError = emailError.isNotEmpty(),
                                    supportingText = {
                                        if (emailError.isNotEmpty()) {
                                            Text(emailError, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { passwordFocusRequester.requestFocus() }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(emailFocusRequester),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Password Field
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        if (passwordError.isNotEmpty()) validatePassword()
                                        if (confirmPasswordError.isNotEmpty()) validateConfirmPassword()
                                    },
                                    label = {
                                        Text(
                                            "Password",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Password",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(
                                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    isError = passwordError.isNotEmpty(),
                                    supportingText = {
                                        if (passwordError.isNotEmpty()) {
                                            Text(passwordError, color = MaterialTheme.colorScheme.error)
                                        } else {
                                            Text(
                                                "Min 8 chars, uppercase, lowercase, number",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { confirmPasswordFocusRequester.requestFocus() }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(passwordFocusRequester),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Confirm Password Field
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = {
                                        confirmPassword = it
                                        if (confirmPasswordError.isNotEmpty()) validateConfirmPassword()
                                    },
                                    label = {
                                        Text(
                                            "Confirm Password",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Confirm Password",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                            Icon(
                                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    isError = confirmPasswordError.isNotEmpty(),
                                    supportingText = {
                                        if (confirmPasswordError.isNotEmpty()) {
                                            Text(confirmPasswordError, color = MaterialTheme.colorScheme.error)
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { keyboardController?.hide() }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(confirmPasswordFocusRequester),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Register Button
                                Button(
                                    onClick = {
                                        keyboardController?.hide()
                                        if (validateForm()) {
                                            viewModel.register(name, email, password, "")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = name.isNotEmpty() &&
                                             email.isNotEmpty() &&
                                             password.isNotEmpty() &&
                                             confirmPassword.isNotEmpty() &&
                                             uiState !is AuthUiState.Loading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (uiState is AuthUiState.Loading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            "Create Account",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Link
                    var loginLinkVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(formVisible) {
                        if (formVisible) {
                            delay(500)
                            loginLinkVisible = true
                        }
                    }

                    AnimatedVisibility(
                        visible = loginLinkVisible,
                        enter = fadeIn(animationSpec = tween(600))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Already have an account? ",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                TextButton(onClick = onNavigateBack) {
                                    Text(
                                        "Login here",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Snackbars Container
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .imePadding()
        ) {
            AnimatedVisibility(
                visible = showSuccessSnackbar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SuccessSnackbar(
                    message = successMessage,
                    onDismiss = {
                        showSuccessSnackbar = false
                        successMessage = ""
                    }
                )
            }

            AnimatedVisibility(
                visible = showErrorSnackbar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ErrorSnackbar(
                    message = errorMessage,
                    onDismiss = {
                        showErrorSnackbar = false
                        errorMessage = ""
                    }
                )
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            Dialog(
                onDismissRequest = { 
                    showSuccessDialog = false
                    onNavigateBack()
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessContainer,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Akun berhasil dibuat!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Silahkan login untuk melanjutkan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessContainer
                            )
                        ) {
                            Text("Login Sekarang")
                        }
                    }
                }
            }
        }
    }
} 