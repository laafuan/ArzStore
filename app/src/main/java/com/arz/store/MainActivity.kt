package com.arz.store

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arz.store.model.GameProduct
import com.arz.store.ui.MainViewModel
import com.arz.store.ui.screen.HomeScreen
import com.arz.store.ui.screen.ProfileScreen
import com.arz.store.ui.screen.TopUpScreen
import com.arz.store.ui.screen.LoginScreen
import com.arz.store.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.arz.store.repository.ArzRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            ArzStoreTheme {
                ArzStoreApp()
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    data class TopUp(val game: GameProduct) : Screen()
    object Login : Screen()
    object Register : Screen()
}

enum class BottomNavDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("Beranda", Icons.Filled.Home, Icons.Outlined.Home),
    HISTORY("Riwayat", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    PROFILE("Profil", Icons.Filled.Person, Icons.Outlined.Person),
}

@Composable
fun ArzStoreApp() {
    val viewModel: MainViewModel = viewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var currentNavDestination by remember { mutableStateOf(BottomNavDestination.HOME) }
    var currentScreen by remember { mutableStateOf<Screen>(if (isLoggedIn) Screen.Home else Screen.Login) }

    // Reactively update screen if auth state changes
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentScreen !is Screen.Register) {
            currentScreen = Screen.Login
        } else if (isLoggedIn && (currentScreen is Screen.Login || currentScreen is Screen.Register)) {
            currentScreen = Screen.Home
        }
    }

    // Hide bottom nav on specific screens
    val showBottomNav = isLoggedIn && currentScreen is Screen.Home

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        containerColor = DarkBg,
        bottomBar = {
            if (showBottomNav) {
                ArzBottomNavBar(
                    currentDestination = currentNavDestination,
                    onDestinationChange = { currentNavDestination = it },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp,
                )
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState is Screen.TopUp || targetState is Screen.Register || (targetState is Screen.Home && initialState is Screen.Login)) {
                        slideInHorizontally(
                            animationSpec = tween(350),
                            initialOffsetX = { it }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(350),
                            targetOffsetX = { -it / 4 }
                        )
                    } else {
                        slideInHorizontally(
                            animationSpec = tween(350),
                            initialOffsetX = { -it / 4 }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = tween(350),
                            targetOffsetX = { it }
                        )
                    }
                },
                label = "screen_transition",
            ) { screen ->
                when (screen) {
                    is Screen.Login -> LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { currentScreen = Screen.Register }
                    )
                    is Screen.Register -> com.arz.store.ui.screen.RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { currentScreen = Screen.Login }
                    )
                    is Screen.Home -> {
                        when (currentNavDestination) {
                            BottomNavDestination.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onGameClick = { game ->
                                    currentScreen = Screen.TopUp(game)
                                }
                            )
                            BottomNavDestination.HISTORY -> com.arz.store.ui.screen.HistoryScreen(viewModel)
                            BottomNavDestination.PROFILE -> ProfileScreen(viewModel = viewModel)
                        }
                    }
                    is Screen.TopUp -> {
                        TopUpScreen(
                            game = screen.game,
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Home },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArzBottomNavBar(
    currentDestination: BottomNavDestination,
    onDestinationChange: (BottomNavDestination) -> Unit,
) {
    NavigationBar(
        containerColor = Color(0xFF0D1526),
        tonalElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
    ) {
        BottomNavDestination.entries.forEach { destination ->
            val isSelected = destination == currentDestination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationChange(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label,
                    )
                },
                label = {
                    Text(destination.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentCyan,
                    selectedTextColor = AccentCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = AccentCyan.copy(alpha = 0.12f),
                )
            )
        }
    }
}