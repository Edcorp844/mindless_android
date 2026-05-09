package com.example.mindaccess.ui.Screen.Auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    //viewModel: AuthViewModel = AuthViewModel()
) {
    val user by viewModel.userState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    /*val user = null
    val isLoading = false
    val error = null
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current*/

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
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color(0,0,0,0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Logo()
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp).animateContentSize()
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Banner
                    AnimatedVisibility(visible = error != null) {
                        error?.let {
                            Column {
                                ErrorMessage(it)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    // Reset Sent Banner
                    AnimatedVisibility(visible = resetSent) {
                        Column {
                            SuccessMessage("Password reset email sent! Check your inbox.")
                            Spacer(modifier = Modifier.height(16.dp))
                        }
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
                                   val serverClientId = context.getString(R.string.default_web_client_id)
                                    android.util.Log.d("LoginScreen", "Using Client ID: $serverClientId")
                                    
                                    if (serverClientId.isEmpty()) {
                                        viewModel.setError("Internal Error: Web Client ID not found in resources.")
                                        return@launch
                                    }

                                    val credentialManager = CredentialManager.create(context)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                       .setServerClientId(serverClientId)
                                        .setAutoSelectEnabled(false)
                                        .setNonce("random_nonce_for_testing_" + System.currentTimeMillis())
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    val result = credentialManager.getCredential(context, request)
                                    val credential = result.credential

                                    android.util.Log.d("LoginScreen", "Credential received: ${credential.type}")

                                    if (credential is GoogleIdTokenCredential) {
                                        val firebaseCredential =
                                            GoogleAuthProvider.getCredential(credential.idToken, null)
                                        viewModel.signInWithCredential(firebaseCredential, onLoginSuccess)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("LoginScreen", "Google Sign-In Error Type: ${e.javaClass.simpleName}")
                                    android.util.Log.e("LoginScreen", "Google Sign-In Error Message: ${e.message}")
                                    
                                    val message = e.message ?: ""
                                    val friendlyMessage = when {
                                        message.contains("No credentials available", ignoreCase = true) -> 
                                            "No Google accounts found or App not authorized.\n\n" +
                                            "Verify:\n" +
                                            "1. Phone has a Google Account.\n" +
                                            "2. SHA-1 matches Firebase Console.\n" +
                                            "3. Google Provider is 'Enabled' in Firebase."
                                        else -> "Error: ${e.javaClass.simpleName} - ${e.message}"
                                    }
                                    viewModel.setError(friendlyMessage)
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

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Form
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL",
                        placeholder = "you@example.com",
                        icon = Icons.Default.Email,
                        imeAction = ImeAction.Next
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
                            onTogglePassword = { showPassword = !showPassword },
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.signIn(email, password, onLoginSuccess)
                                    }
                                    focusManager.clearFocus()
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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

