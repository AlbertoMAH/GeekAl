package com.example.restaurantefinal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.restaurantefinal.ui.screens.HomeScreen
import com.example.restaurantefinal.ui.screens.ReservationsScreen
import com.example.restaurantefinal.ui.screens.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = NavBarScreen.Home.route,
        modifier = modifier
    ) {
        composable(NavBarScreen.Home.route) {
            HomeScreen()
        }
        composable(NavBarScreen.Reservations.route) {
            ReservationsScreen()
        }
        composable(NavBarScreen.Settings.route) {
            SettingsScreen()
        }
    }
}
