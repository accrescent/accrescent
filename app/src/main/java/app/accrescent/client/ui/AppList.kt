package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppList(
    navController: NavController,
    searchQuery: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    viewModel: AppListViewModel = hiltViewModel(),
    filter: (installStatus: InstallStatus) -> Boolean = { true },
    noFilterResultsText: String = "",
    isUpdatesScreen: Boolean = false,
) {
    val apps by viewModel.apps.collectAsState(emptyList())
    val installStatuses = viewModel.installStatuses
    val filteredApps = apps.filter { filter(installStatuses[it.id] ?: InstallStatus.LOADING) }
        .filter { it.name.lowercase().contains(searchQuery.lowercase()) }
        .sortedBy { it.name.lowercase().indexOf(searchQuery.lowercase()) }

    val refreshScope = rememberCoroutineScope()
    val state = rememberPullRefreshState(viewModel.isRefreshing, onRefresh = {
        refreshScope.launch {
            viewModel.refreshRepoData()
            viewModel.refreshInstallStatuses()
        }
    })

    Box(modifier.pullRefresh(state)) {
        val verticalArrangement =
            if (apps.isEmpty() || filteredApps.isEmpty()) Arrangement.Center else Arrangement.Top

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = verticalArrangement) {
            if (apps.isEmpty()) {
                item { CenteredText(stringResource(R.string.swipe_refresh)) }
            } else if (filteredApps.isEmpty()) {
                item { CenteredText(noFilterResultsText) }
            } else {
                item { Spacer(Modifier.height(16.dp)) }
                items(filteredApps, key = { app -> app.id }) { app ->
                    AppCard(
                        app = app,
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { navController.navigate("${Screen.AppDetails.route}/${app.id}") },
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

        if (isUpdatesScreen && filteredApps.isNotEmpty()) {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 8.dp),
                onClick = {
                    if (viewModel.currentlyUpdatingApps.isEmpty()) {
                        viewModel.updateApps(filteredApps.map { it.id })
                    }
                },
            ) {
                Icon(Icons.Default.Download, null)
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    stringResource(
                        if (viewModel.currentlyUpdatingApps.isEmpty()) {
                            R.string.update_all
                        } else {
                            R.string.updating
                        },
                    ),
                )
            }
        }
    }
}
