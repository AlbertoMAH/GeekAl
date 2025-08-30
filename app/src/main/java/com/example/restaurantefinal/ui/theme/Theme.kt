package com.example.restaurantefinal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    secondary = Indigo700,
    background = Gray100,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = Gray900,
    onSurface = Gray800,
)

@Composable
fun RestauranteFinalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Sticking with light theme as per mockup

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}