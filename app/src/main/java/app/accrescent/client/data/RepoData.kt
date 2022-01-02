package app.accrescent.client.data

import kotlinx.serialization.Serializable

typealias DeveloperId = UInt

@Serializable
data class RepoData(
    val timestamp: Long,
    val apps: Map<String, DeveloperId>,
    val developers: Map<DeveloperId, Developer>,
)
