package com.example.mindaccess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mindaccess.ui.Navigation.NavGraph
import com.example.mindaccess.ui.theme.ContrastAwareMindAcessTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContrastAwareMindAcessTheme(dynamicColor = true) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
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