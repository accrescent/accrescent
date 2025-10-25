// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import app.accrescent.client.R
import app.accrescent.client.ui.components.ActionableCard
import app.accrescent.client.ui.components.AppCard
import build.buf.gen.accrescent.appstore.v1.AppListing

private val APP_CARD_PADDING = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

/**
 * Composes a list of apps as a screen template.
 *
 * @param onClickApp called when an app with the given app ID is clicked
 * @param appListingPagingItems the app listings to display
 * @param modifier this composable's modifier
 * @param snackbarHostState the snackbar host state for showing snackbars
 * @param onRefresh a callback for when the user refreshes the screen
 */
@Composable
fun AppListScreenTemplate(
    onClickApp: (String) -> Unit,
    appListingPagingItems: LazyPagingItems<AppListing>,
    emptyListText: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onRefresh: () -> Unit = {},
) {
    val shouldShowErrorScreen by remember {
        derivedStateOf {
            appListingPagingItems.loadState.refresh is LoadState.Error
                    && appListingPagingItems.itemCount == 0
        }
    }
    val shouldShowEmptyListText by remember {
        derivedStateOf {
            appListingPagingItems.loadState.refresh is LoadState.NotLoading
                    && appListingPagingItems.itemCount == 0
        }
    }
    var currentLoadIsUserInitiated by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val retryFailedLoad = {
        currentLoadIsUserInitiated = true
        appListingPagingItems.retry()
    }

    suspend fun showErrorSnackbar() {
        val result = snackbarHostState.showSnackbar(
            message = context.getString(R.string.error_while_refreshing),
            actionLabel = context.getString(R.string.retry),
            withDismissAction = true,
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.Dismissed -> currentLoadIsUserInitiated = false
            SnackbarResult.ActionPerformed -> appListingPagingItems.retry()
        }
    }

    // Show snackbar on errors caused by user-initiated loads, i.e., refreshes and retries
    LaunchedEffect(appListingPagingItems.loadState.refresh) {
        if (
            appListingPagingItems.loadState.refresh is LoadState.Error
            && currentLoadIsUserInitiated
        ) {
            showErrorSnackbar()
        }
    }
    LaunchedEffect(appListingPagingItems.loadState.append) {
        if (
            appListingPagingItems.loadState.append is LoadState.Error
            && currentLoadIsUserInitiated
        ) {
            showErrorSnackbar()
        }
    }

    PullToRefreshBox(
        isRefreshing = appListingPagingItems.loadState.refresh == LoadState.Loading,
        onRefresh = {
            currentLoadIsUserInitiated = true
            appListingPagingItems.refresh()
            onRefresh()
        },
        modifier = modifier,
    ) {
        val verticalArrangement = if (shouldShowErrorScreen || shouldShowEmptyListText) {
            Arrangement.Center
        } else {
            Arrangement.Top
        }

        LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = verticalArrangement) {
            if (shouldShowErrorScreen) {
                item {
                    ActionableCard(
                        bodyText = stringResource(R.string.error_while_refreshing),
                        actionText = stringResource(R.string.retry),
                        onActionClicked = retryFailedLoad,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        cardColors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                        buttonColors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    )
                }
            } else if (shouldShowEmptyListText) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = emptyListText)
                    }
                }
            } else {
                // We have no need to handle appListingPagingItems.loadState.prepend here since
                // AppListingPagingSource doesn't support prepends, i.e., it always sets
                // prevKey == null

                items(
                    count = appListingPagingItems.itemCount,
                    key = appListingPagingItems.itemKey { listing -> listing.appId },
                ) { index ->
                    val appListing = appListingPagingItems[index]
                    // appListing should never be null since placeholders are disabled in our
                    // ViewModel's PagingSource, but we might as well handle that case safely
                    if (appListing != null) {
                        AppCard(
                            name = appListing.name,
                            iconUrl = appListing.icon.url,
                            modifier = Modifier
                                .padding(APP_CARD_PADDING)
                                .clickable { onClickApp(appListing.appId) },
                        )
                    }
                }

                when (appListingPagingItems.loadState.append) {
                    is LoadState.Error -> item {
                        ActionableCard(
                            bodyText = stringResource(R.string.error_while_loading_more_items),
                            actionText = stringResource(R.string.retry),
                            onActionClicked = retryFailedLoad,
                            modifier = Modifier.padding(APP_CARD_PADDING),
                        )
                    }

                    LoadState.Loading -> item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}
