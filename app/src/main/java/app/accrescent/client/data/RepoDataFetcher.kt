package app.accrescent.client.data

import app.accrescent.client.data.net.AppRepoData
import app.accrescent.client.data.net.RepoData

interface RepoDataFetcher {
    fun fetchRepoData(): RepoData
    fun fetchAppRepoData(appId: String): AppRepoData
}
