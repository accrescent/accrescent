package app.accrescent.client.data

import androidx.compose.runtime.mutableStateMapOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AppInstallStatuses @Inject constructor() {
    val statuses = mutableStateMapOf<String, InstallStatus>()
    val downloadProgresses = mutableStateMapOf<String, DownloadProgress>()
}
