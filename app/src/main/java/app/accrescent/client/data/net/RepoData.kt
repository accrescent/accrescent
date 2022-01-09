package app.accrescent.client.data.net

import app.accrescent.client.data.Developer
import kotlinx.serialization.Serializable

typealias DeveloperId = UInt

@Serializable
data class RepoData(
    val timestamp: Long,
    val apps: Map<String, DeveloperId>,
    val developers: Map<DeveloperId, Developer>,
)
