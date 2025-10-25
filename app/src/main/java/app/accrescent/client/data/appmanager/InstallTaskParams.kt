// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

sealed class InstallTaskParams(val appId: String) {
    class InitialInstall(appId: String) : InstallTaskParams(appId)
    class Unarchive(appId: String, val unarchiveId: Int) : InstallTaskParams(appId)
    class Update(appId: String, val currentVersionCode: Long) : InstallTaskParams(appId)
}
