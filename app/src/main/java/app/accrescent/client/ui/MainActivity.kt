package app.accrescent.client.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.accrescent.client.R
import app.accrescent.client.data.BLOG_FUTURE_OF_ACCRESCENT_URL
import app.accrescent.client.data.DONATE_URL
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.ROOT_DOMAIN
import app.accrescent.client.data.Theme
import app.accrescent.client.ui.theme.AccrescentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appId = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)

        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle(false)
            val theme by viewModel.theme.collectAsStateWithLifecycle(Theme.SYSTEM.name)

            AccrescentTheme(
                dynamicColor = dynamicColor,
                darkTheme = when (Theme.valueOf(theme)) {
                    Theme.DARK -> true
                    Theme.LIGHT -> false
                    Theme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                MainContent(appId)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    appId: String?,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val screens = listOf(Screen.AppList, Screen.InstalledApps, Screen.AppUpdates)

    val showBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in screens.map { it.route }
    val showDonateCard by viewModel.shouldShowDonateRequest().collectAsStateWithLifecycle(false)

    val startDestination =
        if (appId != null) "${Screen.AppDetails.route}/{appId}" else Screen.AppList.route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val settingsScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
    )

    Scaffold(
        modifier = if (currentDestination?.route == Screen.Settings.route) {
            modifier.nestedScroll(settingsScrollBehavior.nestedScrollConnection)
        } else {
            modifier
        },
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
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    scrollBehavior = settingsScrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_button))
                        }
                    },
                )
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
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
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
                    AppList(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
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
                    AppList(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        filter = {
                            it == InstallStatus.INSTALLED || it == InstallStatus.UPDATABLE
                                    || it == InstallStatus.DISABLED
                        },
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
                    AppList(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
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
                    AppDetailsScreen(snackbarHostState)
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
                    SettingsScreen()
                }
            }

            AnimatedVisibility(
                visible = showDonateCard,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            ) {
                val context = LocalContext.current

                DonateRequestCard(
                    onDismiss = { viewModel.updateDonateRequestLastSeen() },
                    onDonate = {
                        val intent = Intent(Intent.ACTION_VIEW, DONATE_URL.toUri())
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                        viewModel.updateDonateRequestLastSeen()
                    },
                    onInfo = {
                        val intent = Intent(Intent.ACTION_VIEW, BLOG_FUTURE_OF_ACCRESCENT_URL.toUri())
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    },
                )
            }
        }
    }
}
