package app.accrescent.client.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CenteredText(message: String) {
    Text(text = message, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}
