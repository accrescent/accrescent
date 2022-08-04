package app.accrescent.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun InstalledAppsScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    padding: PaddingValues,
    viewModel: AppListViewModel = viewModel(),
) {
    val apps by viewModel.apps.collectAsState(emptyList())
    val installStatuses = viewModel.installStatuses
    val installedApps = apps.filter {
        when (installStatuses[it.id]) {
            InstallStatus.INSTALLED, InstallStatus.UPDATABLE -> true
            else -> false
        }
    }

    SwipeRefresh(
        modifier = Modifier.padding(padding),
        state = rememberSwipeRefreshState(viewModel.isRefreshing),
        onRefresh = {
            viewModel.refreshRepoData()
            viewModel.refreshInstallStatuses()
        },
        indicator = { state, td ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = td,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
    ) {
        val verticalArrangement =
            if (apps.isEmpty() || installedApps.isEmpty()) Arrangement.Center else Arrangement.Top

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = verticalArrangement) {
            if (apps.isEmpty()) {
                item { CenteredText(stringResource(R.string.swipe_refresh)) }
            } else if (installedApps.isEmpty()) {
                item { CenteredText(stringResource(R.string.no_apps_installed)) }
            } else {
                item { Spacer(Modifier.height(16.dp)) }
                items(installedApps, key = { app -> app.id }) { app ->
                    InstallableAppCard(
                        app = app,
                        installStatus = installStatuses[app.id] ?: InstallStatus.LOADING,
                        onClick = { navController.navigate("${Screen.AppDetails.route}/${app.id}") },
                        onInstallClicked = viewModel::installApp,
                        onUninstallClicked = viewModel::uninstallApp,
                        onOpenClicked = viewModel::openApp,
                    )
                }
            }
        }

        if (viewModel.error != null) {
            LaunchedEffect(snackbarHostState) {
                snackbarHostState.showSnackbar(message = viewModel.error!!)
                viewModel.error = null
            }
        }
    }
}
