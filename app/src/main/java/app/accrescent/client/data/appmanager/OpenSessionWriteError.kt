// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

sealed class OpenSessionWriteError {
    data object FileOpenFailed : OpenSessionWriteError()
    data object SessionSealedOrAbandoned : OpenSessionWriteError()
}
