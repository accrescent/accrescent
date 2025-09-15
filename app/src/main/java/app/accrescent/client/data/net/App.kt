package app.accrescent.client.data.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class App(
    @SerialName("min_version_code") val minVersionCode: Int,
    @SerialName("signing_cert_hashes") val signingCertHashes: List<String>,
)
