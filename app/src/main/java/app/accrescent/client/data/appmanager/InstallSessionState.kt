package app.accrescent.client.data.appmanager

sealed class InstallSessionState {
    data object InProgress : InstallSessionState()
    data class Completed(val sessionId: Int, val result: InstallSessionResult) : InstallSessionState()
}
