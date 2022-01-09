package app.accrescent.client.data

import app.accrescent.client.data.net.RepoData
import app.accrescent.client.data.net.SubRepoData

interface RepoDataFetcher {
    fun fetchRepoData(): RepoData
    fun fetchSubRepoData(developer: Developer): SubRepoData
}
