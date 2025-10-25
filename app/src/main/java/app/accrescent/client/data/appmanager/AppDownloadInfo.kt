// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

import build.buf.gen.accrescent.appstore.v1.AppDownloadInfo as AppDownloadInfoProto
import build.buf.gen.accrescent.appstore.v1.AppUpdateInfo as AppUpdateInfoProto

data class AppDownloadInfo(val splitDownloadInfo: List<SplitDownloadInfo>) {
    companion object {
        fun from(value: AppDownloadInfoProto): AppDownloadInfo {
            return AppDownloadInfo(value.splitDownloadInfoList.map { SplitDownloadInfo.from(it) })
        }

        fun from(value: AppUpdateInfoProto): AppDownloadInfo {
            return AppDownloadInfo(value.splitUpdateInfoList.map { SplitDownloadInfo.from(it) })
        }
    }
}
