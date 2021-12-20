package net.lberrymage.accrescent.data

interface DevelopersFetcher {
    fun fetchLatestDevelopers(): List<Developer>
}