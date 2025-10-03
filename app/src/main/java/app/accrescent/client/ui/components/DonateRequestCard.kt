package app.accrescent.client.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.accrescent.client.R
import app.accrescent.client.ui.theme.AccrescentTheme
import app.accrescent.client.ui.theme.isAppInDarkTheme

private const val CARD_PADDING_DP = 12

@Composable
fun DonateRequestCard(
    onDismiss: () -> Unit,
    onDonate: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier) {
        Box {
            Column(modifier = Modifier.padding(CARD_PADDING_DP.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAppInDarkTheme()) {
                            Icons.Rounded.VolunteerActivism
                        } else {
                            Icons.Outlined.VolunteerActivism
                        },
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = CARD_PADDING_DP.dp)
                            .size(60.dp)
                            .border(
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = LocalContentColor.current,
                                ),
                                shape = RoundedCornerShape(50),
                            )
                            .padding(10.dp),
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.donate_card_heading),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(R.string.donate_card_desc),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onInfo) {
                        Text(stringResource(R.string.donate_card_info_btn))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Button(onClick = onDonate) {
                        Text(stringResource(R.string.donate_card_donate_btn))
                    }
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.donate_card_dismiss_btn),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DonateRequestCardPreviewLightDark() {
    AccrescentTheme(dynamicColor = false) {
        DonateRequestCard(onDismiss = {}, onDonate = {}, onInfo = {})
    }
}

@PreviewDynamicColors
@Composable
private fun DonateRequestCardPreviewDynamicColors() {
    AccrescentTheme(dynamicColor = true) {
        DonateRequestCard(onDismiss = {}, onDonate = {}, onInfo = {})
    }
}
