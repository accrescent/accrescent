package app.accrescent.client.data.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class App(
    val name: String,
    @SerialName("min_version_code") val minVersionCode: Int,
    @SerialName("icon_hash") val iconHash: String,
    @SerialName("signing_key_hashes") val signingKeyHashes: List<String>,
)
