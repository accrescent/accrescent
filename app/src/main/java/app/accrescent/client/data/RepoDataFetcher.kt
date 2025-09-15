package app.accrescent.client.data

import app.accrescent.client.data.net.RepoData

interface RepoDataFetcher {
    fun fetchRepoData(): RepoData
}
