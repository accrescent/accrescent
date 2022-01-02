package app.accrescent.client.data.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class App(@SerialName("version_code") val versionCode: Int, val packages: List<Package>)
