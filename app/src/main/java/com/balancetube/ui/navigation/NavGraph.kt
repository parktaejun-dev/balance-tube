package com.balancetube.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.balancetube.ui.screen.home.HomeScreen
import com.balancetube.ui.screen.login.LoginScreen
import com.balancetube.ui.screen.recommendations.RecommendationScreen
import com.balancetube.ui.screen.settings.SettingsScreen
import com.balancetube.util.AuthManager

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Recommendations : Screen("recommendations/{category}") {
        fun createRoute(category: String) = "recommendations/$category"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authManager: AuthManager,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authManager = authManager
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRecommendations = { category ->
                    navController.navigate(Screen.Recommendations.createRoute(category))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Recommendations.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            RecommendationScreen(
                categoryName = category,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
