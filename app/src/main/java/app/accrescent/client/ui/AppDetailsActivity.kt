package app.accrescent.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.accrescent.client.ui.theme.AccrescentTheme
import dagger.hilt.android.AndroidEntryPoint

const val EXTRA_APPID = "app.accrescent.client.APPID"

@AndroidEntryPoint
class AppDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccrescentTheme {
                Surface(color = MaterialTheme.colors.background) {
                    AppDetailScreen()
                }
            }
        }
    }
}

@Composable
fun AppDetailScreen(viewModel: AppDetailsViewModel = viewModel()) {
    when {
        viewModel.uiState.isFetchingData -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(72.dp))
            }
        }
        viewModel.uiState.appExists -> AppDetails()
        else -> AppNotFoundError()
    }
}

@Composable
fun AppDetails(viewModel: AppDetailsViewModel = viewModel()) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(viewModel.uiState.appName, style = MaterialTheme.typography.h2)
        Column(Modifier.width(256.dp), verticalArrangement = Arrangement.Center) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Version: ${viewModel.uiState.versionName}",
                style = MaterialTheme.typography.body2
            )
            Text(
                "Version code: ${viewModel.uiState.versionCode}",
                style = MaterialTheme.typography.body2
            )
        }
    }
    Box(Modifier.fillMaxSize()) {
        Text(
            viewModel.uiState.appId,
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
