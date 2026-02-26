package com.workspace.exoplanethunter.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workspace.exoplanethunter.presentation.screens.planetdetail.PlanetDetailScreen
import com.workspace.exoplanethunter.presentation.screens.planetlist.PlanetListScreen
import com.workspace.exoplanethunter.presentation.screens.splash.SplashScreen
import com.workspace.exoplanethunter.presentation.screens.starsystem.StarSystemDetailScreen
import com.workspace.exoplanethunter.presentation.screens.starsystem.StarSystemListScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object PlanetList : Screen("planet_list")
    data object PlanetDetail : Screen("planet_detail/{planetId}") {
        fun createRoute(planetId: Long) = "planet_detail/$planetId"
    }
    data object StarSystemList : Screen("star_system_list")
    data object StarSystemDetail : Screen("star_system_detail/{hostName}") {
        fun createRoute(hostName: String) =
            "star_system_detail/${java.net.URLEncoder.encode(hostName, "UTF-8")}"
    }
}

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
                    navController.navigate(Screen.PlanetList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PlanetList.route) {
            PlanetListScreen(
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onNavigateToStarSystems = {
                    navController.navigate(Screen.StarSystemList.route)
                }
            )
        }

        composable(
            route = Screen.PlanetDetail.route,
            arguments = listOf(navArgument("planetId") { type = NavType.LongType })
        ) { backStackEntry ->
            val planetId = backStackEntry.arguments?.getLong("planetId") ?: return@composable
            PlanetDetailScreen(
                planetId = planetId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.StarSystemList.route) {
            StarSystemListScreen(
                onSystemClick = { hostName ->
                    navController.navigate(Screen.StarSystemDetail.createRoute(hostName))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StarSystemDetail.route,
            arguments = listOf(navArgument("hostName") { type = NavType.StringType })
        ) { backStackEntry ->
            val hostName = backStackEntry.arguments?.getString("hostName") ?: return@composable
            val decodedHostName = java.net.URLDecoder.decode(hostName, "UTF-8")
            StarSystemDetailScreen(
                hostName = decodedHostName,
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
