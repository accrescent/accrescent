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
    suspend fun fetchAppRepoData(appId: String) = withContext(dispatcher) {
        repoDataFetcher.fetchAppRepoData(appId)
    }
}
