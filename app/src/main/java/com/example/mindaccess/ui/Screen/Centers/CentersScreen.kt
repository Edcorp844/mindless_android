package com.example.mindaccess.ui.Screen.Centers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindaccess.Domain.Model.CenterModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CentersScreen(
    onCenterClick: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: CentersViewModel = hiltViewModel()
) {
    val centers by viewModel.centers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filteredCenters by viewModel.filteredCenters.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    CentersScreenContent(
        centers = centers,
        filteredCenters = filteredCenters,
        isLoading = isLoading,
        categories = categories,
        selectedCategory = selectedCategory,
        searchQuery = searchQuery,
        snackbarHostState = snackbarHostState,
        onCategorySelected = { viewModel.onCategorySelected(it) },
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        onCenterClick = onCenterClick,
        onSearchClick = onSearchClick
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CentersScreenContent(
    centers: List<CenterModel>,
    filteredCenters: List<CenterModel>,
    isLoading: Boolean,
    categories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    snackbarHostState: SnackbarHostState,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCenterClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChanged,
                            onSearch = { isSearchActive = false },
                            expanded = isSearchActive,
                            onExpandedChange = { isSearchActive = it },
                            placeholder = { Text("Search centers...") },
                            leadingIcon = { 
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowBack, 
                                    contentDescription = "Back", 
                                    modifier = Modifier.clickable { isSearchActive = false }
                                ) 
                            },
                            trailingIcon = { 
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Clear", 
                                        modifier = Modifier.clickable { onSearchQueryChanged("") }
                                    ) 
                                }
                            }
                        )
                    },
                    expanded = isSearchActive,
                    onExpandedChange = { isSearchActive = it },
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCenters) { center ->
                            ExpressiveCenterItem(
                                center = center,
                                onClick = { 
                                    isSearchActive = false
                                    onCenterClick(center.name) 
                                }
                            )
                        }
                    }
                }
            } else {
                val backgroundColor = lerp(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceContainer,
                    scrollBehavior.state.collapsedFraction
                )

                Surface(
                    color = backgroundColor,
                    tonalElevation = if (scrollBehavior.state.collapsedFraction > 0f) 3.dp else 0.dp
                ) {
                    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    text = "Centers",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            },
                            actions = {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                                }
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            // We already handle insets in the parent Column
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        )
                        CategoryFilterRow(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = onCategorySelected
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Official Material 3 Expressive Loading Indicator (Waving morphing shapes)
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Adaptive grid for better utilization of screen space on tablets
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 340.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredCenters) { center ->
                        ExpressiveCenterItem(
                            center = center,
                            onClick = { onCenterClick(center.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveCenterItem(
    center: CenterModel,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = center.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))
                center.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = center.category.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    center.open?.let {
                        Text(
                            text="◉ $it",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }

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
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = null
            )
        }
    }
}
