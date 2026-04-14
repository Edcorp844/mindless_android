package com.example.mindaccess.ui.Screen.Settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(isExpanded: Boolean = false) {
    var getUpdates by remember { mutableStateOf(true) }
    var allowNotifications by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }
    
    var selectedItem by remember { mutableStateOf<String?>(null) }

    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Master list
            Box(modifier = Modifier.weight(1.3f)) {
                SettingsListContent(
                    getUpdates = getUpdates,
                    onGetUpdatesChange = { getUpdates = it },
                    allowNotifications = allowNotifications,
                    onAllowNotificationsChange = { allowNotifications = it },
                    locationEnabled = locationEnabled,
                    onLocationEnabledChange = { locationEnabled = it },
                    onItemClick = { selectedItem = it },
                    selectedItem = selectedItem
                )
            }
            VerticalDivider()
            // Details
            Box(modifier = Modifier.weight(1.5f)) {
                if (selectedItem != null) {
                    SettingDetail(selectedItem!!)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Select a setting to view details", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    } else {
        SettingsListContent(
            getUpdates = getUpdates,
            onGetUpdatesChange = { getUpdates = it },
            allowNotifications = allowNotifications,
            onAllowNotificationsChange = { allowNotifications = it },
            locationEnabled = locationEnabled,
            onLocationEnabledChange = { locationEnabled = it },
            onItemClick = { /* On mobile, we might navigate if we had a NavController here */ }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListContent(
    getUpdates: Boolean,
    onGetUpdatesChange: (Boolean) -> Unit,
    allowNotifications: Boolean,
    onAllowNotificationsChange: (Boolean) -> Unit,
    locationEnabled: Boolean,
    onLocationEnabledChange: (Boolean) -> Unit,
    onItemClick: (String) -> Unit,
    selectedItem: String? = null
) {
    val context = LocalContext.current
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECTION 1: COMMUNICATIONS ---
            SettingsGroup(title = "Communications") {
                ListItem(
                    headlineContent = { Text("App Updates") },
                    leadingContent = { 
                        Icon(Icons.Outlined.Update, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        ExpressiveSwitch(checked = getUpdates, onCheckedChange = onGetUpdatesChange)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
                Spacer(modifier = Modifier.height(1.dp))
                ListItem(
                    headlineContent = { Text("Notifications") },
                    leadingContent = { 
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        ExpressiveSwitch(checked = allowNotifications, onCheckedChange = onAllowNotificationsChange)
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                )
            }

            // --- SECTION 2: PERMISSIONS ---
            SettingsGroup(title = "Permissions") {
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
                    headlineContent = { Text("System Permissions") },
                    leadingContent = { Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { onItemClick("System Permissions") },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "System Permissions") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    )
                )
                Spacer(modifier = Modifier.height(1.dp))
                ListItem(
                    headlineContent = { Text("Usage Tracking") },
                    leadingContent = { Icon(Icons.Outlined.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Usage Tracking") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Usage Tracking") }
                )
            }

            // --- SECTION 3: ABOUT ---
            SettingsGroup(title = "About Mind Access") {
                ListItem(
                    headlineContent = { Text("Help & Support") },
                    leadingContent = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selectedItem == "Help & Support") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.clickable { onItemClick("Help & Support") }
                )
                Spacer(modifier = Modifier.height(1.dp))
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
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    headlineContent = { Text("Software Version") },
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

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDetail(title: String) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp)) {
            Text("Details for $title would go here. In a real app, this might be another set of options or descriptive text.")
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
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

