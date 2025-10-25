// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

import build.buf.gen.accrescent.appstore.v1.SplitDownloadInfo as SplitDownloadInfoProto
import build.buf.gen.accrescent.appstore.v1.SplitUpdateInfo as SplitUpdateInfoProto

data class SplitDownloadInfo(val apkUrl: String, val size: Long) {
    companion object {
        fun from(value: SplitDownloadInfoProto): SplitDownloadInfo {
            return SplitDownloadInfo(apkUrl = value.url, size = value.downloadSize.toLong())
        }

        fun from(value: SplitUpdateInfoProto): SplitDownloadInfo {
            return SplitDownloadInfo(apkUrl = value.apkUrl, size = value.apkDownloadSize.toLong())
        }
    }
}
