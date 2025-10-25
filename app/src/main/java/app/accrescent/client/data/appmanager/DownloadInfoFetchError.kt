// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

import io.grpc.Status
import io.grpc.StatusException

sealed class DownloadInfoFetchError {
    data object AppIncompatible : DownloadInfoFetchError()
    data object AppNotFound : DownloadInfoFetchError()
    data object RequestCanceled : DownloadInfoFetchError()
    data object RequestTimeout : DownloadInfoFetchError()
    data object ServiceUnavailable : DownloadInfoFetchError()
    data object UnrecognizedResponse : DownloadInfoFetchError()

    companion object {
        fun from(value: StatusException): DownloadInfoFetchError {
            return when (value.status.code) {
                Status.Code.CANCELLED -> RequestCanceled
                Status.Code.DEADLINE_EXCEEDED -> RequestTimeout
                Status.Code.NOT_FOUND -> AppNotFound
                Status.Code.FAILED_PRECONDITION -> AppIncompatible
                Status.Code.UNAVAILABLE -> ServiceUnavailable
                else -> UnrecognizedResponse
            }
        }
    }
}
