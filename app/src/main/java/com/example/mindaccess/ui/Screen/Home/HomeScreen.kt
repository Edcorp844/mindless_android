package com.example.mindaccess.ui.Screen.Home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindaccess.ui.Components.LoadingIndicator
import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationState
import com.mapbox.maps.extension.compose.style.MapStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val centers by viewModel.centers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(32.5825, 0.3476)) // Default to Kampala
                    zoom(11.0)
                    pitch(0.0)
                    bearing(0.0)
                }
            }

            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = { MapStyle(Style.MAPBOX_STREETS) }
            ) {
                centers.forEach { center ->
                    PointAnnotation(
                        point = Point.fromLngLat(center.location.longitude, center.location.latitude),
                        pointAnnotationState = remember { PointAnnotationState() }
                    )
                }
            }

            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
