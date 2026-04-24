package com.app.exoplanethunter.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.exoplanethunter.presentation.screens.planetdetail.PlanetDetailScreen
import com.app.exoplanethunter.presentation.screens.planetlist.PlanetListScreen
import com.app.exoplanethunter.presentation.screens.splash.SplashScreen
import com.app.exoplanethunter.presentation.screens.starsystem.StarSystemDetailScreen
import com.app.exoplanethunter.presentation.screens.starsystem.StarSystemListScreen
import com.app.exoplanethunter.presentation.theme.CosmicCyan
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.SurfaceCard
import com.app.exoplanethunter.presentation.theme.SurfaceDark
import com.app.exoplanethunter.presentation.theme.TextMuted

// ---------------------------------------------------------------------------
// Screen routes
// ---------------------------------------------------------------------------

sealed class Screen(val route: String) {
    data object Splash : Screen(NavRoutes.SPLASH)
    data object Main : Screen(NavRoutes.MAIN)
    data object PlanetDetail : Screen(NavRoutes.PLANET_DETAIL) {
        fun createRoute(planetId: Long) = "planet_detail/$planetId"
    }
    data object StarSystemDetail : Screen(NavRoutes.STAR_SYSTEM_DETAIL) {
        fun createRoute(systemId: Long) = "star_system_detail/$systemId"
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation tabs
// ---------------------------------------------------------------------------

enum class BottomNavTab(val label: String, val icon: ImageVector) {
    Planets("Planets", Icons.Default.Public),
    StarSystems("Star Systems", Icons.Default.Star)
}

// ---------------------------------------------------------------------------
// Root navigation graph
// ---------------------------------------------------------------------------

@Composable
fun ExoplanetNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideInHorizontally(tween(400)) { it } + fadeIn(tween(400))
        },
        exitTransition = {
            slideOutHorizontally(tween(400)) { -it / 3 } + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(tween(400)) { -it / 3 } + fadeIn(tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(tween(400)) { it } + fadeOut(tween(300))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onDataLoaded = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onSystemClick = { systemId ->
                    navController.navigate(Screen.StarSystemDetail.createRoute(systemId))
                }
            )
        }

        composable(
            route = Screen.PlanetDetail.route,
            arguments = listOf(navArgument(NavArgs.PLANET_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val planetId = backStackEntry.arguments?.getLong(NavArgs.PLANET_ID) ?: return@composable
            PlanetDetailScreen(
                planetId = planetId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StarSystemDetail.route,
            arguments = listOf(navArgument(NavArgs.SYSTEM_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val systemId = backStackEntry.arguments?.getLong(NavArgs.SYSTEM_ID) ?: return@composable
            StarSystemDetailScreen(
                systemId = systemId,
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Main screen with bottom navigation bar
// ---------------------------------------------------------------------------

@Composable
private fun MainScreen(
    onPlanetClick: (Long) -> Unit,
    onSystemClick: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.Planets.name) }

    Scaffold(
        containerColor = SpaceBlack,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                BottomNavTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.name,
                        onClick = { selectedTab = tab.name },
                        icon = {
                            Icon(tab.icon, contentDescription = tab.label)
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (selectedTab == tab.name)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicCyan,
                            selectedTextColor = CosmicCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = SurfaceCard
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (BottomNavTab.valueOf(selectedTab)) {
                BottomNavTab.Planets -> PlanetListScreen(
                    onPlanetClick = onPlanetClick
                )
                BottomNavTab.StarSystems -> StarSystemListScreen(
                    onSystemClick = onSystemClick
                )
            }
        }
    }
}
