package app.accrescent.client.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.accrescent.client.data.REPOSITORY_URL
import app.accrescent.client.data.db.App
import coil.compose.AsyncImage

@Composable
fun AppCard(app: App, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                "$REPOSITORY_URL/apps/${app.id}/icon.png",
                "App icon",
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .size(48.dp)
                    .clip(CircleShape),
            )
            Text(
                app.name,
                modifier = Modifier.padding(vertical = 24.dp),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
