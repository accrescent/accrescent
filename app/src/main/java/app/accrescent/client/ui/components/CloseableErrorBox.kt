package app.accrescent.client.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.accrescent.client.R
import app.accrescent.client.ui.theme.AccrescentTheme

@Composable
fun CloseableErrorBox(
    errorText: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = errorText,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.close),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CloseableErrorBoxPreviewLightDark() {
    AccrescentTheme(dynamicColor = false) {
        CloseableErrorBox(
            errorText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
            onClose = {},
        )
    }
}
