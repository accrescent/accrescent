package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.accrescent.client.data.db.App
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun AppListScreen(
    navController: NavController,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    viewModel: AppListViewModel = viewModel(),
) {
    val apps by viewModel.apps.collectAsState(emptyList())

    SwipeRefresh(
        state = rememberSwipeRefreshState(viewModel.isRefreshing),
        onRefresh = { viewModel.refreshRepoData() },
    ) {
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            if (apps.isEmpty()) {
                item {
                    Text(
                        "Swipe down to refresh",
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
                        onInstallClicked = viewModel::installApp,
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
    onInstallClicked: (String) -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("details/${app.id}") },
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
                onClick = { onInstallClicked(app.id) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
            ) {
                Text("Install", color = Color.LightGray)
            }
        }
    }
}
