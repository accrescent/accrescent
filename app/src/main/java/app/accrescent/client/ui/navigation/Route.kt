// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.navigation

import androidx.annotation.DrawableRes
import app.accrescent.client.R
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    class AppDetails(val appId: String) : Route

    @Serializable
    object Settings : Route {
        @DrawableRes
        val navIconResId = R.drawable.settings_rounded_24px
        val descriptionResourceId = R.string.settings
    }

    @Serializable
    object AllApps : Route, NavBarItem {
        override val navIconResId = R.drawable.apps_rounded_24px
        override val descriptionResourceId = R.string.app_list
    }

    @Serializable
    object InstalledApps : Route, NavBarItem {
        override val navIconResId = R.drawable.download_rounded_24px
        override val descriptionResourceId = R.string.installed
    }

    @Serializable
    object UpdatableApps : Route, NavBarItem {
        override val navIconResId = R.drawable.update_rounded_24px
        override val descriptionResourceId = R.string.app_updates
    }
}
