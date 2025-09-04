package app.accrescent.client.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(appId: String, name: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                appId,
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .size(48.dp),
            )
            Text(
                name,
                modifier = Modifier.padding(vertical = 24.dp),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
