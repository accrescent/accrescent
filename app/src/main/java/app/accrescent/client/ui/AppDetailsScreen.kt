package app.accrescent.client.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppDetailsScreen(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    viewModel: AppDetailsViewModel = viewModel(),
) {
    when {
        viewModel.uiState.isFetchingData -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(72.dp))
            }
        }
        viewModel.uiState.appExists -> AppDetails(
            id = viewModel.uiState.appId,
            name = viewModel.uiState.appName,
            versionName = viewModel.uiState.versionName,
            versionCode = viewModel.uiState.versionCode,
        )
        else -> AppNotFoundError()
    }

    if (viewModel.uiState.error != null) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(message = viewModel.uiState.error!!)
        }
    }
}

@Composable
fun AppDetails(id: String, name: String, versionName: String, versionCode: Long) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(name, style = MaterialTheme.typography.h2)
        Column(Modifier.width(256.dp), verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Version: $versionName",
                style = MaterialTheme.typography.body2
            )
            Text(
                "Version code: $versionCode",
                style = MaterialTheme.typography.body2
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        Text(
            id,
            Modifier.align(Alignment.BottomCenter),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun AppNotFoundError() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.onError
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    "Sorry, we couldn't find that app",
                    Modifier.align(Alignment.CenterVertically),
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.h4
                )
            }
        }
    }
}
