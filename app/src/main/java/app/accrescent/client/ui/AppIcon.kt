package app.accrescent.client.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.accrescent.client.data.REPOSITORY_URL
import coil3.compose.SubcomposeAsyncImage

@Composable
fun AppIcon(appId: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        "$REPOSITORY_URL/apps/$appId/icon.png",
        null,
        modifier.clip(CircleShape),
        loading = { CircularProgressIndicator() },
    )
}
