// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data

import app.accrescent.client.di.IoDispatcher
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RepoDataRemoteDataSource @Inject constructor(
    private val repoDataFetcher: RepoDataFetcher,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend fun fetchRepoData() = withContext(dispatcher) { repoDataFetcher.fetchRepoData() }
}
