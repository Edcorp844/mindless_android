package com.example.mindaccess

import android.os.Bundle
import android.widget.ListView
import android.widget.Toolbar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mindaccess.ui.theme.ContrastAwareMindAcessTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContrastAwareMindAcessTheme(dynamicColor = true){
                MindAccessApp()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindAccessApp() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Centers",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding -> // Removed the extra { here
        // Wrap your screen in a Box or Column to apply the padding
        Box(modifier = Modifier.padding(innerPadding)) {
            CentersListScreen()
        }
    } // Removed the extra } here
}

@Composable
fun CentersListScreen() {
    val categories = listOf("All", "Hospital", "Clinic", "Pharmacy", "Laboratory")
    var selectedCategory by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. The Filter Row (Fixed at top)
        CategoryFilterRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        // 2. The List (Scrollable)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Filter your items based on the selection
            items(10) { index ->
                ExpressiveCenterItem(
                    title = "Medical Center $index",
                    description = "Open 24/7 • 2.5km away",
                    category = if (index % 2 == 0) "Hospital" else "Clinic",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun ExpressiveCenterItem(
    title: String,
    description: String,
    category: String,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large, // Modern rounded corners
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // The Category Pill (Rounded Container)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Right Side: The Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                // Google Chat/WhatsApp style uses rounded pills
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = null // Clean, borderless look
            )
        }
    }
}


@Preview
@Composable
fun GreetingPreview() {
    ContrastAwareMindAcessTheme(darkTheme = true,){

        MindAccessApp()
    }
}

@Preview
@Composable
fun LightPreview() {
    ContrastAwareMindAcessTheme(darkTheme = false,){
        MindAccessApp()
    }
}