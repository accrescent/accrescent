package app.accrescent.client.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import app.accrescent.client.R
import app.accrescent.client.ui.components.AppCard
import app.accrescent.client.ui.components.CenteredText
import app.accrescent.client.ui.state.InstalledAppsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InstalledAppsScreen(
    onClickApp: (appId: String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    viewModel: InstalledAppsViewModel = hiltViewModel(),
) {
    val appListings = viewModel.appListings.collectAsLazyPagingItems()

    val refreshScope = rememberCoroutineScope()
    val refreshingMessage = stringResource(R.string.swipe_refreshing)
    val state = rememberPullRefreshState(viewModel.isRefreshing, onRefresh = {
        refreshScope.launch { viewModel.refreshRepoData() }
    })

    Box(modifier.pullRefresh(state)) {
        val verticalArrangement = if (appListings.itemCount == 0) Arrangement.Center else Arrangement.Top

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = verticalArrangement) {
            if (appListings.itemCount == 0) {
                item { CenteredText(stringResource(R.string.no_apps_installed)) }
            } else {
                item { Spacer(Modifier.height(16.dp)) }
                items(count = appListings.itemCount, appListings.itemKey { it.appId }) { index ->
                    val listing = appListings[index]

                    // Placeholders are disabled, so the listing shouldn't ever be null. We can
                    // choose to enable placeholders later.
                    if (listing != null) {
                        AppCard(
                            name = listing.name,
                            iconUrl = listing.icon.url,
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(CardDefaults.shape)
                                .clickable { onClickApp(listing.appId) }
                        )
                    }
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
            modifier = Modifier
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = if (viewModel.isRefreshing) {
                        refreshingMessage
                    } else {
                        ""
                    }
                }
                .align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
