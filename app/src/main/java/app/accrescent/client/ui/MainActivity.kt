package app.accrescent.client.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import app.accrescent.client.data.db.App
import app.accrescent.client.ui.theme.AccrescentTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccrescentTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState(snackbarHostState = viewModel.snackbarHostState)

    Scaffold(scaffoldState = scaffoldState) {
        AppList()
    }
}

@Composable
fun AppList(viewModel: MainViewModel = viewModel()) {
    val apps by viewModel.apps.collectAsState(emptyList())
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { viewModel.refreshRepoData() }
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
                items(apps) {
                    InstallableAppCard(it)
                }
            }
        }
    }
}

@Composable
fun InstallableAppCard(app: App, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                val intent = Intent(context, AppDetailsActivity::class.java).apply {
                    putExtra(EXTRA_APPID, app.id)
                }
                startActivity(context, intent, null)
            },
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
                onClick = { viewModel.installApp(app.id) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant)
            ) {
                Text("Install", color = Color.LightGray)
            }
        }
    }
}
