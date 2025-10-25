// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.accrescent.client.ui.theme.AccrescentTheme

/**
 * An simple [Card] for displaying a message and an action button.
 *
 * @param bodyText the text for the main body of the card
 * @param actionText the text for the action button
 * @param onActionClicked the callback invoked when the action button is clicked
 * @param cardColors the colors to apply to the card element
 * @param buttonColors the colors to apply to the action button
 */
@Composable
fun ActionableCard(
    bodyText: String,
    actionText: String,
    onActionClicked: () -> Unit,
    modifier: Modifier = Modifier,
    cardColors: CardColors = CardDefaults.cardColors(),
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    Card(modifier = modifier, colors = cardColors) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = bodyText)
            Button(
                onClick = onActionClicked,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End),
                colors = buttonColors,
            ) {
                Text(text = actionText)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ActionableCardPreviewLightDark() {
    AccrescentTheme(dynamicColor = false) {
        ActionableCard(
            bodyText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod"
                    + " tempor incididunt ut labore et dolore magna aliqua.",
            actionText = "Learn more",
            onActionClicked = {},
        )
    }
}
