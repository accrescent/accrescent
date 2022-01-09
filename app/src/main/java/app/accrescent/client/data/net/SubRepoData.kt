package app.accrescent.client.data.net

import kotlinx.serialization.Serializable

@Serializable
data class SubRepoData(val timestamp: Long, val apps: Map<String, App>)
