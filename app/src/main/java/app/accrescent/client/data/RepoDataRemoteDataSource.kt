package app.accrescent.client.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepoDataRemoteDataSource @Inject constructor(private val repoDataFetcher: RepoDataFetcher) {
    suspend fun fetchRepoData() =
        withContext(Dispatchers.IO) { repoDataFetcher.fetchRepoData() }

    suspend fun fetchSubRepoData(developer: Developer) =
        withContext(Dispatchers.IO) { repoDataFetcher.fetchSubRepoData(developer) }
}
