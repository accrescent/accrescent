package app.accrescent.client.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Update
import androidx.compose.ui.graphics.vector.ImageVector
import app.accrescent.client.R

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val navIcon: ImageVector?,
    val navIconSelected: ImageVector?,
) {
    object AppList : Screen("app_list", R.string.app_list, Icons.Outlined.Apps, Icons.Rounded.Apps)

    object InstalledApps : Screen(
        "installed_apps",
        R.string.installed,
        Icons.Outlined.Download,
        Icons.Rounded.Download,
    )

    object AppUpdates :
        Screen("app_updates", R.string.app_updates, Icons.Outlined.Update, Icons.Rounded.Update)

    object AppDetails : Screen("app_details", 0, null, null)

    object Settings : Screen("settings", R.string.settings, null, Icons.Rounded.Settings)
}
