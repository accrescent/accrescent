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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.accrescent.client.R
import app.accrescent.client.data.Theme
import app.accrescent.client.presentation.components.SearchAppBar
import app.accrescent.client.presentation.navigation.Navigation
import app.accrescent.client.presentation.navigation.Screen
import app.accrescent.client.presentation.navigation.TopLevelScreen
import app.accrescent.client.presentation.screens.settings.SettingsViewModel
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
            val dynamicColor by viewModel.dynamicColor.collectAsState(false)
            val theme by viewModel.theme.collectAsState(Theme.SYSTEM.name)

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
fun MainContent(appId: String?, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val searchQuery = remember { mutableStateOf(TextFieldValue()) }

    val startDestination =
        if (appId != null) Screen.AppDetails(appId) else Screen.AppList
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = TopLevelScreen.entries.map { it.route::class }.any { route ->
        currentDestination?.hierarchy?.any {
            it.hasRoute(route)
        } == true
    }
    val settingsScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
    )

    Scaffold(
        modifier = if (currentDestination?.hasRoute(Screen.Settings::class) == true) {
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
                visible = currentDestination?.hasRoute(Screen.AppDetails::class) == true,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                CenterAlignedTopAppBar(title = {})
            }
            AnimatedVisibility(
                visible = currentDestination?.hasRoute(Screen.Settings::class) == true,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    scrollBehavior = settingsScrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.back_button)
                            )
                        }
                    },
                )
            }
            AnimatedVisibility(
                visible = currentDestination?.hasRoute(Screen.AppDetails::class) == false && !currentDestination.hasRoute(
                    Screen.Settings::class
                ),
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400)),
            ) {
                SearchAppBar(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    // keep the search bar open if query is not empty when returning from an other screen
                    expandedInitially = searchQuery.value.text.isNotEmpty()
                ) {
                    IconButton(onClick = { navController.navigate(Screen.Settings) }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(animationSpec = tween(400)) { it },
                exit = slideOutVertically(animationSpec = tween(400)) { it },
            ) {
                NavigationBar {
                    TopLevelScreen.entries.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.hasRoute(screen.route::class) } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) screen.navIconSelected else screen.navIcon,
                                    contentDescription = stringResource(screen.title)
                                )
                            },
                            label = { Text(stringResource(screen.title)) },
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

        Navigation(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = startDestination,
            searchQuery = searchQuery.value.text,
            snackbarHostState = snackbarHostState
        )
    }
}
