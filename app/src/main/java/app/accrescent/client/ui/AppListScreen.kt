package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.db.App
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun AppListScreen(
    navController: NavController,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    padding: PaddingValues,
    viewModel: AppListViewModel = viewModel(),
) {
    val apps by viewModel.apps.collectAsState(emptyList())
    val installStatuses = viewModel.installStatuses

    SwipeRefresh(
        modifier = Modifier.padding(padding),
        state = rememberSwipeRefreshState(viewModel.isRefreshing),
        onRefresh = { viewModel.refreshRepoData() },
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
            } else {
                item { Spacer(Modifier.height(16.dp)) }
                items(apps) { app ->
                    InstallableAppCard(
                        app = app,
                        navController = navController,
                        installStatus = installStatuses[app.id] ?: InstallStatus.INSTALLABLE,
                        onInstallClicked = viewModel::installApp,
                        onOpenClicked = viewModel::openApp,
                    )
                }
            }
        }

        if (viewModel.error != null) {
            LaunchedEffect(scaffoldState.snackbarHostState) {
                scaffoldState.snackbarHostState.showSnackbar(message = viewModel.error!!)
            }
        }
    }
}

@Composable
fun InstallableAppCard(
    app: App,
    navController: NavController,
    installStatus: InstallStatus,
    onInstallClicked: (String) -> Unit,
    onOpenClicked: (String) -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("${Screen.AppDetails.route}/${app.id}") },
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                app.name,
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.h4,
            )
            Button(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                onClick = {
                    when (installStatus) {
                        InstallStatus.INSTALLABLE -> onInstallClicked(app.id)
                        InstallStatus.INSTALLED -> onOpenClicked(app.id)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
            ) {
                when (installStatus) {
                    InstallStatus.INSTALLABLE ->
                        Text(stringResource(R.string.install), color = Color.LightGray)
                    InstallStatus.INSTALLED ->
                        Text(stringResource(R.string.open), color = Color.LightGray)
                }
            }
        }
    }
}
