package app.accrescent.client.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AppUpdatesScreen(padding: PaddingValues) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(padding), contentAlignment = Alignment.Center) {
        Text("Just kidding. This is a shim.")
    }
}
