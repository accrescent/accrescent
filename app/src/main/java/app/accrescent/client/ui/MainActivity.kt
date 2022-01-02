package app.accrescent.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.accrescent.client.ui.theme.AccrescentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccrescentTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RefreshButton()
                        AppList()
                    }
                }
            }
        }
    }
}

@Composable
fun RefreshButton(viewModel: MainViewModel = viewModel()) {
    Button(onClick = { viewModel.refreshRepoData() }, Modifier.padding(12.dp)) {
        Text("Refresh")
    }
}

@Composable
fun AppList(viewModel: MainViewModel = viewModel()) {
    val apps by viewModel.apps.collectAsState(emptyList())

    LazyColumn {
        items(apps) {
            InstallableAppCard(it.id)
        }
    }
}

@Composable
fun InstallableAppCard(appId: String, viewModel: MainViewModel = viewModel()) {
    Card(Modifier.padding(8.dp), backgroundColor = MaterialTheme.colors.primary) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(appId, modifier = Modifier.padding(8.dp))
            Button(
                onClick = { viewModel.installApp(appId) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
            ) {
                Text("Install", color = Color.LightGray)
            }
        }
    }
}
