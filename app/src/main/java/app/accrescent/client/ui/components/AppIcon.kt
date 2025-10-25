// SPDX-FileCopyrightText: © 2023 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil3.compose.SubcomposeAsyncImage

@Composable
fun AppIcon(iconUrl: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        iconUrl,
        null,
        modifier.clip(CircleShape),
        loading = { CircularProgressIndicator() },
    )
}
