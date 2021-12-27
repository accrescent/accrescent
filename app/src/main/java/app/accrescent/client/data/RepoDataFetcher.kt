package app.accrescent.client.data

interface RepoDataFetcher {
    fun fetchLatestRepoData(): RepoData
}
