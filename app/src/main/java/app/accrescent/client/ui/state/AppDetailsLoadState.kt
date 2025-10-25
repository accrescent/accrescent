// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

sealed class AppDetailsLoadState {
    data object Loading : AppDetailsLoadState()
    data class Loaded(val appDetails: AppDetails) : AppDetailsLoadState()

    sealed class Error : AppDetailsLoadState() {
        data class AppNotFound(val appId: String) : Error()
        data object Internal : Error()
        data object Network : Error()
        data object Timeout : Error()
        data object Unknown : Error()
    }
}
