package app.accrescent.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import app.accrescent.client.ui.theme.AccrescentTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainContent() {
    val scaffoldState = rememberScaffoldState(snackbarHostState = SnackbarHostState())
    val navController = rememberAnimatedNavController()
    val screens = listOf(Screen.AppList, Screen.AppUpdates)

    val showBottomBar =
        navController.currentBackStackEntryAsState().value?.destination?.route in screens.map { it.route }

    Scaffold(scaffoldState = scaffoldState, content = { padding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = Screen.AppList.route,
        ) {
            composable(Screen.AppList.route, enterTransition = {
                when (initialState.destination.route) {
                    Screen.AppUpdates.route ->
                        slideInHorizontally(animationSpec = tween(350)) { -it }
                    else -> null
                }
            }, exitTransition = {
                when (targetState.destination.route) {
                    "${Screen.AppDetails.route}/{appId}" ->
                        fadeOut(animationSpec = tween(350))
                    Screen.AppUpdates.route ->
                        slideOutHorizontally(animationSpec = tween(350)) { -it }
                    else -> null
                }
            }) {
                val model = hiltViewModel<AppListViewModel>()
                AppListScreen(
                    navController = navController,
                    scaffoldState = scaffoldState,
                    padding = padding,
                    viewModel = model,
                )
            }
            composable(Screen.AppUpdates.route, enterTransition = {
                when (initialState.destination.route) {
                    Screen.AppList.route ->
                        slideInHorizontally(animationSpec = tween(350)) { it }
                    else -> null
                }
            }, exitTransition = {
                when (targetState.destination.route) {
                    Screen.AppList.route ->
                        slideOutHorizontally(animationSpec = tween(350)) { it }
                    else -> null
                }
            }) { AppUpdatesScreen(padding = padding) }
            composable(
                "${Screen.AppDetails.route}/{appId}",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "https://accrescent.app/app/{appId}"
                }),
                enterTransition = {
                    when (initialState.destination.route) {
                        Screen.AppList.route ->
                            slideInVertically(animationSpec = tween(400)) { it } +
                                    fadeIn(animationSpec = tween(400))
                        else -> null
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Screen.AppList.route ->
                            slideOutVertically(animationSpec = tween(600)) { it } +
                                    fadeOut(animationSpec = tween(400))
                        else -> null
                    }
                }
            ) {
                val model = hiltViewModel<AppDetailsViewModel>()
                AppDetailsScreen(scaffoldState = scaffoldState, viewModel = model)
            }
        }
    }, bottomBar = {
        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(animationSpec = tween(400)) { it },
            exit = slideOutVertically(animationSpec = tween(400)) { it },
        ) {
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
