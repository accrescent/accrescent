package app.accrescent.client.presentation.components

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
import app.accrescent.client.data.db.App

@Composable
fun AppCard(app: App, modifier: Modifier = Modifier) {
    Card(modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                app.id,
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .size(48.dp),
            )
            Text(
                app.name,
                modifier = Modifier.padding(vertical = 24.dp),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
