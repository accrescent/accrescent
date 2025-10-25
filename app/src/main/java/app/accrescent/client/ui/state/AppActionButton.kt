// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

sealed class AppActionButton {
    data object Install : AppActionButton()
    data object Update : AppActionButton()
    data object Open : AppActionButton()
    data object Unarchive : AppActionButton()
    data object Enable : AppActionButton()
    data object Uninstall : AppActionButton()
    data class Cancel(val enabled: Boolean) : AppActionButton()
}
