package app.accrescent.client.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Update
import androidx.compose.ui.graphics.vector.ImageVector
import app.accrescent.client.R

sealed class Screen(val route: String, @StringRes val resourceId: Int, val navIcon: ImageVector) {
    object AppList : Screen("app_list", R.string.app_list, Icons.Rounded.Menu)
    object AppDetails : Screen("app_details", 0, Icons.Rounded.Info)
    object AppUpdates : Screen("app_updates", R.string.app_updates, Icons.Rounded.Update)
}
