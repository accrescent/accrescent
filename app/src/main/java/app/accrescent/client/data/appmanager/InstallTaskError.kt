package app.accrescent.client.data.appmanager

sealed class InstallTaskError {
    data class ApkDownload(val source: ApkDownloadError) : InstallTaskError()
    data class DownloadInfoFetch(val source: DownloadInfoFetchError) : InstallTaskError()
    data class InstallSessionCreation(val source: InstallSessionCreationError) : InstallTaskError()
    data class InstallSessionOpen(val source: InstallSessionOpenError) : InstallTaskError()
    data object NoMinVersionCode : InstallTaskError()
    data object NoSignerInfo : InstallTaskError()
    data class PackageVerification(val source: PackageVerificationError) : InstallTaskError()
}
