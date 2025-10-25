// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

sealed class UnarchiveError {
    data object PackageOrInstallerNotFound : UnarchiveError()
    data object ParametersUnsatisfiable : UnarchiveError()
}
