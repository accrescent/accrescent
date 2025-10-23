package app.accrescent.client.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.accrescent.client.R
import app.accrescent.client.ui.state.UpdatableAppsViewModel

@Composable
fun UpdatableAppsScreen(
    onClickApp: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    viewModel: UpdatableAppsViewModel = hiltViewModel(),
) {
    val appListingPagingItems = viewModel.appListings.collectAsLazyPagingItems()

    AppListScreenTemplate(
        onClickApp = onClickApp,
        appListingPagingItems = appListingPagingItems,
        emptyListText = stringResource(R.string.up_to_date),
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::loadUpdatableAppIds,
    )
}
