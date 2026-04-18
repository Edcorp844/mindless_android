package com.example.mindaccess.ui.Navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mindaccess.ui.Screen.CenterDetails.CenterDetailsScreen
import com.example.mindaccess.ui.Screen.Main.MainScreen

@Composable
fun NavGraph(navController: NavHostController) {
    var isCalculatingRoute by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") { backStackEntry ->
            val directionsCenterId = backStackEntry.savedStateHandle.getStateFlow<Int?>("directions_center_id", null).collectAsState()
            
            MainScreen(
                onCenterClick = { centerName ->
                    // Clear any pending directions when navigating to details
                    backStackEntry.savedStateHandle["directions_center_id"] = null
                    navController.navigate("centerDetails/$centerName")
                },
                onSearchClick = {
                    // Handle global search click
                },
                directionsCenterId = directionsCenterId.value,
                onDirectionsHandled = {
                    backStackEntry.savedStateHandle["directions_center_id"] = null
                },
                isCalculatingRoute = isCalculatingRoute,
                onCalculatingRouteChange = { isCalculatingRoute = it }
            )
        }
        composable(
            route = "centerDetails/{centerName}",
            arguments = listOf(navArgument("centerName") { type = androidx.navigation.NavType.StringType })
        ) {
            CenterDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onDirectionsClick = { center ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("directions_center_id", center.id)
                    navController.popBackStack()
                },
                isDirectionsLoading = isCalculatingRoute
            )
        }
    }
}
