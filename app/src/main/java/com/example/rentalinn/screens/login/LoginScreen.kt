package com.example.rentalinn.screens.login

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rentalinn.R
import com.example.rentalinn.navigation.Screen
import com.example.rentalinn.viewmodel.AuthUiState
import com.example.rentalinn.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onNavigateToRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val viewModel: AuthViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Validation functions
    fun validateEmail(): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return when {
            email.isEmpty() -> {
                emailError = "Email is required"
                false
            }
            !email.contains("@") -> {
                emailError = "Email must contain @ symbol"
                false
            }
            !emailRegex.matches(email) -> {
                emailError = "Please enter a valid email address"
                false
            }
            else -> {
                emailError = ""
                true
            }
        }
    }

    fun validatePassword(): Boolean {
        return when {
            password.isEmpty() -> {
                passwordError = "Password is required"
                false
            }
            else -> {
                passwordError = ""
                true
            }
        }
    }

    fun validateForm(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        return isEmailValid && isPasswordValid
    }

    // Handle UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                // Show success message
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (uiState as AuthUiState.Success).message ?: "Login successful!",
                        duration = SnackbarDuration.Short
                    )
                }
                
                // Clear form
                email = ""
                password = ""
                
                // Navigate based on role
                delay(1500)
                val user = (uiState as AuthUiState.Success).user
                when (user?.role) {
                    "admin" -> {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    "user" -> {
                        navController.navigate(Screen.UserDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (uiState as AuthUiState.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))

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
                            .size(140.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.3f)
                            )
                            .background(
                                Color.White,
                                shape = CircleShape
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logonjay),
                            contentDescription = "RentalInn Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

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
                            text = "Rentalin",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign in to your RentalInn account",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

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
                                        Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { passwordFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = emailError.isNotEmpty(),
                                supportingText = if (emailError.isNotEmpty()) {
                                    { Text(emailError, color = MaterialTheme.colorScheme.error) }
                                } else null,
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
                                },
                                label = {
                                    Text(
                                        "Password",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Password",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            if (isPasswordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None
                                                    else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { keyboardController?.hide() }
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(passwordFocusRequester),
                                isError = passwordError.isNotEmpty(),
                                supportingText = if (passwordError.isNotEmpty()) {
                                    { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    if (validateForm()) {
                                        keyboardController?.hide()
                                        viewModel.login(email, password)
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
                                        "Sign In",
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

                // Register Link
                var registerVisible by remember { mutableStateOf(false) }

                LaunchedEffect(formVisible) {
                    if (formVisible) {
                        delay(500)
                        registerVisible = true
                    }
                }

                AnimatedVisibility(
                    visible = registerVisible,
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
                        TextButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                "Don't have an account? Register here",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}