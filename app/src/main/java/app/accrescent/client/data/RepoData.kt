package app.accrescent.client.data

import kotlinx.serialization.Serializable

@Serializable
data class RepoData(val timestamp: Long, val developers: List<Developer>)
