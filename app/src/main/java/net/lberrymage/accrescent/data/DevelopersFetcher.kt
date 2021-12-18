package net.lberrymage.accrescent.data

interface DevelopersFetcher {
    suspend fun fetchLatestDevelopers(): Developer
}