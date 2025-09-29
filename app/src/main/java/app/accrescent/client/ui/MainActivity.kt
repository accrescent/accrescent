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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import app.accrescent.client.R
import app.accrescent.client.data.BLOG_FUTURE_OF_ACCRESCENT_URL
import app.accrescent.client.data.DONATE_URL
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
    val routes = listOf(Route.AllApps, Route.InstalledApps, Route.UpdatableApps)

    val showBottomBar = routes.any {
        navController.currentBackStackEntryAsState().value?.destination?.hasRoute(it::class) == true
    }
    val showDonateCard by viewModel.shouldShowDonateRequest().collectAsStateWithLifecycle(false)

    val startDestination = if (appId != null) Route.AppDetails(appId = appId) else Route.AllApps
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val settingsScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
    )

    Scaffold(
        modifier = if (currentDestination?.hasRoute<Route.Settings>() == true) {
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
                visible = currentDestination?.hasRoute<Route.AppDetails>() == true,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CenterAlignedTopAppBar(title = {})
            }
            AnimatedVisibility(
                visible = currentDestination?.hasRoute<Route.Settings>() == true,
                enter = fadeIn(),
                exit = fadeOut(),
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
                visible = currentDestination?.hasRoute<Route.AppDetails>() != true
                        && currentDestination?.hasRoute<Route.Settings>() != true,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CenterAlignedTopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { navController.navigate(Route.Settings) }) {
                            Icon(
                                imageVector = Route.Settings.navIcon,
                                contentDescription = stringResource(Route.Settings.descriptionResourceId)
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                NavigationBar {
                    routes.forEach { route ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.hasRoute(route::class) } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) route.navIconSelected else route.navIcon,
                                    contentDescription = stringResource(route.descriptionResourceId)
                                )
                            },
                            label = { Text(stringResource(route.descriptionResourceId)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
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
                composable<Route.AllApps>(enterTransition = {
                    val dest = initialState.destination
                    when {
                        dest.hasRoute<Route.InstalledApps>() || dest.hasRoute<Route.UpdatableApps>() ->
                            slideInHorizontally { -it }

                        else -> null
                    }
                }, exitTransition = {
                    val dest = targetState.destination
                    when {
                        dest.hasRoute<Route.AppDetails>() -> fadeOut()
                        dest.hasRoute<Route.InstalledApps>() ||
                                dest.hasRoute<Route.UpdatableApps>() -> slideOutHorizontally { -it }

                        dest.hasRoute<Route.Settings>() -> fadeOut()
                        else -> null
                    }
                }) {
                    AllAppsScreen(
                        onClickApp = { navController.navigate(Route.AppDetails(appId = it)) },
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable<Route.InstalledApps>(enterTransition = {
                    val dest = initialState.destination
                    when {
                        dest.hasRoute<Route.AllApps>() -> slideInHorizontally { it }
                        dest.hasRoute<Route.UpdatableApps>() -> slideInHorizontally { -it }
                        else -> null
                    }
                }, exitTransition = {
                    val dest = targetState.destination
                    when {
                        dest.hasRoute<Route.AppDetails>() -> fadeOut()
                        dest.hasRoute<Route.AllApps>() -> slideOutHorizontally { it }
                        dest.hasRoute<Route.UpdatableApps>() -> slideOutHorizontally { -it }
                        dest.hasRoute<Route.Settings>() -> fadeOut()
                        else -> null
                    }
                }) {
                    InstalledAppsScreen(
                        onClickApp = { navController.navigate(Route.AppDetails(appId = it)) },
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable<Route.UpdatableApps>(enterTransition = {
                    val dest = initialState.destination
                    when {
                        dest.hasRoute<Route.InstalledApps>() ||
                                dest.hasRoute<Route.AllApps>() -> slideInHorizontally { it }

                        else -> null
                    }
                }, exitTransition = {
                    val dest = targetState.destination
                    when {
                        dest.hasRoute<Route.AppDetails>() -> fadeOut()
                        dest.hasRoute<Route.AllApps>() ||
                                dest.hasRoute<Route.InstalledApps>() -> slideOutHorizontally { it }

                        dest.hasRoute<Route.Settings>() -> fadeOut()
                        else -> null
                    }
                }) {
                    UpdatableAppsScreen(
                        onClickApp = { navController.navigate(Route.AppDetails(appId = it)) },
                        snackbarHostState = snackbarHostState,
                    )
                }
                composable<Route.AppDetails>(
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "https://${ROOT_DOMAIN}/app/{appId}"
                    }),
                    enterTransition = {
                        val dest = initialState.destination
                        when {
                            dest.hasRoute<Route.AllApps>() ||
                                    dest.hasRoute<Route.InstalledApps>() ||
                                    dest.hasRoute<Route.UpdatableApps>() ->
                                slideInVertically { it } + fadeIn()

                            else -> null
                        }
                    },
                    exitTransition = {
                        val dest = targetState.destination
                        when {
                            dest.hasRoute<Route.AllApps>() ||
                                    dest.hasRoute<Route.InstalledApps>() ||
                                    dest.hasRoute<Route.UpdatableApps>() ->
                                slideOutVertically { it } + fadeOut()

                            else -> null
                        }
                    }
                ) {
                    AppDetailsScreen(snackbarHostState)
                }
                composable<Route.Settings>(enterTransition = {
                    val dest = initialState.destination
                    when {
                        dest.hasRoute<Route.AllApps>() ||
                                dest.hasRoute<Route.InstalledApps>() ||
                                dest.hasRoute<Route.UpdatableApps>() ->
                            slideInVertically { -it } + fadeIn()

                        else -> null
                    }
                }, exitTransition = {
                    val dest = targetState.destination
                    when {
                        dest.hasRoute<Route.AllApps>() ||
                                dest.hasRoute<Route.InstalledApps>() ||
                                dest.hasRoute<Route.UpdatableApps>() ->
                            slideOutVertically { -it } + fadeOut()

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
