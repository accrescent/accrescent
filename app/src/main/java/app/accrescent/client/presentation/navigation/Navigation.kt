package app.accrescent.client.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.ROOT_DOMAIN
import app.accrescent.client.presentation.screens.app_details.AppDetailsScreen
import app.accrescent.client.presentation.screens.app_list.AppList
import app.accrescent.client.presentation.screens.settings.SettingsScreen

@Composable
fun Navigation(
    searchQuery: String,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.AppList,
    navController: NavHostController = rememberNavController()
) {


    NavHost(
        modifier = modifier, navController = navController, startDestination = startDestination
    ) {
        composable<Screen.AppList>(enterTransition = {
            val destination = initialState.destination

            if (destination.hasRoute(Screen.InstalledApps::class) || destination.hasRoute(Screen.AppUpdates::class))
                slideInHorizontally(animationSpec = tween(350)) { -it }
            else null

        }, exitTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppDetails::class) -> fadeOut(
                    animationSpec = tween(
                        350
                    )
                )

                destination.hasRoute(Screen.InstalledApps::class) -> slideOutHorizontally(
                    animationSpec = tween(350)
                ) { -it }

                destination.hasRoute(Screen.AppUpdates::class) -> slideOutHorizontally(
                    animationSpec = tween(350)
                ) { -it }

                destination.hasRoute(Screen.Settings::class) -> fadeOut(
                    animationSpec = tween(
                        350
                    )
                )

                else -> null

            }
        }) {
            AppList(
                navController = navController,
                searchQuery = searchQuery,
                snackbarHostState = snackbarHostState
            )
        }

        composable<Screen.InstalledApps>(enterTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppList::class) -> slideInHorizontally(
                    animationSpec = tween(
                        350
                    )
                ) { it }

                destination.hasRoute(Screen.AppUpdates::class) -> slideInHorizontally(
                    animationSpec = tween(350)
                ) { -it }

                else -> null

            }
        }, exitTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppDetails::class) -> fadeOut(
                    animationSpec = tween(
                        350
                    )
                )

                destination.hasRoute(Screen.AppList::class) -> slideOutHorizontally(
                    animationSpec = tween(350)
                ) { it }

                destination.hasRoute(Screen.AppUpdates::class) -> slideOutHorizontally(
                    animationSpec = tween(350)
                ) { -it }

                destination.hasRoute(Screen.Settings::class) -> fadeOut(
                    animationSpec = tween(
                        350
                    )
                )

                else -> null

            }
        }) {
            AppList(
                navController = navController,
                searchQuery = searchQuery,
                snackbarHostState = snackbarHostState,
                filter = {
                    it == InstallStatus.INSTALLED || it == InstallStatus.UPDATABLE || it == InstallStatus.DISABLED
                },
                noFilterResultsText = stringResource(R.string.no_apps_installed),
            )
        }

        composable<Screen.AppUpdates>(enterTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.InstalledApps::class) -> slideInHorizontally(
                    animationSpec = tween(350)
                ) { it }

                destination.hasRoute(Screen.AppList::class) -> slideInHorizontally(
                    animationSpec = tween(
                        350
                    )
                ) { it }

                else -> null

            }
        }, exitTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppDetails::class) -> fadeOut(animationSpec = tween(350))

                destination.hasRoute(Screen.AppList::class) -> slideOutHorizontally(
                    animationSpec = tween(
                        350
                    )
                ) { it }

                destination.hasRoute(Screen.InstalledApps::class) -> slideOutHorizontally(
                    animationSpec = tween(350)
                ) { it }

                destination.hasRoute(Screen.Settings::class) -> fadeOut(animationSpec = tween(350))

                else -> null

            }
        }) {
            AppList(
                navController = navController,
                searchQuery = searchQuery,
                snackbarHostState = snackbarHostState,
                filter = { it == InstallStatus.UPDATABLE },
                noFilterResultsText = stringResource(R.string.up_to_date),
            )
        }

        composable<Screen.AppDetails>(deepLinks = listOf(navDeepLink {
            uriPattern = "https://$ROOT_DOMAIN/app/{appId}"
        }), enterTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppList::class) -> slideInVertically(
                    animationSpec = tween(
                        400
                    )
                ) { it } + fadeIn(animationSpec = tween(400))

                destination.hasRoute(Screen.InstalledApps::class) -> slideInVertically(
                    animationSpec = tween(400)
                ) { it } + fadeIn(animationSpec = tween(400))

                destination.hasRoute(Screen.AppUpdates::class) -> slideInVertically(
                    animationSpec = tween(400)
                ) { it } + fadeIn(animationSpec = tween(400))

                else -> null

            }
        }, exitTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppList::class) -> slideOutVertically(
                    animationSpec = tween(
                        600
                    )
                ) { it } + fadeOut(animationSpec = tween(400))

                destination.hasRoute(Screen.InstalledApps::class) -> slideOutVertically(
                    animationSpec = tween(600)
                ) { it } + fadeOut(animationSpec = tween(400))

                destination.hasRoute(Screen.AppUpdates::class) -> slideOutVertically(
                    animationSpec = tween(600)
                ) { it } + fadeOut(animationSpec = tween(400))

                else -> null

            }
        }) {
            AppDetailsScreen(snackbarHostState)
        }

        composable<Screen.Settings>(enterTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppList::class) -> slideInVertically(
                    animationSpec = tween(
                        400
                    )
                ) { -it } + fadeIn(animationSpec = tween(400))

                destination.hasRoute(Screen.InstalledApps::class) -> slideInVertically(
                    animationSpec = tween(
                        400
                    )
                ) { -it } + fadeIn(animationSpec = tween(400))

                destination.hasRoute(Screen.AppUpdates::class) -> slideInVertically(
                    animationSpec = tween(
                        400
                    )
                ) { -it } + fadeIn(animationSpec = tween(400))

                else -> null

            }
        }, exitTransition = {
            val destination = initialState.destination

            when {
                destination.hasRoute(Screen.AppList::class) -> slideOutVertically(
                    animationSpec = tween(
                        600
                    )
                ) { -it } + fadeOut(animationSpec = tween(400))

                destination.hasRoute(Screen.InstalledApps::class) -> slideOutVertically(
                    animationSpec = tween(600)
                ) { -it } + fadeOut(animationSpec = tween(400))

                destination.hasRoute(Screen.AppUpdates::class) -> slideOutVertically(
                    animationSpec = tween(
                        600
                    )
                ) { -it } + fadeOut(animationSpec = tween(400))

                else -> null

            }
        }) {
            SettingsScreen()
        }
    }

}