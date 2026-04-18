package com.example.mindaccess.ui.Screen.CenterDetails

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.net.toUri
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Model.Coordinates
import com.example.mindaccess.Domain.Model.CenterCategory
import com.example.mindaccess.Domain.Model.CategoryIcon
import com.example.mindaccess.Domain.Model.ContactInfo
import com.example.mindaccess.Domain.Model.ContactEmail
import com.example.mindaccess.ui.Components.LoadingIndicator
import com.example.mindaccess.ui.Components.LoadingIndicatorType
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenterDetailsScreen(
    onBackClick: () -> Unit,
    onDirectionsClick: (CenterModel) -> Unit = {},
    viewModel: CenterDetailsViewModel = hiltViewModel(),
    isDirectionsLoading: Boolean = false
) {
    val center by viewModel.center.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    CenterDetailsContent(
        center = center,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        isExpanded = false,
        onDirectionsClick = onDirectionsClick,
        isDirectionsLoading = isDirectionsLoading
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenterDetailsContent(
    modifier: Modifier = Modifier,
    center: CenterModel?,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState? = null,
    onBackClick: (() -> Unit)? = null,
    isExpanded: Boolean = false,
    onDirectionsClick: (CenterModel) -> Unit = {},
    isDirectionsLoading: Boolean = false
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showPhoneDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    val contact = center?.contact
    val phones = contact?.phone?.filterNot { it.isBlank() } ?: emptyList()
    val emails = contact?.email ?: emptyList()

    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("Select Phone Number") },
            text = {
                Column {
                    phones.forEach { phone ->
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                                context.startActivity(intent)
                                showPhoneDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(phone)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Select Email Address") },
            text = {
                Column {
                    emails.forEach { email ->
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, "mailto:${email.email}".toUri())
                                context.startActivity(intent)
                                showEmailDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${email.type}: ${email.email}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = center?.name ?: if (isLoading) "Loading..." else "Not Found",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    if (onBackClick != null && !isExpanded) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = if (isExpanded) WindowInsets(0, 0, 0, 0) else WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            center?.let { data ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    data.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    data.open?.let { workingDays ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = workingDays,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (phones.size > 1) {
                                    showPhoneDialog = true
                                } else if (phones.size == 1) {
                                    val intent = Intent(Intent.ACTION_DIAL, "tel:${phones[0]}".toUri())
                                    context.startActivity(intent)
                                }
                            },
                            enabled = phones.isNotEmpty(),
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            shape = ButtonDefaults.filledTonalShape,
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call")
                        }

                        Button(
                            onClick = {
                                if (emails.size > 1) {
                                    showEmailDialog = true
                                } else if (emails.size == 1) {
                                    val intent = Intent(Intent.ACTION_SENDTO, "mailto:${emails[0].email}".toUri())
                                    context.startActivity(intent)
                                }
                            },
                            enabled = emails.isNotEmpty(),
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            shape = ButtonDefaults.filledTonalShape,
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Email")
                        }

                        Button(
                            onClick = { 
                                data.let { 
                                    onDirectionsClick(it) 
                                } 
                            },
                            enabled = !isDirectionsLoading,
                            shape = ButtonDefaults.filledTonalShape,
                        ) {
                            if (isDirectionsLoading) {
                                LoadingIndicator(
                                    modifier = Modifier.size(24.dp),
                                    type = LoadingIndicatorType.WAVY
                                )
                            } else {
                                Icon(Icons.Default.Directions, contentDescription = null)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Directions", softWrap = false)
                        }
                    }

                    data.contact?.let { contactInfo ->
                        val phones = contactInfo.phone.orEmpty()
                        val emails = contactInfo.email.orEmpty()
                        
                        if (phones.isNotEmpty() || emails.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionTitle("Contacts")
                            Spacer(modifier = Modifier.height(8.dp))

                            if (phones.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = phones.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            emails.forEach { email ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${email.type}: ${email.email}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    if (data.services.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionTitle("Services")
                        Spacer(modifier = Modifier.height(8.dp))
                        data.services.forEach { service ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "◉",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(text = service, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                }
            } ?: run {
                Box(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Center not found", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview(showBackground = true)
@Composable
fun CenterScreenPreview() {
    val lat = 40.7128 + (Random.nextDouble() - 0.5) * 0.1
    val lon = -74.0060 + (Random.nextDouble() - 0.5) * 0.1

    val center = CenterModel(
        id = 1,
        name = "Center Health Center IV Kra Municipality",
        description = "This is a description for center located in New York.",
        coordinates = Coordinates(lat, lon),
        category = CenterCategory(1, "Medical", "#FF0000", CategoryIcon("health")),
        open = "Mon - Fri, 9AM - 5PM",
        contact = ContactInfo(
            phone = listOf("555-01098-3667"),
            email = listOf(ContactEmail("Work", "contact@center.com")),
            other = emptyList()
        ),
        services = listOf("Consultation", "Therapy", "Workshops", "Support Groups")
    )

    MaterialTheme {
        CenterDetailsScreen(onBackClick = {})
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun ContactButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}
