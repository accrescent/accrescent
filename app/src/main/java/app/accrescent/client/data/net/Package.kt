package app.accrescent.client.data.net

import kotlinx.serialization.Serializable

@Serializable
data class Package(val file: String, val hash: String)
