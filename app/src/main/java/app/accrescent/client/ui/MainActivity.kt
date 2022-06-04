package app.accrescent.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import app.accrescent.client.ui.theme.AccrescentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccrescentTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val scaffoldState = rememberScaffoldState(snackbarHostState = SnackbarHostState())
    val navController = rememberNavController()
    val screens = listOf(Screen.AppList, Screen.AppUpdates)

    val showBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in screens.map { it.route }

    Scaffold(scaffoldState = scaffoldState, content = { padding ->
        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = Screen.AppList.route,
        ) {
            composable(Screen.AppList.route) {
                val model = hiltViewModel<AppListViewModel>()
                AppListScreen(
                    navController = navController,
                    scaffoldState = scaffoldState,
                    viewModel = model,
                )
            }
            composable(Screen.AppUpdates.route) { AppUpdatesScreen() }
            composable(
                "app_details/{appId}",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "https://accrescent.app/app/{appId}"
                })
            ) {
                val model = hiltViewModel<AppDetailsViewModel>()
                AppDetailsScreen(scaffoldState = scaffoldState, viewModel = model)
            }
        }
    }, bottomBar = {
        if (showBottomBar) {
            BottomNavigation(modifier = Modifier.height(80.dp)) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    BottomNavigationItem(
                        modifier = Modifier.padding(16.dp),
                        icon = {
                            Icon(
                                screen.navIcon,
                                contentDescription = stringResource(screen.resourceId)
                            )
                        },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
    })
}
