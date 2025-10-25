// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

sealed class ApkDownloadError {
    data object InvalidApkUrl : ApkDownloadError()
    data object IoException : ApkDownloadError()
    data class OpenSessionWrite(val source: OpenSessionWriteError) : ApkDownloadError()
    data object UnsuccessfulResponseCode : ApkDownloadError()
}
