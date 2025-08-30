package com.example.restaurantefinal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.restaurantefinal.ui.screens.HomeScreen
import com.example.restaurantefinal.ui.screens.ProfileScreen
import com.example.restaurantefinal.ui.screens.ReservationsScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onShowMessage: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavBarScreen.Home.route,
        modifier = modifier
    ) {
        composable(NavBarScreen.Home.route) {
            HomeScreen(onShowMessage = onShowMessage)
        }
        composable(NavBarScreen.Reservations.route) {
            ReservationsScreen()
        }
        composable(NavBarScreen.Profile.route) {
            ProfileScreen()
        }
    }
}
