package app.accrescent.client.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import app.accrescent.client.R
import app.accrescent.client.ui.state.InstalledAppsViewModel

/**
 * Composes the screen listing all installed apps available in the store.
 *
 * @param onClickApp called when an app with the given ID is clicked
 * @param modifier this composable's modifier
 * @param snackbarHostState the snackbar host state for showing snackbars
 * @param viewModel this screen's ViewModel
 */
@Composable
fun InstalledAppsScreen(
    onClickApp: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    viewModel: InstalledAppsViewModel = hiltViewModel(),
) {
    val appListingPagingItems = viewModel.appListings.collectAsLazyPagingItems()

    AppListScreenTemplate(
        onClickApp = onClickApp,
        appListingPagingItems = appListingPagingItems,
        emptyListText = stringResource(R.string.no_apps_installed),
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}
