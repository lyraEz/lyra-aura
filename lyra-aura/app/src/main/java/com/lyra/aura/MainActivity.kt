package com.lyra.aura

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lyra.aura.model.AppSettings
import com.lyra.aura.model.AppTheme
import com.lyra.aura.ui.navigation.LyraNavHost
import com.lyra.aura.ui.navigation.Screen
import com.lyra.aura.ui.theme.LavenderBg
import com.lyra.aura.ui.theme.LyraAuraTheme
import com.lyra.aura.viewmodel.MainViewModel
import com.lyra.aura.viewmodel.PresenceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val presenceViewModel: PresenceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            val settings by mainViewModel.settings.collectAsState(AppSettings())
            val appTheme = runCatching { AppTheme.valueOf(settings.theme) }.getOrDefault(AppTheme.LAVENDER_DARK)

            // Keep screen on if setting enabled and connected
            val connectionState by mainViewModel.connectionState.collectAsState()
            LaunchedEffect(settings.keepScreenOnWhileConnected, connectionState) {
                if (settings.keepScreenOnWhileConnected &&
                    connectionState is com.lyra.aura.model.ConnectionState.Connected) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LyraAuraTheme(appTheme = appTheme) {
                LyraAuraApp(mainViewModel = mainViewModel, presenceViewModel = presenceViewModel)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            } else {
                mainViewModel.setNotificationPermission(true)
            }
        } else {
            mainViewModel.setNotificationPermission(true)
        }
    }
}

@Composable
private fun LyraAuraApp(
    mainViewModel: MainViewModel,
    presenceViewModel: PresenceViewModel,
) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val tosAccepted by mainViewModel.tosAccepted.collectAsState()
    var showTosDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tosAccepted) {
        if (!tosAccepted) showTosDialog = true
    }

    val bottomNavRoutes = listOf(Screen.Home.route, Screen.Configure.route, Screen.Presets.route, Screen.Settings.route)
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LavenderBg,
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                LyraBottomBar(
                    currentRoute   = currentRoute,
                    onNavigate     = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LyraNavHost(
                navController     = navController,
                mainViewModel     = mainViewModel,
                presenceViewModel = presenceViewModel,
            )
        }
    }

    // ToS dialog
    if (showTosDialog) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Important Warning", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "This app uses your Discord user token to set a Rich Presence.\n\n" +
                    "• Using user tokens for automation may violate Discord's Terms of Service\n" +
                    "• Your account could be suspended by Discord\n" +
                    "• This is an educational fork for learning purposes only\n\n" +
                    "Proceed at your own risk.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = { mainViewModel.acceptTos(); showTosDialog = false },
                    shape = RoundedCornerShape(14.dp),
                ) { Text("I Understand, Continue") }
            },
        )
    }
}

// ── Bottom Navigation ──────────────────────────────────────────────────────

private data class BottomNavDef(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavDef(Screen.Home,      "Home",     Icons.Filled.Home,         Icons.Outlined.Home),
    BottomNavDef(Screen.Configure, "Presence", Icons.Filled.Tune,         Icons.Outlined.Tune),
    BottomNavDef(Screen.Presets,   "Presets",  Icons.Filled.Bookmark,     Icons.Outlined.BookmarkBorder),
    BottomNavDef(Screen.Settings,  "Settings", Icons.Filled.Settings,     Icons.Outlined.Settings),
)

@Composable
private fun LyraBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    )
                )
            )
            .navigationBarsPadding(),
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                ),
            )
        }
    }
}
