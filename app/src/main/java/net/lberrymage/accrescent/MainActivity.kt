package net.lberrymage.accrescent

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import net.lberrymage.accrescent.data.MetadataFetcherImpl
import net.lberrymage.accrescent.data.MetadataRemoteDataSource
import net.lberrymage.accrescent.data.MetadataRepository
import net.lberrymage.accrescent.ui.theme.AccrescentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccrescentTheme {
                // A surface container using the 'background' color from the theme
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
fun DisplayButton() {
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        var displayText by remember { mutableStateOf("") }

        Button(onClick = {
            val metadataFetcher = MetadataFetcherImpl()
            val metadataRemoteDataSource = MetadataRemoteDataSource(metadataFetcher)
            val metadataRepository = MetadataRepository(metadataRemoteDataSource)

            scope.launch { displayText = metadataRepository.fetchLatestMetadata().data }
        }) {
            Text("Refresh")
        }
        if (displayText.isNotEmpty()) {
            Text(displayText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AccrescentTheme {
        DisplayButton()
    }
}