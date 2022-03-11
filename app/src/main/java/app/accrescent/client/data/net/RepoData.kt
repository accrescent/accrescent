package app.accrescent.client.data.net

import kotlinx.serialization.Serializable

@Serializable
data class RepoData(
    val timestamp: Long,
    val apps: Map<String, App>,
)
