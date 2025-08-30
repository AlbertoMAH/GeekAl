package com.example.restaurantefinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.restaurantefinal.ui.composables.MessageDialog
import com.example.restaurantefinal.ui.navigation.AppBottomNavBar
import com.example.restaurantefinal.ui.navigation.AppNavigation
import com.example.restaurantefinal.ui.theme.RestauranteFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestauranteFinalTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val onShowMessage: (String) -> Unit = { message ->
        dialogMessage = message
        showDialog = true
    }

    Box {
        Scaffold(
            bottomBar = { AppBottomNavBar(navController = navController) }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onShowMessage = onShowMessage
            )
        }

        if (showDialog) {
            MessageDialog(
                message = dialogMessage,
                onDismiss = { showDialog = false }
            )
        }
    }
}
