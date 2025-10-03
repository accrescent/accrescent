package app.accrescent.client.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Update
import app.accrescent.client.R
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    class AppDetails(val appId: String) : Route

    @Serializable
    object Settings : Route {
        val navIcon = Icons.Rounded.Settings
        val descriptionResourceId = R.string.settings
    }

    @Serializable
    object AllApps : Route, NavBarItem {
        override val navIcon = Icons.Outlined.Apps
        override val navIconSelected = Icons.Rounded.Apps
        override val descriptionResourceId = R.string.app_list
    }

    @Serializable
    object InstalledApps : Route, NavBarItem {
        override val navIcon = Icons.Outlined.Download
        override val navIconSelected = Icons.Rounded.Download
        override val descriptionResourceId = R.string.installed
    }

    @Serializable
    object UpdatableApps : Route, NavBarItem {
        override val navIcon = Icons.Outlined.Update
        override val navIconSelected = Icons.Rounded.Update
        override val descriptionResourceId = R.string.app_updates
    }
}
