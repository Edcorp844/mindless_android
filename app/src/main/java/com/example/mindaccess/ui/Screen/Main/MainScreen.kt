package com.example.mindaccess.ui.Screen.Main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mindaccess.ui.Screen.Centers.CentersScreen
import com.example.mindaccess.ui.Screen.Home.HomeScreen
import com.example.mindaccess.ui.Screen.Settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Centers : Screen("centers_list", "Centers", Icons.Outlined.Apartment)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    onCenterClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    directionsCenterId: Int? = null,
    onDirectionsHandled: () -> Unit = {},
    isCalculatingRoute: Boolean = false,
    onCalculatingRouteChange: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Centers,
        Screen.Settings
    )

    // Detect screen size for adaptive navigation
    val activity = LocalContext.current as android.app.Activity
    val windowSizeClass = calculateWindowSizeClass(activity)
    val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(modifier = Modifier.fillMaxSize()) {
        // Tablet Side Bar (Navigation Rail)
        if (useNavigationRail) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxHeight(),
                windowInsets = WindowInsets.safeDrawing
            ) {
                Spacer(Modifier.weight(1f))
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationRailItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        alwaysShowLabel = true
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }

        Scaffold(
            bottomBar = {
                // Mobile Bottom Bar
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                    ) {
                        items.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                alwaysShowLabel = true
                            )
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) { 
                        HomeScreen(
                            onCenterClick = onCenterClick,
                            pendingDirectionsCenterId = directionsCenterId,
                            onDirectionsHandled = onDirectionsHandled,
                            isCalculatingRoute = isCalculatingRoute,
                            onCalculatingRouteChange = onCalculatingRouteChange
                        )
                    }
                    composable(Screen.Centers.route) {
                        CentersScreen(
                            onCenterClick = { centerName ->
                                if (useNavigationRail) {
                                    // In tablet mode, we might handle this differently, 
                                    // but for now let's see if we can use the split view inside CentersScreen
                                }
                                onCenterClick(centerName)
                            },
                            onSearchClick = onSearchClick,
                            isExpanded = useNavigationRail
                        )
                    }
                    composable(Screen.Settings.route) { 
                        SettingsScreen(isExpanded = useNavigationRail) 
                    }
                }
            }
        }
    }
}
