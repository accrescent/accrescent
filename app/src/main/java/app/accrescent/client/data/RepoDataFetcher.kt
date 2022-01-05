package app.accrescent.client.data

interface RepoDataFetcher {
    fun fetchRepoData(): RepoData
    fun fetchSubRepoData(developer: Developer): SubRepoData
}
