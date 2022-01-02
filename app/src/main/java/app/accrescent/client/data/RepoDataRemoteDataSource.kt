package app.accrescent.client.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepoDataRemoteDataSource @Inject constructor(private val repoDataFetcher: RepoDataFetcher) {
    suspend fun fetchLatestRepoData() =
        withContext(Dispatchers.IO) { repoDataFetcher.fetchLatestRepoData() }

    suspend fun fetchSubRepoData(developer: Developer) =
        withContext(Dispatchers.IO) { repoDataFetcher.fetchSubRepoData(developer) }
}
