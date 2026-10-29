package com.app.exoplanethunter.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.exoplanethunter.R
import com.app.exoplanethunter.presentation.components.SafeAreaInsets
import com.app.exoplanethunter.presentation.screens.about.AboutScreen
import com.app.exoplanethunter.presentation.screens.about.AboutPageScreen
import com.app.exoplanethunter.presentation.screens.favorites.FavoritesScreen
import com.app.exoplanethunter.presentation.screens.galaxymap.GalaxyMapScreen
import com.app.exoplanethunter.presentation.screens.planetdetail.PlanetDetailScreen
import com.app.exoplanethunter.presentation.screens.planetlist.PlanetListScreen
import com.app.exoplanethunter.presentation.screens.planetlist.PlanetListViewModel
import org.koin.androidx.compose.koinViewModel
import com.app.exoplanethunter.presentation.screens.splash.SplashScreen
import com.app.exoplanethunter.presentation.screens.search.SearchScreen
import com.app.exoplanethunter.presentation.screens.compare.CompareScreen
import com.app.exoplanethunter.presentation.screens.spacefact.SpaceFactDetailScreen
import com.app.exoplanethunter.presentation.screens.spacefact.SpaceFactLibraryScreen
import com.app.exoplanethunter.presentation.screens.statistics.StatisticsScreen
import com.app.exoplanethunter.presentation.screens.starsystem.StarSystemDetailScreen
import com.app.exoplanethunter.presentation.screens.starsystem.StarSystemListScreen
import com.app.exoplanethunter.presentation.components.AppIcons
import com.app.exoplanethunter.presentation.theme.BodyFamily
import com.app.exoplanethunter.presentation.theme.InkText
import com.app.exoplanethunter.presentation.theme.InkTextFaint
import com.app.exoplanethunter.presentation.theme.MatchaDeep
import com.app.exoplanethunter.presentation.theme.OnMatchaDeep
import com.app.exoplanethunter.presentation.theme.SpaceBlack
import com.app.exoplanethunter.presentation.theme.Well

// ---------------------------------------------------------------------------
// Screen routes
// ---------------------------------------------------------------------------

sealed class Screen(val route: String) {
    data object Splash : Screen(NavRoutes.SPLASH)
    data object Main : Screen(NavRoutes.MAIN)
    data object About : Screen(NavRoutes.ABOUT)
    data object PlanetDetail : Screen(NavRoutes.PLANET_DETAIL) {
        fun createRoute(planetId: Long) = "planet_detail/$planetId"
    }
    data object StarSystemDetail : Screen(NavRoutes.STAR_SYSTEM_DETAIL) {
        fun createRoute(systemId: Long) = "star_system_detail/$systemId"
    }
    data object Compare : Screen(NavRoutes.COMPARE) {
        fun createRoute(planetAId: Long, planetBId: Long) = "compare/$planetAId/$planetBId"
    }
    data object SpaceFact : Screen(NavRoutes.SPACE_FACT) {
        fun createRoute(factId: Int) = "space_fact/$factId"
    }
    data object SpaceFactLibrary : Screen(NavRoutes.SPACE_FACT_LIBRARY)
    data object GalaxyMap : Screen(NavRoutes.GALAXY_MAP)
    data object Search : Screen("search")
}

// ---------------------------------------------------------------------------
// Bottom navigation tabs
// ---------------------------------------------------------------------------

enum class BottomNavTab(@StringRes val labelRes: Int, val icon: ImageVector) {
    Planets(R.string.nav_planets, AppIcons.Planet),
    StarSystems(R.string.nav_stars, AppIcons.Sun),
    Favorites(R.string.nav_favorites, AppIcons.Star),
    Statistics(R.string.nav_stats, AppIcons.Bars),
    About(R.string.nav_about, AppIcons.Sliders)
}

// ---------------------------------------------------------------------------
// Root navigation graph
// ---------------------------------------------------------------------------

@Composable
fun ExoplanetNavigation(
    initialFactId: Int? = null,
    onFactConsumed: () -> Unit = {},
    initialPlanetId: Long? = null,
    onPlanetConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Handle a tap on the space-fact notification: jump to Main, then push the fact screen.
    LaunchedEffect(initialFactId) {
        val factId = initialFactId ?: return@LaunchedEffect
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
        navController.navigate(Screen.SpaceFact.createRoute(factId))
        onFactConsumed()
    }

    // Handle a tap on the Planet-of-the-Day widget: jump to Main, then push planet detail.
    LaunchedEffect(initialPlanetId) {
        val planetId = initialPlanetId ?: return@LaunchedEffect
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
        navController.navigate(Screen.PlanetDetail.createRoute(planetId))
        onPlanetConsumed()
    }

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
        composable(
            Screen.Splash.route,
            // Cross-fade out with a slight scale-up into the Planets list.
            exitTransition = { fadeOut(tween(400)) + scaleOut(tween(400), targetScale = 1.06f) }
        ) {
            SplashScreen(
                onDataLoaded = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Screen.Main.route,
            enterTransition = {
                if (initialState.destination.route == Screen.Splash.route) fadeIn(tween(400))
                else slideInHorizontally(tween(400)) { it } + fadeIn(tween(400))
            }
        ) {
            MainScreen(
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onSystemClick = { systemId ->
                    navController.navigate(Screen.StarSystemDetail.createRoute(systemId))
                },
                onCompare = { planetAId, planetBId ->
                    navController.navigate(Screen.Compare.createRoute(planetAId, planetBId))
                },
                onOpenGalaxyMap = {
                    navController.navigate(Screen.GalaxyMap.route)
                },
                onOpenFactLibrary = {
                    navController.navigate(Screen.SpaceFactLibrary.route)
                },
                onOpenSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onOpenAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.About.route) {
            AboutPageScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onSystemClick = { systemId ->
                    navController.navigate(Screen.StarSystemDetail.createRoute(systemId))
                }
            )
        }

        composable(Screen.SpaceFactLibrary.route) {
            SpaceFactLibraryScreen(
                onFactClick = { factId ->
                    navController.navigate(Screen.SpaceFact.createRoute(factId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.GalaxyMap.route) {
            GalaxyMapScreen(
                onSystemClick = { systemId ->
                    navController.navigate(Screen.StarSystemDetail.createRoute(systemId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PlanetDetail.route,
            arguments = listOf(navArgument(NavArgs.PLANET_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val planetId = backStackEntry.arguments?.getLong(NavArgs.PLANET_ID) ?: return@composable
            PlanetDetailScreen(
                planetId = planetId,
                onBack = { navController.popBackStack() },
                onOpenSystem = { systemId ->
                    navController.navigate(Screen.StarSystemDetail.createRoute(systemId))
                }
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

        composable(
            route = Screen.Compare.route,
            arguments = listOf(
                navArgument(NavArgs.COMPARE_A) { type = NavType.LongType },
                navArgument(NavArgs.COMPARE_B) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val planetAId = backStackEntry.arguments?.getLong(NavArgs.COMPARE_A) ?: return@composable
            val planetBId = backStackEntry.arguments?.getLong(NavArgs.COMPARE_B) ?: return@composable
            CompareScreen(
                planetAId = planetAId,
                planetBId = planetBId,
                onPlanetClick = { planetId ->
                    navController.navigate(Screen.PlanetDetail.createRoute(planetId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SpaceFact.route,
            arguments = listOf(navArgument(NavArgs.FACT_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val factId = backStackEntry.arguments?.getInt(NavArgs.FACT_ID) ?: return@composable
            SpaceFactDetailScreen(
                factId = factId,
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
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
    onSystemClick: (Long) -> Unit,
    onCompare: (Long, Long) -> Unit,
    onOpenGalaxyMap: () -> Unit,
    onOpenFactLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAbout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.Planets.name) }
    // Keeps each tab's saved UI state (scroll position, search text) while it's off screen,
    // so switching tabs and back returns you to where you were.
    val tabStateHolder = rememberSaveableStateHolder()
    // Shared with the Planets tab (same back-stack entry), so Stats can filter it.
    val planetListViewModel: PlanetListViewModel = koinViewModel()

    Scaffold(
        containerColor = SpaceBlack,
        // Status bar, navigation bar and any display cutout: the tab screens draw their own
        // headers, so the Scaffold pads them out of the system bars for all of them at once.
        contentWindowInsets = SafeAreaInsets,
        bottomBar = {
            NavigationBar(
                containerColor = Well,
                contentColor = InkText,
                tonalElevation = 0.dp,
                windowInsets = SafeAreaInsets.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            ) {
                BottomNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab.name
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.name },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(tab.labelRes),
                                fontFamily = BodyFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnMatchaDeep,
                            selectedTextColor = InkText,
                            unselectedIconColor = InkTextFaint,
                            unselectedTextColor = InkTextFaint,
                            indicatorColor = MatchaDeep
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            tabStateHolder.SaveableStateProvider(selectedTab) {
                when (BottomNavTab.valueOf(selectedTab)) {
                    BottomNavTab.Planets -> PlanetListScreen(
                        onPlanetClick = onPlanetClick,
                        onCompare = onCompare,
                        onOpenSearch = onOpenSearch
                    )
                    BottomNavTab.StarSystems -> StarSystemListScreen(
                        onSystemClick = onSystemClick,
                        onOpenGalaxyMap = onOpenGalaxyMap,
                        onOpenSearch = onOpenSearch
                    )
                    BottomNavTab.Favorites -> FavoritesScreen(
                        onPlanetClick = onPlanetClick,
                        onBrowsePlanets = { selectedTab = BottomNavTab.Planets.name }
                    )
                    BottomNavTab.Statistics -> StatisticsScreen(
                        onMethodSelected = { method ->
                            planetListViewModel.onFilterSelected(method)
                            selectedTab = BottomNavTab.Planets.name
                        }
                    )
                    BottomNavTab.About -> AboutScreen(
                        onOpenFactLibrary = onOpenFactLibrary,
                        onOpenAbout = onOpenAbout
                    )
                }
            }
        }
    }
}
