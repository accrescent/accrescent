// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

sealed class InstallSessionCreationError {
    data object InstallationServicesUnavailable : InstallSessionCreationError()
    data object ParametersUnsatisfiable : InstallSessionCreationError()
    data object SessionParamsInvalid : InstallSessionCreationError()
}
