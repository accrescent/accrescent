package net.lberrymage.accrescent.data

import javax.inject.Inject

class DeveloperRepository @Inject constructor(private val developersRemoteDataSource: DevelopersRemoteDataSource) {
    suspend fun fetchLatestDevelopers(): Developer = developersRemoteDataSource.fetchLatestDevelopers()
}
