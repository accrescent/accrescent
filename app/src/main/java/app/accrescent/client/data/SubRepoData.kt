package app.accrescent.client.data

import app.accrescent.client.data.net.App
import kotlinx.serialization.Serializable

@Serializable
data class SubRepoData(val timestamp: Long, val apps: Map<String, App>)
