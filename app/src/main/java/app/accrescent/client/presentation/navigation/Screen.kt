package app.accrescent.client.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Update
import androidx.compose.ui.graphics.vector.ImageVector
import app.accrescent.client.R
import kotlinx.serialization.Serializable

sealed class Screen {

    @Serializable
    data object AppList : Screen()

    @Serializable
    data object InstalledApps : Screen()

    @Serializable
    data object AppUpdates : Screen()

    @Serializable
    data class AppDetails(val appId: String) : Screen()

    @Serializable
    data object Settings : Screen()
}

enum class TopLevelScreen(
    @StringRes val title: Int,
    val navIcon: ImageVector,
    val navIconSelected: ImageVector,
    val route: Screen
) {
    Apps(
        R.string.app_list,
        Icons.Outlined.Apps,
        Icons.Rounded.Apps,
        Screen.AppList
    ),
    InstalledApps(
        R.string.installed,
        Icons.Outlined.Download,
        Icons.Rounded.Download,
        Screen.InstalledApps
    ),
    AppUpdates(
        R.string.app_updates,
        Icons.Outlined.Update,
        Icons.Rounded.Update,
        Screen.AppUpdates
    ),
}