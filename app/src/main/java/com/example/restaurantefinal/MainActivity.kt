package com.example.restaurantefinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.restaurantefinal.ui.theme.RestauranteFinalTheme
import com.example.restaurantefinal.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestauranteFinalTheme {
                HomeScreen()
            }
        }
    }
}
