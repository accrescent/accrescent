package net.lberrymage.accrescent.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import net.lberrymage.accrescent.ui.theme.AccrescentTheme

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
                        DisplayButton()
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayButton(viewModel: MainViewModel = viewModel()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { viewModel.refreshDevelopers() }) { Text("Refresh") }
        if (viewModel.publicKey.isNotEmpty()) {
            Text(viewModel.publicKey)
        }
    }
}