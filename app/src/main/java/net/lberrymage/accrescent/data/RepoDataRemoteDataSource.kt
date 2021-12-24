package net.lberrymage.accrescent.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepoDataRemoteDataSource @Inject constructor(private val repoDataFetcher: RepoDataFetcher) {
    suspend fun fetchLatestRepoData() =
        withContext(Dispatchers.IO) { repoDataFetcher.fetchLatestRepoData() }
}
