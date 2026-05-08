package com.example.mindaccess.ui.Screen.Auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindaccess.R
import com.example.mindaccess.ui.Components.LoadingIndicator
import com.example.mindaccess.ui.Components.LoadingIndicatorType
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user by viewModel.userState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var resetSent by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background Glows
        BackgroundGlows()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Logo()

            Spacer(modifier = Modifier.height(40.dp))

            // Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Sign in to continue to MindAccess.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Error Banner
                    AnimatedVisibility(visible = error != null) {
                        error?.let {
                            ErrorMessage(it)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Reset Sent Banner
                    AnimatedVisibility(visible = resetSent) {
                        SuccessMessage("Password reset email sent! Check your inbox.")
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Social Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialButton(
                            modifier = Modifier.weight(1f),
                            text = "Google",
                            icon = painterResource(id = R.drawable.ic_google)
                        ) {
                            coroutineScope.launch {
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(context.getString(R.string.default_web_client_id))
                                        .setAutoSelectEnabled(true)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(context, request)
                                    val credential = result.credential

                                    if (credential is GoogleIdTokenCredential) {
                                        val firebaseCredential =
                                            GoogleAuthProvider.getCredential(credential.idToken, null)
                                        viewModel.signInWithCredential(firebaseCredential, onLoginSuccess)
                                    }
                                } catch (e: Exception) {
                                    // Handle exception
                                }
                            }
                        }
                        SocialButton(
                            modifier = Modifier.weight(1f),
                            text = "Apple",
                            icon = painterResource(id = R.drawable.ic_apple)
                        ) {
                            val activity = context as? android.app.Activity
                            if (activity != null) {
                                viewModel.signInWithOAuth(activity, "apple.com", onLoginSuccess)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL",
                        placeholder = "you@example.com",
                        icon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "PASSWORD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Forgot password?",
                                modifier = Modifier.clickable {
                                    if (email.isNotBlank()) {
                                        viewModel.resetPassword(email) { resetSent = true }
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        AuthTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = null,
                            placeholder = "••••••••",
                            icon = Icons.Default.Lock,
                            isPassword = true,
                            showPassword = showPassword,
                            onTogglePassword = { showPassword = !showPassword }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.signIn(email, password, onLoginSuccess) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        if (isLoading) {
                            LoadingIndicator(type = LoadingIndicatorType.WAVY)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sign In", fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GuestButton(
                        onClick = { viewModel.signInAnonymously(onLoginSuccess) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Create one",
                    modifier = Modifier.clickable { onNavigateToRegister() },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun BackgroundGlows() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-150).dp, y = (-150).dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF34C759).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 150.dp, y = 150.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF007AFF).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun Logo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Simple MindAccess Logo approximation
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_compass), // Replace with actual logo
                    contentDescription = null,
                    tint = Color(0xFF34C759)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = buildAnnotatedString {
                append("Mind")
                withStyle(SpanStyle(color = Color(0xFF34C759))) {
                    append("Access")
                }
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String?,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedBorderColor = Color(0xFF34C759),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email),
            singleLine = true
        )
    }
}

@Composable
fun SocialButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun GuestButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = "Continue as Guest",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SuccessMessage(message: String) {
    Surface(
        color = Color(0xFF34C759).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF34C759).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF34C759), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF34C759))
        }
    }
}

