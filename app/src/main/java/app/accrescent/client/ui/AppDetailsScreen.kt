package app.accrescent.client.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.accrescent.client.R

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
            viewModel.uiState.error = null
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
        Text(name, style = MaterialTheme.typography.headlineMedium)
        Column(Modifier.width(256.dp), verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.version, versionName),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.version_code, versionCode),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        Text(
            id,
            Modifier.align(Alignment.BottomCenter),
            style = MaterialTheme.typography.bodyLarge
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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                Text(
                    stringResource(R.string.cant_find_app),
                    Modifier.align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
