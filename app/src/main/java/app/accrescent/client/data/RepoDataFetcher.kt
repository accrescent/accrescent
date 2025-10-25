// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data

import app.accrescent.client.data.net.RepoData

interface RepoDataFetcher {
    fun fetchRepoData(): RepoData
}
