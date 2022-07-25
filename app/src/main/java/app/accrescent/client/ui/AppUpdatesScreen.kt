package app.accrescent.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun AppUpdatesScreen(
    navController: NavController,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    padding: PaddingValues,
    viewModel: AppListViewModel = viewModel(),
) {
    val apps by viewModel.apps.collectAsState(emptyList())
    val installStatuses = viewModel.installStatuses
    val updatableApps = apps.filter { installStatuses[it.id] == InstallStatus.UPDATABLE }

    SwipeRefresh(
        modifier = Modifier.padding(padding),
        state = rememberSwipeRefreshState(viewModel.isRefreshing),
        onRefresh = {
            viewModel.refreshRepoData()
            viewModel.refreshInstallStatuses()
        },
    ) {
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            if (apps.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.swipe_refresh),
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else if (updatableApps.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.up_to_date),
                        Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                item { Spacer(Modifier.height(16.dp)) }
                items(updatableApps, key = { app -> app.id }) { app ->
                    InstallableAppCard(
                        app = app,
                        navController = navController,
                        installStatus = installStatuses[app.id] ?: InstallStatus.LOADING,
                        onInstallClicked = viewModel::installApp,
                        onUninstallClicked = viewModel::uninstallApp,
                        onOpenClicked = viewModel::openApp,
                    )
                }
            }
        }

        if (viewModel.error != null) {
            LaunchedEffect(scaffoldState.snackbarHostState) {
                scaffoldState.snackbarHostState.showSnackbar(message = viewModel.error!!)
                viewModel.error = null
            }
        }
    }
}
