// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.navigation

import androidx.annotation.DrawableRes

sealed interface NavBarItem {
    @get:DrawableRes
    val navIconResId: Int

    val descriptionResourceId: Int
}
