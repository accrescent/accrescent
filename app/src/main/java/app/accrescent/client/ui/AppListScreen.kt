package app.accrescent.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.util.isPrivileged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppListScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    padding: PaddingValues,
    viewModel: AppListViewModel = viewModel(),
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState(emptyList())
    val installStatuses = viewModel.installStatuses
    val requireUserAction by viewModel.requireUserAction.collectAsState(!context.isPrivileged())

    val refreshScope = rememberCoroutineScope()
    val state = rememberPullRefreshState(viewModel.isRefreshing, onRefresh = {
        refreshScope.launch {
            viewModel.refreshRepoData()
            viewModel.refreshInstallStatuses()
        }
    })

    Box(
        modifier = Modifier
            .padding(padding)
            .pullRefresh(state)
    ) {
        val verticalArrangement = if (apps.isEmpty()) Arrangement.Center else Arrangement.Top

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = verticalArrangement) {
            if (apps.isEmpty()) {
                item { CenteredText(stringResource(R.string.swipe_refresh)) }
            } else {
                item { Spacer(Modifier.height(16.dp)) }
                items(apps, key = { app -> app.id }) { app ->
                    InstallableAppCard(
                        app = app,
                        installStatus = installStatuses[app.id] ?: InstallStatus.LOADING,
                        onClick = { navController.navigate("${Screen.AppDetails.route}/${app.id}") },
                        onInstallClicked = viewModel::installApp,
                        onUninstallClicked = viewModel::uninstallApp,
                        onOpenClicked = viewModel::openApp,
                        requireUserAction = requireUserAction,
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

        PullRefreshIndicator(
            refreshing = viewModel.isRefreshing,
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
