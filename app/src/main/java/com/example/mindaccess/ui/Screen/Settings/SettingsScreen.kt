package com.example.mindaccess.ui.Screen.Settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.jeziellago.compose.markdowntext.MarkdownText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mindaccess.R
import com.example.mindaccess.Domain.Model.*
import com.example.mindaccess.ui.Screen.Auth.LoginScreen
import com.example.mindaccess.ui.Screen.Auth.RegisterScreen
import com.google.firebase.auth.FirebaseUser
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.mindaccess.ui.Screen.Auth.AuthViewModel
import com.example.mindaccess.ui.Screen.Auth.BackgroundGlows
import com.example.mindaccess.ui.Screen.Home.NotificationDialog
import com.example.mindaccess.ui.theme.ContrastAwareMindAcessTheme
import com.example.mindaccess.ui.theme.backgroundDark
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    isExpanded: Boolean = false,
    onNavigateToNotifications: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val dataUsage by viewModel.dataUsage.collectAsState()
    val currentUser by viewModel.userState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
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
                    userProfile = userProfile,
                    notifications = notifications,
                    onUpdateUser = { viewModel.updateCurrentUser() },
                    onItemClick = { 
                        if (it == "Notifications") onNavigateToNotifications()
                        else selectedItem = it 
                    },
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
                        notifications = notifications,
                        onNavigate = { 
                            if (it == "Notifications") onNavigateToNotifications()
                            else selectedItem = it 
                        },
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
                    userProfile = userProfile,
                    notifications = notifications,
                    onUpdateUser = { viewModel.updateCurrentUser() },
                    onItemClick = { 
                        if (it == "Notifications") onNavigateToNotifications()
                        else selectedItem = it 
                    }
                )
            } else {
                BackHandler {
                    selectedItem = null
                }
                SettingDetail(
                    title = targetItem,
                    viewModel = viewModel,
                    currentUser = currentUser,
                    notifications = notifications,
                    onNavigate = { 
                        if (it == "Notifications") onNavigateToNotifications()
                        else selectedItem = it 
                    },
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
    userProfile: UserProfile?,
    notifications: List<AppNotification>,
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
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
                currentUser = currentUser,
                userProfile = userProfile,
                onAccountClick = {
                    onItemClick("Account")
                }
            )

            if (currentUser != null && !currentUser.isAnonymous) {
                val unreadCount = notifications.count { !it.read }
                SettingsGroup(title = "") {
                    ListItem(
                        headlineContent = { Text("Notifications") },
                        leadingContent = {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (unreadCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            unreadCount.toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selectedItem == "Notifications") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier.clickable { onItemClick("Notifications") }
                    )
                }
            }

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
    notifications: List<AppNotification>,
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
    val userProfile by viewModel.userProfile.collectAsState()

    var isEditingProfile by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (title == "Account") "" else title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0, 0, 0, 0)),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (title == "Account" && currentUser != null && !currentUser.isAnonymous) {
                        val isPasswordProvider = currentUser.providerData.any { it.providerId == "password" }
                        if (isPasswordProvider) {
                            IconButton(onClick = { isEditingProfile = !isEditingProfile }) {
                                Icon(
                                    if (isEditingProfile) Icons.Outlined.Close else Icons.Outlined.Edit,
                                    contentDescription = "Edit Profile"
                                )
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            when (title) {
                "Account" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        BackgroundGlows()

                        val isAnonymous = currentUser?.isAnonymous ?: true
                        if (currentUser == null || isAnonymous) {
                            var isRegistering by remember { mutableStateOf(false) }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .imePadding()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                if (isRegistering) {
                                    RegisterScreen(
                                        onRegisterSuccess = { viewModel.updateCurrentUser() },
                                        onNavigateToLogin = { isRegistering = false }
                                    )
                                } else {
                                    LoginScreen(
                                        onLoginSuccess = { viewModel.updateCurrentUser() },
                                        onNavigateToRegister = { isRegistering = true }
                                    )
                                }
                            }
                        } else {
                            ProfileDetail(
                                userProfile = userProfile,
                                isEditing = isEditingProfile,
                                onEditChange = { isEditingProfile = it },
                                onSignOut = {
                                    viewModel.signOut()
                                    onBack()
                                },
                                onUpdateProfile = { name, photo ->
                                    viewModel.updateProfile(name, photo) { }
                                }
                            )
                        }
                    }
                }
                "Notifications" -> {
                    NotificationDialog(
                        notifications = notifications,
                        onDismiss = onBack,
                        onMarkAsRead = { viewModel.markAsRead(it) },
                        onMarkAllAsRead = { viewModel.markAllAsRead() }
                    )
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
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                "Version ${data.version} | Last Updated: ${data.lastUpdated}",
                                style = MaterialTheme.typography.labelMedium
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            MarkdownText(
                                markdown = data.content,
                                modifier = Modifier.fillMaxWidth()
                            )
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
    currentUser: FirebaseUser?,
    userProfile: UserProfile? = null,
    onAccountClick: () -> Unit
) {
    val isAnonymous = currentUser?.isAnonymous ?: true
    val isSignedIn = currentUser != null && !isAnonymous
    val displayName = userProfile?.displayName ?: currentUser?.displayName ?: "User"
    val photoUrl = userProfile?.photoURL ?: currentUser?.photoUrl?.toString()
    
    Surface(
        onClick = onAccountClick,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
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
                if (isSignedIn && photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
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
                    text = if (isSignedIn) displayName else "MindAccess Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isSignedIn) (currentUser?.email ?: "") else "Sign in",
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
fun ProfileDetail(
    userProfile: UserProfile?,
    isEditing: Boolean,
    onEditChange: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onUpdateProfile: (String, String?) -> Unit
) {
    var displayName by remember(userProfile) { mutableStateOf(userProfile?.displayName ?: "") }
    var photoUrl by remember(userProfile) { mutableStateOf(userProfile?.photoURL ?: "") }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUrl = it.toString()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0,0,0,0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    // Profile Image Section
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            if (photoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        if (isEditing) {
                            SmallFloatingActionButton(
                                onClick = { launcher.launch("image/*") },
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit photo",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                onUpdateProfile(displayName, photoUrl.takeIf { it.isNotEmpty() })
                                onEditChange(false)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Save Changes")
                        }
                    } else {
                        Text(
                            text = userProfile?.displayName ?: "No Name",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = userProfile?.role ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }



                if (!isEditing) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        SettingsGroup(title = "") {
                            ListItem(
                                headlineContent = { Text("Email") },
                                supportingContent = { Text(userProfile?.email ?: "") },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        SettingsGroup(title = "") {
                            ListItem(
                                headlineContent = { Text("Provider") },
                                supportingContent  = { Text(userProfile?.provider ?: "Email") },
                                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            userProfile?.createdAt?.let {
                                ListItem(
                                    headlineContent = { Text("Member Since") },
                                    supportingContent  = {
                                        val date = it.toDate()
                                        Text(
                                            DateFormat.getMediumDateFormat(LocalContext.current)
                                                .format(date)
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                                )
                            }
                        }
                    }
                }
            }

            // Logout at the very bottom
            if (!isEditing) {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sign Out")
                }
            }
        }
    }
}

class DammyVieModel{
    init{}
    public fun signOut(){}
    public fun updateProfile(name: String, photo: String?){

    }
    public fun onBack(){}
    public fun updateCurrentUser(){}
}

class AuthViewModel{
    init {

    }
}

/*
@Preview(showSystemUi = true )
@Composable
fun ProfileDetailPreview() {
    val profile = UserProfile(
        displayName = "John Doe",
        email = "john@doe.com",
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now(),
        role = "user"
    )
    val viewModel = DammyVieModel()
    val isEditing = false
    var isRegistering by remember { mutableStateOf(false) }

    ContrastAwareMindAcessTheme(darkTheme = true, dynamicColor = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0, 0, 0, 0)),
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                BackgroundGlows()
                if (isRegistering) {
                    RegisterScreen(
                        onRegisterSuccess = { viewModel.updateCurrentUser() },
                        onNavigateToLogin = { isRegistering = false }
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = { viewModel.updateCurrentUser() },
                        onNavigateToRegister = { isRegistering = true },
                        viewModel = AuthViewModel()
                    )
                }
            }
        }

    }
}
*/
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
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(content = content)
        }
    }
}
