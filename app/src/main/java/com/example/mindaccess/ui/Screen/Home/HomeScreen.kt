package com.example.mindaccess.ui.Screen.Home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mapbox.maps.CameraOptions
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.mindaccess.Domain.Model.CenterModel
import com.example.mindaccess.ui.Components.LoadingIndicator
import com.example.mindaccess.ui.Components.LoadingIndicatorType
import com.example.mindaccess.ui.Screen.CenterDetails.CenterDetailsContent
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.geojson.LineString
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationState
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.example.mindaccess.ui.Screen.Settings.SettingsViewModel
import com.google.android.gms.tasks.Task
import android.location.Location

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    onCenterClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    pendingDirectionsCenterId: Int? = null,
    onDirectionsHandled: () -> Unit = {},
    isCalculatingRoute: Boolean = false,
    onCalculatingRouteChange: (Boolean) -> Unit = {}
) {
    val centers by viewModel.centers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val locationEnabled by settingsViewModel.locationEnabled.collectAsState()

    var selectedCenter by remember { mutableStateOf<CenterModel?>(null) }
    var isNavigating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val isDarkMode = isSystemInDarkTheme()
    val mapStyle = if (isDarkMode) Style.DARK else Style.MAPBOX_STREETS
    
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    var navigationRoutes by remember { mutableStateOf<List<NavigationRoute>>(emptyList()) }
    
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(32.5825, 0.3476)) // Default to Kampala
            zoom(11.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    val mapboxNavigation = remember {
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup(
                NavigationOptions.Builder(context)
                    .build()
            )
        }
        MapboxNavigationApp.current()
    }

    val routesObserver = remember {
        RoutesObserver { routeUpdate ->
            navigationRoutes = routeUpdate.navigationRoutes
        }
    }

    DisposableEffect(mapboxNavigation) {
        mapboxNavigation?.registerRoutesObserver(routesObserver)
        onDispose {
            mapboxNavigation?.unregisterRoutesObserver(routesObserver)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Function to request routes
    val requestRoute = remember(mapboxNavigation, fusedLocationClient, snackbarHostState) {
        { center: CenterModel, startImmediately: Boolean ->
            onCalculatingRouteChange(true)
            val destination = Point.fromLngLat(center.coordinates.longitude, center.coordinates.latitude)
            
            val requestMapboxRoute = { origin: Point ->
                mapboxNavigation?.requestRoutes(
                    RouteOptions.builder()
                        .applyDefaultNavigationOptions()
                        .applyLanguageAndVoiceUnitOptions(context)
                        .coordinatesList(listOf(origin, destination))
                        .alternatives(false)
                        .build(),
                    object : NavigationRouterCallback {
                        override fun onRoutesReady(
                            routes: List<NavigationRoute>,
                            routerOrigin: String
                        ) {
                            onCalculatingRouteChange(false)
                            mapboxNavigation?.setNavigationRoutes(routes)
                            
                            if (startImmediately) {
                                isNavigating = true
                            } else {
                                routes.firstOrNull()?.directionsRoute?.geometry()?.let { geometry ->
                                    val points = LineString.fromPolyline(geometry, 6).coordinates()
                                    if (points.isNotEmpty()) {
                                        scope.launch {
                                            val cameraOptions = mapViewportState.cameraForCoordinates(
                                                points,
                                                CameraOptions.Builder().build(),
                                                EdgeInsets(100.0, 100.0, 100.0, 100.0),
                                                null,
                                                null
                                            )
                                            mapViewportState.flyTo(cameraOptions)
                                        }
                                    }
                                }
                            }
                        }
                        override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                            onCalculatingRouteChange(false)
                            scope.launch { snackbarHostState.showSnackbar("Failed to find route") }
                        }
                        override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                            onCalculatingRouteChange(false)
                        }
                    }
                )
            }

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        requestMapboxRoute(Point.fromLngLat(lastLocation.longitude, lastLocation.latitude))
                    } else {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { currentLocation ->
                                if (currentLocation != null) {
                                    requestMapboxRoute(Point.fromLngLat(currentLocation.longitude, currentLocation.latitude))
                                } else {
                                    onCalculatingRouteChange(false)
                                    scope.launch { 
                                        snackbarHostState.showSnackbar("Location unavailable. Please set location in emulator.") 
                                    }
                                }
                            }
                            .addOnFailureListener {
                                onCalculatingRouteChange(false)
                                scope.launch { snackbarHostState.showSnackbar("Failed to get current location") }
                            }
                    }
                }.addOnFailureListener {
                    onCalculatingRouteChange(false)
                    scope.launch { snackbarHostState.showSnackbar("Location permission or service error") }
                }
            } catch (e: SecurityException) {
                onCalculatingRouteChange(false)
            }
        }
    }

    // Handle pending directions from details screen
    LaunchedEffect(pendingDirectionsCenterId, centers) {
        if (pendingDirectionsCenterId != null && centers.isNotEmpty()) {
            val center = centers.find { it.id == pendingDirectionsCenterId }
            if (center != null) {
                selectedCenter = center
                onDirectionsHandled()
                // Automatically trigger directions and start navigation
                requestRoute(center, true)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permission granted, fetch location
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        location?.let {
                            mapViewportState.setCameraOptions {
                                center(Point.fromLngLat(it.longitude, it.latitude))
                                zoom(13.0)
                            }
                        }
                    }
            } catch (e: SecurityException) {
                // Handle exception
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        mapViewportState.setCameraOptions {
                            center(Point.fromLngLat(it.longitude, it.latitude))
                            zoom(13.0)
                        }
                    }
                }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState) 
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    style = { MapStyle(mapStyle) }
                ) {
                    MapEffect(isNavigating) { mapView ->
                        mapView.location.updateSettings {
                            enabled = locationEnabled
                            locationPuck = LocationPuck2D()
                            pulsingEnabled = locationEnabled
                        }
                        
                        if (isNavigating) {
                            mapboxNavigation?.startTripSession()
                            mapView.viewport.transitionTo(
                                mapView.viewport.makeFollowPuckViewportState(
                                    FollowPuckViewportStateOptions.Builder()
                                        .pitch(45.0)
                                        .zoom(17.0)
                                        .build()
                                )
                            )
                        } else {
                            mapboxNavigation?.stopTripSession()
                            mapView.viewport.idle()
                            mapViewportState.setCameraOptions {
                                pitch(0.0)
                            }
                        }
                    }

                    navigationRoutes.firstOrNull()?.let { route ->
                        val routePoints = route.directionsRoute.geometry()?.let { geometry ->
                            LineString.fromPolyline(geometry, 6).coordinates()
                        } ?: emptyList()
                        
                        if (routePoints.isNotEmpty()) {
                            PolylineAnnotation(
                                points = routePoints,
                                polylineAnnotationState = remember(route.directionsRoute.geometry()) {
                                    PolylineAnnotationState().apply {
                                        lineColor = Color(0xFF007AFF)
                                        lineWidth = 5.0
                                    }
                                }
                            )
                        }
                    }

                    centers.forEach { center ->
                        key(center.id) {
                            val categoryColor = remember(center.category.color) {
                                when (center.category.color) {
                                    "systemPink" -> Color(0xFFFF2D55)
                                    "systemRed" -> Color(0xFFFF3B30)
                                    "systemBlue" -> Color(0xFF007AFF)
                                    "systemGreen" -> Color(0xFF34C759)
                                    "systemOrange" -> Color(0xFFFF9500)
                                    "systemYellow" -> Color(0xFFFFCC00)
                                    else -> try {
                                        Color(android.graphics.Color.parseColor(center.category.color))
                                    } catch (e: Exception) {
                                        Color.Gray
                                    }
                                }
                            }
                            val iconVector = getCategoryIcon(center.category.icon?.displayName ?: "")
                            val painter = rememberVectorPainter(iconVector)
                            val iconImage = rememberIconImage(
                                key = (center.category.icon?.displayName ?: "") + center.category.color,
                                painter = object : Painter() {
                                    override val intrinsicSize: Size = painter.intrinsicSize
                                    override fun DrawScope.onDraw() {
                                        with(painter) {
                                            draw(size, colorFilter = ColorFilter.tint(categoryColor))
                                        }
                                    }
                                }
                            )

                            PointAnnotation(
                                point = Point.fromLngLat(center.coordinates.longitude, center.coordinates.latitude),
                                pointAnnotationState = remember(center.id) {
                                    PointAnnotationState()
                                }.apply {
                                    this.iconImage = iconImage
                                    this.iconSize = 1.2
                                    this.textField = center.name
                                    this.textSize = 12.0
                                    this.textOffset = listOf(0.0, 1.5)
                                    this.textAnchor = com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor.TOP
                                    this.textColor = if (isDarkMode) Color.White else Color.Black
                                    this.textHaloColor = if (isDarkMode) Color.Black else Color.White
                                    this.textHaloWidth = 1.0
                                },
                                onClick = {
                                    selectedCenter = center
                                    true
                                }
                            )
                        }
                    }
                }

                if (isLoading || isCalculatingRoute) {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        type = LoadingIndicatorType.SHAPES
                    )
                }

                if (isNavigating) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Navigating to ${selectedCenter?.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Follow the blue line on the map",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Button(
                                    onClick = { isNavigating = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Text("Stop")
                                }
                            }
                        }
                    }
                }

            this@Row.AnimatedVisibility(
                visible = selectedCenter != null && !isExpanded && !isNavigating,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                    selectedCenter?.let { center ->
                        MapFloatingCard(
                            center = center,
                            onClose = { 
                                selectedCenter = null
                                navigationRoutes = emptyList()
                                mapboxNavigation?.setNavigationRoutes(emptyList())
                            },
                            onDetailsClick = { 
                                selectedCenter = null
                                onCenterClick(center.name) 
                            },
                            onDirectionsClick = { requestRoute(center, false) },
                            isCalculatingRoute = isCalculatingRoute,
                            hasRoute = navigationRoutes.isNotEmpty(),
                            onStartNavigation = { isNavigating = true }
                        )
                    }
                }
            }

            this@Row.AnimatedVisibility(
                visible = selectedCenter != null && isExpanded,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                Box(modifier = Modifier.width(400.dp).fillMaxHeight()) {
                    CenterDetailsContent(
                        center = selectedCenter,
                        isLoading = false,
                        onBackClick = { selectedCenter = null },
                        isExpanded = true,
                        onDirectionsClick = { center -> requestRoute(center, true) },
                        isDirectionsLoading = isCalculatingRoute
                    )
                }
            }
        }
    }
}

@Composable
fun MapFloatingCard(
    center: CenterModel,
    onClose: () -> Unit,
    onDetailsClick: () -> Unit,
    onDirectionsClick: () -> Unit,
    isCalculatingRoute: Boolean,
    hasRoute: Boolean = false,
    onStartNavigation: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = center.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = center.category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            center.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (hasRoute && !isCalculatingRoute) {
                    Button(
                        onClick = onStartNavigation,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start", fontSize = 14.sp)
                    }
                } else {
                    Button(
                        onClick = onDirectionsClick,
                        enabled = !isCalculatingRoute,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isCalculatingRoute) {
                            LoadingIndicator(
                                modifier = Modifier.size(24.dp),
                                type = LoadingIndicatorType.WAVY
                            )
                        } else {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Directions", fontSize = 14.sp)
                    }
                }

                OutlinedButton(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Details", fontSize = 14.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                }
            }
        }
    }
}

private fun getCategoryIcon(displayName: String): ImageVector {
    return when (displayName.lowercase()) {
        "building2" -> Icons.Default.Business
        "hospital" -> Icons.Default.LocalHospital
        "sofa" -> Icons.Default.Chair
        "health", "medical" -> Icons.Default.MedicalServices
        "education", "school" -> Icons.Default.School
        "social", "support" -> Icons.Default.Groups
        "legal" -> Icons.Default.Gavel
        "emergency" -> Icons.Default.Warning
        "mental", "psychology" -> Icons.Default.Psychology
        "community" -> Icons.Default.LocationCity
        else -> Icons.Default.Place
    }
}
