package app.accrescent.client.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import app.accrescent.client.data.DownloadProgress

data class AppDetailsUiState(
    val isFetchingData: Boolean = false,
    var error: String? = null,
    val appExists: Boolean = true,
    val appId: String,
    val appName: String = "",
    val versionName: String = "",
    val versionCode: Long = 0,
    var downloadProgress: MutableState<DownloadProgress?> = mutableStateOf(null),
)
