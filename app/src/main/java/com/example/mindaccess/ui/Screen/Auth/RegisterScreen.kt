package com.example.mindaccess.ui.Screen.Auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindaccess.ui.Components.LoadingIndicator
import com.example.mindaccess.ui.Components.LoadingIndicatorType

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user by viewModel.userState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundGlows()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Logo()

            Spacer(modifier = Modifier.height(40.dp))

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
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Join MindAccess to start your journey.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedVisibility(visible = error != null) {
                        error?.let {
                            ErrorMessage(it)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    AuthTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "FULL NAME",
                        placeholder = "John Doe",
                        icon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL",
                        placeholder = "you@example.com",
                        icon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "PASSWORD",
                        placeholder = "••••••••",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "CONFIRM PASSWORD",
                        placeholder = "••••••••",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        showPassword = showPassword,
                        onTogglePassword = { showPassword = !showPassword }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (password == confirmPassword) {
                                viewModel.signUp(email, password, name, onRegisterSuccess)
                            } else {
                                // Handled by simple check for now, can be improved in ViewModel
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && password == confirmPassword
                    ) {
                        if (isLoading) {
                            LoadingIndicator(type = LoadingIndicatorType.WAVY)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sign Up", fontWeight = FontWeight.Black)
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
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    modifier = Modifier.clickable { onNavigateToLogin() },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
