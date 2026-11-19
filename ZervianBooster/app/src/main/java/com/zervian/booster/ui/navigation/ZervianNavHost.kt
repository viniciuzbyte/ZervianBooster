package com.zervian.booster.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.State
import com.zervian.booster.ui.screens.dashboard.DashboardScreen
import com.zervian.booster.ui.screens.games.AddGameScreen
import com.zervian.booster.ui.screens.games.GameListScreen
import com.zervian.booster.ui.screens.splash.SplashScreen

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_GAMES = "games"
private const val ROUTE_ADD_GAME = "add_game"

@Composable
fun ZervianNavHost() {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry.value?.destination?.route != ROUTE_SPLASH &&
        backStackEntry.value?.destination?.route != ROUTE_ADD_GAME

    Scaffold(
        bottomBar = {
            if (showBottomBar) ZervianBottomBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_SPLASH,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(ROUTE_SPLASH) {
                SplashScreen(onFinished = {
                    navController.navigate(ROUTE_DASHBOARD) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                })
            }
            composable(ROUTE_DASHBOARD) { DashboardScreen() }
            composable(ROUTE_GAMES) {
                GameListScreen(onAddGame = { navController.navigate(ROUTE_ADD_GAME) })
            }
            composable(ROUTE_ADD_GAME) {
                AddGameScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun ZervianBottomBar(navController: androidx.navigation.NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.hierarchy?.firstOrNull()?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == ROUTE_DASHBOARD,
            onClick = { navController.navigateSingleTop(ROUTE_DASHBOARD) },
            icon = { Icon(Icons.Filled.Speed, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = currentRoute == ROUTE_GAMES,
            onClick = { navController.navigateSingleTop(ROUTE_GAMES) },
            icon = { Icon(Icons.Filled.SportsEsports, contentDescription = "Games") },
            label = { Text("Games") }
        )
    }
}

private fun androidx.navigation.NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
