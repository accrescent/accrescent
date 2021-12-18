package net.lberrymage.accrescent.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DevelopersRemoteDataSource @Inject constructor(private val developersFetcher: DevelopersFetcher) {
    suspend fun fetchLatestDevelopers() =
        withContext(Dispatchers.IO) { developersFetcher.fetchLatestDevelopers() }
}
