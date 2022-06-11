package app.accrescent.client.data

import androidx.compose.runtime.mutableStateMapOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInstallStatuses @Inject constructor() {
    val statuses = mutableStateMapOf<String, InstallStatus>()
}
