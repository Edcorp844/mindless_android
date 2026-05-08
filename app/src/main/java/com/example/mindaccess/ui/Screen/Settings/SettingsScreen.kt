package com.example.mindaccess.ui.Screen.Settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mindaccess.R
import com.example.mindaccess.Domain.Model.*
import com.google.firebase.auth.FirebaseUser
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    isExpanded: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val dataUsage by viewModel.dataUsage.collectAsState()
    val currentUser by viewModel.userState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var selectedItem by remember { mutableStateOf<String?>(null) }

    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Master list
            Box(modifier = Modifier.weight(1.3f)) {
                SettingsListContent(
                    locationEnabled = locationEnabled,
                    onLocationEnabledChange = { viewModel.setLocationEnabled(it) },
                    dataUsage = dataUsage,
                    currentUser = currentUser,
                    onUpdateUser = { viewModel.updateCurrentUser() },
                    onItemClick = { selectedItem = it },
                    selectedItem = selectedItem
                )
            }
            VerticalDivider()
            // Details
            Box(modifier = Modifier.weight(1.5f)) {
                if (selectedItem != null) {
                    SettingDetail(
                        title = selectedItem!!,
                        viewModel = viewModel,
                        currentUser = currentUser,
                        onNavigate = { selectedItem = it },
                        onBack = { selectedItem = null }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a setting to view details", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    } else {
        AnimatedContent(
            targetState = selectedItem,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "settings_navigation"
        ) { targetItem: String? ->
            if (targetItem == null) {
                SettingsListContent(
                    locationEnabled = locationEnabled,
                    onLocationEnabledChange = { viewModel.setLocationEnabled(it) },
                    dataUsage = dataUsage,
                    currentUser = currentUser,
                    onUpdateUser = { viewModel.updateCurrentUser() },
                    onItemClick = { selectedItem = it }
                )
            } else {
                BackHandler {
                    selectedItem = null
                }
                SettingDetail(
                    title = targetItem,
                    viewModel = viewModel,
                    currentUser = currentUser,
                    onNavigate = { selectedItem = it },
                    onBack = { selectedItem = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListContent(
    locationEnabled: Boolean,
    onLocationEnabledChange: (Boolean) -> Unit,
    dataUsage: String,
    currentUser: FirebaseUser?,
    onUpdateUser: () -> Unit,
    onItemClick: (String) -> Unit,
    selectedItem: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
    val versionName = packageInfo.versionName
    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION: ACCOUNT (iOS Style Cloud Account Tile) ---
            AccountTile(
                user = null,
                currentUser = currentUser,
                onAccountClick = {
                    onItemClick("Account")
                }
            )

            // --- SECTION: PERMISSIONS AND USAGE ---
            SettingsGroup(title = "Permissions And Usage") {
                ListItem(
                    headlineContent = { Text("Location Zoning") },
                    leadingContent = { 
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        ExpressiveSwitch(checked = locationEnabled, onCheckedChange = onLocationEnabledChange)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
                Spacer(modifier = Modifier.height(1.dp))
                ListItem(
                    headlineContent = { Text("Usage Tracking") },
                    leadingContent = { Icon(Icons.Outlined.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Text(dataUsage) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Usage Tracking") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Usage Tracking") }
                )
            }

            // System Permissions Button (Standalone style)
            SettingsGroup(title = "") {
                ListItem(
                    headlineContent = { Text("System Permissions") },
                    leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { 
                        Button(
                            onClick = { openAppSettings() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Open", fontSize = 12.sp)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                )
            }

            SettingsGroup(title = "Legal and Regulatory") {
                ListItem(
                    headlineContent = { Text("Terms & Conditions") },
                    leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Terms & Conditions") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Terms & Conditions") }
                )
                Spacer(modifier = Modifier.height(1.dp))
                ListItem(
                    headlineContent = { Text("Open Source Licenses") },
                    leadingContent = { Icon(Icons.Outlined.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Licenses") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Licenses") }
                )
            }

            SettingsGroup(title = "Support") {
                ListItem(
                    headlineContent = { Text("Help & Support") },
                    leadingContent = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Help and Support") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Help and Support") }
                )
                Spacer(modifier = Modifier.height(1.dp))
                ListItem(
                    headlineContent = { Text("FAQ's") },
                    leadingContent = { Icon(Icons.Outlined.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Frequently asked questions") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Frequently asked questions") }
                )
            }

            // --- SECTION: ABOUT ---
            SettingsGroup(title = "About Mind Access") {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    headlineContent = { Text("Version") },
                    leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Text(
                            text = "$versionName ($versionCode)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDetail(
    title: String,
    viewModel: SettingsViewModel,
    currentUser: FirebaseUser?,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val termsState by viewModel.termsState.collectAsState()
    val licenseState by viewModel.licenseState.collectAsState()
    val helpState by viewModel.helpState.collectAsState()
    val faqState by viewModel.faqState.collectAsState()
    val dataUsage by viewModel.dataUsage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (title) {
                "Account" -> {
                    if (currentUser == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Spacer(modifier = Modifier.height(40.dp))
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = RoundedCornerShape(30.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                text = "Sign in to Mind Access",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Sign in with your Google account or email to sync your mind data, centers, and preferences across all your devices.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))

                            var useEmail by remember { mutableStateOf(false) }
                            var isSignUp by remember { mutableStateOf(false) }
                            var email by remember { mutableStateOf("") }
                            var password by remember { mutableStateOf("") }
                            var errorMessage by remember { mutableStateOf<String?>(null) }
                            var isLoading by remember { mutableStateOf(false) }

                            if (!useEmail) {
                                Button(
                                    onClick = {
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
                                                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                                                    com.google.firebase.auth.FirebaseAuth.getInstance()
                                                        .signInWithCredential(firebaseCredential)
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                viewModel.updateCurrentUser()
                                                            }
                                                        }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign in with Google")
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = { useEmail = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Continue with Email")
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it; errorMessage = null },
                                        label = { Text("Email") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                                    )

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it; errorMessage = null },
                                        label = { Text("Password") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                                    )

                                    if (errorMessage != null) {
                                        Text(
                                            text = errorMessage!!,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            isLoading = true
                                            val callback: (Boolean, String?) -> Unit = { success, error ->
                                                isLoading = false
                                                if (!success) {
                                                    errorMessage = error
                                                }
                                            }
                                            if (isSignUp) {
                                                viewModel.signUpWithEmail(email, password, callback)
                                            } else {
                                                viewModel.signInWithEmail(email, password, callback)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        enabled = email.isNotBlank() && password.isNotBlank() && !isLoading
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(if (isSignUp) "Create Account" else "Sign In")
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { useEmail = false }) {
                                            Text("Back to Google")
                                        }
                                        TextButton(onClick = { isSignUp = !isSignUp }) {
                                            Text(if (isSignUp) "Already have an account?" else "Create account")
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(100.dp),
                                shape = RoundedCornerShape(50.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (currentUser.photoUrl != null) {
                                    AsyncImage(
                                        model = currentUser.photoUrl,
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = currentUser.displayName ?: "Not Signed In",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser.email ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Button(
                                onClick = {
                                    viewModel.signOut()
                                    onBack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign Out")
                            }
                        }
                    }
                }
                "Usage Tracking" -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Data Usage Details", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Total App Data: $dataUsage", style = MaterialTheme.typography.bodyLarge)
                        Text("Includes both sent and received bytes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                "Terms & Conditions" -> {
                    LegalContent(state = termsState) { data ->
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                            Text("Version ${data.version} | Last Updated: ${data.lastUpdated}", style = MaterialTheme.typography.labelMedium)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(data.content)
                        }
                    }
                }
                "Licenses" -> {
                    LegalContent(state = licenseState) { data ->
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Software Licenses",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = data.platform,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Last Updated: ${data.lastUpdated}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                            items(data.licenses) { license ->
                                var showFullLicense by remember { mutableStateOf(false) }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showFullLicense = !showFullLicense }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .animateContentSize()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = license.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = license.licenseType,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (license.url != null) {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(license.url))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Launch,
                                                    contentDescription = "Open source website",
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (showFullLicense) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    if (showFullLicense) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Text(
                                                text = license.content,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(12.dp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                            item { Spacer(modifier = Modifier.height(32.dp)) }
                        }
                    }
                }
                "Help and Support" -> {
                    LegalContent(state = helpState) { data ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "How can we help?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = data.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            data.contact?.let { contact ->
                                Text(
                                    text = "Contact Support",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    contact.email?.let { email ->
                                        ListItem(
                                            headlineContent = { Text("Email Support") },
                                            supportingContent = { Text(email) },
                                            leadingContent = {
                                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            },
                                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                        this.data = Uri.parse("mailto:$email")
                                                    }
                                                    context.startActivity(intent)
                                                }
                                        )
                                    }
                                    contact.phone?.let { phone ->
                                        ListItem(
                                            headlineContent = { Text("Phone Support") },
                                            supportingContent = { Text(phone) },
                                            leadingContent = {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            },
                                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .clickable {
                                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                                        this.data = Uri.parse("tel:$phone")
                                                    }
                                                    context.startActivity(intent)
                                                }
                                        )
                                    }
                                }
                            }

                            data.faq?.let { faqText ->
                                Spacer(modifier = Modifier.height(24.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    onClick = { onNavigate("Frequently asked questions") }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QuestionAnswer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Have questions?",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = faqText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                "Frequently asked questions" -> {
                    LegalContent(state = faqState) { data ->
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Frequently Asked Questions",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Last Updated: ${data.lastUpdated}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            items(data.items) { faq ->
                                FaqItem(faq)
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Details for $title would go here.")
                    }
                }
            }
        }
    }
}

@Composable
fun <T> LegalContent(state: LegalState<T>, content: @Composable (T) -> Unit) {
    when (state) {
        is LegalState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is LegalState.Success -> {
            content(state.data)
        }
        is LegalState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ExpressiveSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        }
    )
}

@Composable
fun FaqItem(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AccountTile(
    user: FirebaseUser?,
    currentUser: FirebaseUser?,
    onAccountClick: () -> Unit
) {
    val displayUser = currentUser ?: user
    
    Surface(
        onClick = onAccountClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / Profile Image
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (displayUser?.photoUrl != null) {
                    AsyncImage(
                        model = displayUser.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayUser?.displayName ?: "Sign in to Mind Access",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = displayUser?.email ?: "Google Account, Cloud, & Sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}
