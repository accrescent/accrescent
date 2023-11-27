package app.accrescent.client.data.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppRepoData(
    val version: String,
    @SerialName("version_code") val versionCode: Long,
    @SerialName("abi_splits") val abiSplits: List<String>,
    @SerialName("density_splits") val densitySplits: List<String>,
    @SerialName("lang_splits") val langSplits: List<String>,
    @SerialName("short_description") val shortDescription: String? = null,
)
