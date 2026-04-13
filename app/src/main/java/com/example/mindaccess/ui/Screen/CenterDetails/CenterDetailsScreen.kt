package com.example.mindaccess.ui.Screen.CenterDetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.Domain.Model.GeoLocationCordModel
import java.util.Hashtable
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenterDetailsScreen(
    onBackClick: () -> Unit,
    viewModel: CenterDetailsViewModel = hiltViewModel()
) {
    val center by viewModel.center.collectAsState()
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        text = center?.name ?: "Loading...",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        center?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = data.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Description")
                Text(
                    text = data.description ?: "No description available.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Visiting Hours")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = data.workingDays ?: "Not specified")
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Contact")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.contact?.get("phone")?.let { phone ->
                        ContactButton(Icons.Default.Call, "Call") {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                            context.startActivity(intent)
                        }
                    }
                    data.contact?.get("email")?.let { email ->
                        ContactButton(Icons.Default.Email, "Email") {
                            val intent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())
                            context.startActivity(intent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val gmmIntentUri =
                            "geo:${data.location.latitude},${data.location.longitude}?q=${data.name}".toUri()
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Directions")
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Services")
                data.services?.forEach { service ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("•", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        Text(text = service, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun CenterScreenPreview(){
    val context = LocalContext.current
    val lat = 40.7128 + (Random.nextDouble() - 0.5) * 0.1
    val lon = -74.0060 + (Random.nextDouble() - 0.5) * 0.1

    val center = CenterModel(
        name = "Center Health Center IV Kra Municipality",
        description = "This is a description for center located in New York. This should test how the biges center dec looks like. Not probably the biggest but its ideal" +
                "So here we are trying to convience peaople..just pray for us",
        location = GeoLocationCordModel(lat, lon),
        category = listOf("Medical", "Mental Health", "Community", "Youth").random(),
        workingDays = "Mon - Fri, 9AM - 5PM",
        contact = Hashtable<String, String>().apply {
            put("phone", "555-01098-3667)")
            put("email", "center@example.com")
        },
        services = listOf("Consultation", "Therapy", "Workshops", "Support Groups, Consultation, Therapy, Workshops, Support Groups","Support Groups, Consultation, Therapy, Workshops, Support Groups","Consultation", "Therapy", "Workshops",  ).shuffled().take(10)
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(
                    text = center.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold
                ) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        val center = center
        center?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                Text(
                    text = data.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("Description")
                Text(
                    text = data.description ?: "No description available.",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Visiting Hours")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = data.workingDays ?: "Not specified")
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Contact")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.contact?.get("phone")?.let { phone ->
                        ContactButton(Icons.Default.Call, "Call") {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                            context.startActivity(intent)
                        }
                    }
                    data.contact?.get("email")?.let { email ->
                        ContactButton(Icons.Default.Email, "Email") {
                            val intent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri())
                            context.startActivity(intent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val gmmIntentUri =
                            "geo:${data.location.latitude},${data.location.longitude}?q=${data.name}".toUri()
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Directions")
                }

                Spacer(modifier = Modifier.height(24.dp))

                SectionTitle("Services")
                data.services?.forEach { service ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("•", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        Text(text = service, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
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
