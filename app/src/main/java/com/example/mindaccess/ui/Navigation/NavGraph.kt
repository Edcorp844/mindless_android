package com.example.mindaccess.ui.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mindaccess.ui.Screen.CenterDetails.CenterDetailsScreen
import com.example.mindaccess.ui.Screen.Main.MainScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onCenterClick = { centerName ->
                    navController.navigate("centerDetails/$centerName")
                },
                onSearchClick = {
                    // Handle global search click
                }
            )
        }
        composable(
            route = "centerDetails/{centerName}",
            arguments = listOf(navArgument("centerName") { type = androidx.navigation.NavType.StringType })
        ) {
            CenterDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onDirectionsClick = { center ->
                    // Navigate back to main screen and let it handle directions
                    // In a more robust app, you'd use a shared ViewModel or a specific nav result
                    navController.popBackStack()
                }
            )
        }
    }
}
