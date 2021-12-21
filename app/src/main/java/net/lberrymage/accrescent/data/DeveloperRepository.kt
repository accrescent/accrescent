package net.lberrymage.accrescent.data

import javax.inject.Inject

class DeveloperRepository @Inject constructor(
    private val developersRemoteDataSource: DevelopersRemoteDataSource,
    private val developersLocalDataSource: DevelopersLocalDataSource,
) {
    suspend fun fetchLatestDevelopers() {
        val developers = developersRemoteDataSource.fetchLatestDevelopers()
        developersLocalDataSource.saveDevelopers(*developers.toTypedArray())
        developersLocalDataSource.deleteRemovedDevelopers(developers.map { it.username })
    }

    fun getPublicKey(username: String) = developersLocalDataSource.getPublicKey(username)
}
