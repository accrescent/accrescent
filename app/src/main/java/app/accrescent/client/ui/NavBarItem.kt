package app.accrescent.client.ui

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface NavBarItem {
    val navIcon: ImageVector
    val navIconSelected: ImageVector
    val descriptionResourceId: Int
}
