package com.example.mindaccess

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.mindaccess.ui.Navigation.NavGraph
import com.example.mindaccess.ui.theme.ContrastAwareMindAcessTheme
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxNavigationApp.attach(this)
        enableEdgeToEdge()

        askNotificationPermission()

        setContent {
            ContrastAwareMindAcessTheme(dynamicColor = true) {
                val navController = rememberNavController()
                var navigateToNotifications by remember { mutableStateOf(false) }

                LaunchedEffect(intent) {
                    if (intent?.getStringExtra("navigate_to") == "notifications") {
                        navigateToNotifications = true
                        intent.removeExtra("navigate_to")
                    }
                }

                NavGraph(
                    navController = navController,
                    startWithNotifications = navigateToNotifications,
                    onNotificationsHandled = { navigateToNotifications = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining why the features requires this permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}


@Preview
@Composable
fun DefaultPreview() {
    ContrastAwareMindAcessTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}