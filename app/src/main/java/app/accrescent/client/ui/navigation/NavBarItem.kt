// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface NavBarItem {
    val navIcon: ImageVector
    val navIconSelected: ImageVector
    val descriptionResourceId: Int
}
