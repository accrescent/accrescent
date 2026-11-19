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
        class UpdateAvailable(
            enabled: Boolean,
            val compatible: Boolean,
            val source: UpdateSource,
        ) : Installed(enabled)
    }

    /**
     * Where updates for an installed app currently come from
     */
    enum class UpdateSource {
        /** Accrescent already updates this app */
        Accrescent,

        /**
         * Another installer updates this app, but Accrescent can take over updates because the
         * installed copy is signed by the same key as Accrescent's copy
         */
        OtherInstaller,

        /**
         * Another installer updates this app and its installed copy is signed by a different key
         * than Accrescent's copy, so Accrescent can't update it without uninstalling it first
         */
        OtherInstallerDifferentSigner,
    }
}
