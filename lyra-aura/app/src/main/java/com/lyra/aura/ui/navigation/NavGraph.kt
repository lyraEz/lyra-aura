package com.lyra.aura.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lyra.aura.ui.screen.*
import com.lyra.aura.viewmodel.MainViewModel
import com.lyra.aura.viewmodel.PresenceViewModel

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Configure: Screen("configure")
    object Presets  : Screen("presets")
    object Settings : Screen("settings")
    object Login    : Screen("login")
    object History  : Screen("history")
    object Log      : Screen("log")
    object About    : Screen("about")
}

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,      "Home",     "home"),
    BottomNavItem(Screen.Configure, "Presence", "presence"),
    BottomNavItem(Screen.Presets,   "Presets",  "presets"),
    BottomNavItem(Screen.Settings,  "Settings", "settings"),
)

data class BottomNavItem(val screen: Screen, val label: String, val icon: String)

@Composable
fun LyraNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    presenceViewModel: PresenceViewModel,
) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Home.route,
        enterTransition  = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
        exitTransition   = { fadeOut(tween(180)) },
        popEnterTransition  = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 8 } },
        popExitTransition   = { fadeOut(tween(180)) },
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                mainViewModel    = mainViewModel,
                presenceViewModel = presenceViewModel,
                onNavigateLogin  = { navController.navigate(Screen.Login.route) },
                onNavigateConfigure = { navController.navigate(Screen.Configure.route) },
            )
        }
        composable(Screen.Configure.route) {
            ConfigureScreen(
                mainViewModel    = mainViewModel,
                presenceViewModel = presenceViewModel,
            )
        }
        composable(Screen.Presets.route) {
            PresetsScreen(
                mainViewModel    = mainViewModel,
                presenceViewModel = presenceViewModel,
                onNavigateConfigure = { navController.navigate(Screen.Configure.route) },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                mainViewModel = mainViewModel,
                onNavigateLogin = { navController.navigate(Screen.Login.route) },
                onNavigateAbout = { navController.navigate(Screen.About.route) },
                onNavigateLog   = { navController.navigate(Screen.Log.route) },
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                mainViewModel = mainViewModel,
                onBack        = { navController.popBackStack() },
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                mainViewModel    = mainViewModel,
                presenceViewModel = presenceViewModel,
                onBack           = { navController.popBackStack() },
            )
        }
        composable(Screen.Log.route) {
            LogScreen(
                mainViewModel = mainViewModel,
                onBack        = { navController.popBackStack() },
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
