package app.accrescent.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import app.accrescent.client.ui.theme.AccrescentTheme
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
fun MainContent() {
    val scaffoldState = rememberScaffoldState(snackbarHostState = SnackbarHostState())
    val navController = rememberNavController()

    Scaffold(scaffoldState = scaffoldState, content = { padding ->
        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = "list",
        ) {
            composable("list") {
                val model = hiltViewModel<AppListViewModel>()
                AppListScreen(
                    navController = navController,
                    scaffoldState = scaffoldState,
                    viewModel = model,
                )
            }
            composable(
                "details/{appId}",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "https://accrescent.app/app/{appId}"
                })
            ) {
                val model = hiltViewModel<AppDetailsViewModel>()
                AppDetailsScreen(scaffoldState = scaffoldState, viewModel = model)
            }
        }
    })
}
