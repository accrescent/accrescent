package app.accrescent.client.ui.state

import app.accrescent.client.data.appmanager.InstallSessionResult

sealed class InstallTaskState {
    data object DownloadEnqueued : InstallTaskState()
    data class DownloadRunning(val progress: DownloadProgress) : InstallTaskState()
    sealed class DownloadError : InstallTaskState() {
        data object ApkDownloadError : DownloadError()
        data object AppIncompatible : DownloadError()
        data object AppNotFound : DownloadError()
        data object AppStoreUnreachable : DownloadError()
        data object Canceled : DownloadError()
        data object FileOpenFailed : DownloadError()
        data object Internal : DownloadError()
        data object InstallationRequirementsNotMet : DownloadError()
        data object InstallationUnavailable : DownloadError()
        data object InvalidServerResponse : DownloadError()
        data object IoError : DownloadError()
        data object NotAlreadyInstalled : DownloadError()
        data object PackageVerificationFailed : DownloadError()
        data object Timeout : DownloadError()
        data object Unknown : DownloadError()
    }

    data object Installing : InstallTaskState()
    data class Completed(val result: InstallSessionResult) : InstallTaskState()
    data object NotRunning : InstallTaskState()
}
