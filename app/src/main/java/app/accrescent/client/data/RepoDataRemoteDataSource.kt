package app.accrescent.client.data

import app.accrescent.client.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepoDataRemoteDataSource @Inject constructor(
    private val repoDataFetcher: RepoDataFetcher,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend fun fetchRepoData() = withContext(dispatcher) { repoDataFetcher.fetchRepoData() }

    suspend fun fetchSubRepoData(developer: Developer) =
        withContext(dispatcher) { repoDataFetcher.fetchSubRepoData(developer) }
}
