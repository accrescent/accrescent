package app.accrescent.client.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.accrescent.client.data.REPOSITORY_URL
import coil.compose.AsyncImage

@Composable
fun AppIcon(appId: String, modifier: Modifier = Modifier) {
    AsyncImage(
        "$REPOSITORY_URL/apps/$appId/icon.png",
        "App icon",
        modifier.clip(CircleShape),
    )
}
