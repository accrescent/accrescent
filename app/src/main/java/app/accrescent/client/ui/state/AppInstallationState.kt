// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

sealed class AppInstallationState {
    data class NotInstalled(
        val compatible: Boolean,
        val archived: Boolean,
    ) : AppInstallationState()

    sealed class Installed(val enabled: Boolean) : AppInstallationState() {
        class UpToDate(enabled: Boolean) : Installed(enabled)
        class UpdateAvailable(enabled: Boolean, val compatible: Boolean) : Installed(enabled)
    }
}
