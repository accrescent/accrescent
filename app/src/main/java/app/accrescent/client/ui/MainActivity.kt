package app.accrescent.client.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.ROOT_DOMAIN
import app.accrescent.client.ui.theme.AccrescentTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appId = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                )
            }

            val viewModel: SettingsViewModel = hiltViewModel()
            val dynamicColor by viewModel.dynamicColor.collectAsState(false)

            AccrescentTheme(dynamicColor = dynamicColor) {
                MainContent(
                    systemUiController,
                    appId
                )
            }
        }

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && this.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    systemUiController: SystemUiController,
    appId: String?
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberAnimatedNavController()
    val screens = listOf(Screen.AppList, Screen.InstalledApps, Screen.AppUpdates)

    val showBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in screens.map { it.route }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceColorEl2 = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    SideEffect {
        systemUiController.setNavigationBarColor(
            if (showBottomBar) surfaceColorEl2 else surfaceColor
        )
    }

    val startDestination =
        if (appId != null) "${Screen.AppDetails.route}/{appId}" else Screen.AppList.route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // This little hack is used to ensure smooth transition animations when navigating
            // between AppListScreen and AppDetailsScreen. LookaheadLayout may provide a simpler
            // solution once Compose 1.3.0 becomes stable.
            AnimatedVisibility(
                visible = currentDestination?.route == "${Screen.AppDetails.route}/{appId}",
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                CenterAlignedTopAppBar(title = {})
            }
            AnimatedVisibility(
                visible = currentDestination?.route == Screen.Settings.route,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                CenterAlignedTopAppBar(title = { Text(stringResource(R.string.settings)) })
            }
            AnimatedVisibility(
                visible = currentDestination?.route != "${Screen.AppDetails.route}/{appId}"
                        && currentDestination?.route != Screen.Settings.route,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                CenterAlignedTopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(
                                imageVector = Screen.Settings.navIconSelected!!,
                                contentDescription = stringResource(Screen.Settings.resourceId)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(animationSpec = tween(400)) { it },
                exit = slideOutVertically(animationSpec = tween(400)) { it },
            ) {
                NavigationBar {
                    screens.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) screen.navIconSelected!! else screen.navIcon!!,
                                    contentDescription = stringResource(screen.resourceId)
                                )
                            },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }) { padding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Screen.AppList.route, enterTransition = {
                when (initialState.destination.route) {
                    Screen.InstalledApps.route,
                    Screen.AppUpdates.route ->
                        slideInHorizontally(animationSpec = tween(350)) { -it }
                    else -> null
                }
            }, exitTransition = {
                when (targetState.destination.route) {
                    "${Screen.AppDetails.route}/{appId}" ->
                        fadeOut(animationSpec = tween(350))
                    Screen.InstalledApps.route,
                    Screen.AppUpdates.route ->
                        slideOutHorizontally(animationSpec = tween(350)) { -it }
                    Screen.Settings.route ->
                        fadeOut(animationSpec = tween(350))
                    else -> null
                }
            }) {
                val model = hiltViewModel<AppListViewModel>()
                AppList(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    padding = padding,
                    viewModel = model,
                )
            }
            composable(Screen.InstalledApps.route, enterTransition = {
                when (initialState.destination.route) {
                    Screen.AppList.route ->
                        slideInHorizontally(animationSpec = tween(350)) { it }
                    Screen.AppUpdates.route ->
                        slideInHorizontally(animationSpec = tween(350)) { -it }
                    else -> null
                }
            }, exitTransition = {
                when (targetState.destination.route) {
                    "${Screen.AppDetails.route}/{appId}" ->
                        fadeOut(animationSpec = tween(350))
                    Screen.AppList.route ->
                        slideOutHorizontally(animationSpec = tween(350)) { it }
                    Screen.AppUpdates.route ->
                        slideOutHorizontally(animationSpec = tween(350)) { -it }
                    Screen.Settings.route ->
                        fadeOut(animationSpec = tween(350))
                    else -> null
                }
            }) {
                val model = hiltViewModel<AppListViewModel>()
                AppList(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    padding = padding,
                    viewModel = model,
                    filter = { it == InstallStatus.INSTALLED || it == InstallStatus.UPDATABLE },
                    noFilterResultsText = stringResource(R.string.no_apps_installed),
                )
            }
            composable(Screen.AppUpdates.route, enterTransition = {
                when (initialState.destination.route) {
                    Screen.InstalledApps.route,
                    Screen.AppList.route ->
                        slideInHorizontally(animationSpec = tween(350)) { it }
                    else -> null
                }
            }, exitTransition = {
                when (targetState.destination.route) {
                    "${Screen.AppDetails.route}/{appId}" ->
                        fadeOut(animationSpec = tween(350))
                    Screen.AppList.route,
                    Screen.InstalledApps.route ->
                        slideOutHorizontally(animationSpec = tween(350)) { it }
                    Screen.Settings.route ->
                        fadeOut(animationSpec = tween(350))
                    else -> null
                }
            }) {
                val model = hiltViewModel<AppListViewModel>()
                AppList(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    padding = padding,
                    viewModel = model,
                    filter = { it == InstallStatus.UPDATABLE },
                    noFilterResultsText = stringResource(R.string.up_to_date),
                )
            }
            composable(
                "${Screen.AppDetails.route}/{appId}", arguments = listOf(navArgument("appId") {
                    type = NavType.StringType
                    defaultValue = appId ?: ""
                }),
                deepLinks = listOf(navDeepLink {
                    uriPattern = "https://${ROOT_DOMAIN}/app/{appId}"
                }),
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.AppList.route,
                        Screen.InstalledApps.route,
                        Screen.AppUpdates.route ->
                            slideInVertically(animationSpec = tween(400)) { it } +
                                    fadeIn(animationSpec = tween(400))
                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.AppList.route,
                        Screen.InstalledApps.route,
                        Screen.AppUpdates.route ->
                            slideOutVertically(animationSpec = tween(600)) { it } +
                                    fadeOut(animationSpec = tween(400))
                        else -> null
                    }
                }
            ) {
                val model = hiltViewModel<AppDetailsViewModel>()
                AppDetailsScreen(snackbarHostState = snackbarHostState, viewModel = model)
            }
            composable(Screen.Settings.route, enterTransition = {
                when (initialState.destination.route) {
                    Screen.AppList.route,
                    Screen.InstalledApps.route,
                    Screen.AppUpdates.route ->
                        slideInVertically(animationSpec = tween(400)) { -it } +
                                fadeIn(animationSpec = tween(400))
                    else -> null
                }
            }, exitTransition = {
                when (targetState.destination.route) {
                    Screen.AppList.route,
                    Screen.InstalledApps.route,
                    Screen.AppUpdates.route ->
                        slideOutVertically(animationSpec = tween(600)) { -it } +
                                fadeOut(animationSpec = tween(400))
                    else -> null
                }
            }) {
                val model = hiltViewModel<SettingsViewModel>()
                SettingsScreen(padding = padding, viewModel = model)
            }
        }
    }
}
