package com.example.mindaccess.ui.Screen.CenterDetails

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.net.toUri
import com.example.mindaccess.Domain.Model.CategoryIconModel
import com.example.mindaccess.Domain.Model.CenterCategoryModel
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Model.GeoLocationCordModel
import com.example.mindaccess.ui.Components.LoadingIndicator
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenterDetailsScreen(
    onBackClick: () -> Unit,
    onDirectionsClick: (CenterModel) -> Unit = {},
    viewModel: CenterDetailsViewModel = hiltViewModel()
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
        onDirectionsClick = onDirectionsClick
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
    onDirectionsClick: (CenterModel) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val contact = data.contact
                        val isContactAvailable = !contact.isNullOrBlank()
                        
                        Button(
                            onClick = {
                                if (isContactAvailable) {
                                    val intent = Intent(Intent.ACTION_DIAL, "tel:$contact".toUri())
                                    context.startActivity(intent)
                                }
                            },
                            enabled = isContactAvailable,
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            shape = ButtonDefaults.filledTonalShape,
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call")
                        }

                        Button(
                            onClick = { data.let { onDirectionsClick(it) } },
                            shape = ButtonDefaults.filledTonalShape,
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Directions")
                        }
                    }

                    data.contact?.let { contact ->
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionTitle("Contact: ")
                            Text(text = contact, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

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
        description = "This is a description for center located in New York. This should test how the biges center dec looks like. Not probably the biggest but its ideal" +
                "So here we are trying to convience peaople..just pray for us",
        location = GeoLocationCordModel(lat, lon),
        category = CenterCategoryModel(1, "Medical", "#FF0000", CategoryIconModel("health")),
        open = "Mon - Fri, 9AM - 5PM",
        contact = "555-01098-3667",
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
