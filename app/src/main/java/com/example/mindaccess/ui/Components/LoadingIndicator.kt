package com.example.mindaccess.ui.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class LoadingIndicatorType {
    SHAPES,
    WAVY
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    type: LoadingIndicatorType = LoadingIndicatorType.SHAPES
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            LoadingIndicatorType.SHAPES -> {
                androidx.compose.material3.LoadingIndicator()
            }
            LoadingIndicatorType.WAVY -> {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
