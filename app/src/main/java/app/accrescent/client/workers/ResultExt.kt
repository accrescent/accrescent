package app.accrescent.client.workers

import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import app.accrescent.client.data.appmanager.ApkDownloadError
import app.accrescent.client.data.appmanager.DownloadInfoFetchError
import app.accrescent.client.data.appmanager.InstallSessionCreationError
import app.accrescent.client.data.appmanager.InstallSessionOpenError
import app.accrescent.client.data.appmanager.InstallTaskError
import app.accrescent.client.data.appmanager.OpenSessionReadError
import app.accrescent.client.data.appmanager.OpenSessionWriteError
import app.accrescent.client.data.appmanager.PackageVerificationError

object ResultExt {
    fun from(value: InstallTaskError): Result {
        val errorType = when (value) {
            is InstallTaskError.ApkDownload -> when (value.source) {
                ApkDownloadError.InvalidApkUrl -> ErrorType.INVALID_APK_URL
                ApkDownloadError.IoException -> ErrorType.INPUT_OUTPUT
                is ApkDownloadError.OpenSessionWrite -> when (value.source.source) {
                    OpenSessionWriteError.FileOpenFailed -> ErrorType.FILE_OPEN_FAILED
                    OpenSessionWriteError.SessionSealedOrAbandoned ->
                        ErrorType.SESSION_SEALED_OR_ABANDONED
                }

                ApkDownloadError.UnsuccessfulResponseCode -> ErrorType.UNSUCCESSFUL_RESPONSE_CODE
            }

            is InstallTaskError.DownloadInfoFetch -> when (value.source) {
                DownloadInfoFetchError.AppIncompatible -> ErrorType.APP_INCOMPATIBLE
                DownloadInfoFetchError.AppNotFound -> ErrorType.APP_NOT_FOUND
                DownloadInfoFetchError.RequestCanceled -> ErrorType.REQUEST_CANCELED
                DownloadInfoFetchError.RequestTimeout -> ErrorType.REQUEST_TIMEOUT
                DownloadInfoFetchError.ServiceUnavailable -> ErrorType.APP_STORE_SERVICE_UNAVAILABLE
                DownloadInfoFetchError.UnrecognizedResponse ->
                    ErrorType.UNRECOGNIZED_SERVER_RESPONSE
            }

            is InstallTaskError.InstallSessionCreation -> when (value.source) {
                InstallSessionCreationError.InstallationServicesUnavailable ->
                    ErrorType.INSTALLATION_SERVICES_UNAVAILABLE

                InstallSessionCreationError.ParametersUnsatisfiable ->
                    ErrorType.INSTALL_SESSION_PARAMS_UNSATISFIABLE

                InstallSessionCreationError.SessionParamsInvalid ->
                    ErrorType.INSTALL_SESSION_PARAMS_INVALID
            }

            InstallTaskError.NoMinVersionCode -> ErrorType.NO_MIN_VERSION_CODE
            InstallTaskError.NoSignerInfo -> ErrorType.NO_SIGNER_INFO
            is InstallTaskError.InstallSessionOpen -> when (value.source) {
                InstallSessionOpenError.ParametersUnsatisfiable ->
                    ErrorType.INSTALL_SESSION_PARAMS_UNSATISFIABLE

                InstallSessionOpenError.SessionInvalidOrNotOwned ->
                    ErrorType.INSTALL_SESSION_INVALID_OR_NOT_OWNED
            }

            is InstallTaskError.PackageVerification -> when (value.source) {
                PackageVerificationError.IoError -> ErrorType.INPUT_OUTPUT
                PackageVerificationError.MinimumVersionNotMet -> ErrorType.MINIMUM_VERSION_NOT_MET
                PackageVerificationError.MultipleSigners -> ErrorType.APP_HAS_MULTIPLE_SIGNERS
                PackageVerificationError.NotSignedByRequiredSigner ->
                    ErrorType.NOT_SIGNED_BY_REQUIRED_SIGNER

                is PackageVerificationError.OpenSessionRead -> when (value.source.source) {
                    OpenSessionReadError.IoError -> ErrorType.INPUT_OUTPUT
                    OpenSessionReadError.SessionCommittedOrAbandoned ->
                        ErrorType.SESSION_COMMITTED_OR_ABANDONED
                }

                PackageVerificationError.PackageParsingFailed -> ErrorType.PACKAGE_PARSING_FAILED
                PackageVerificationError.SigningInfoNotPresent -> ErrorType.SIGNING_INFO_NOT_PRESENT
            }
        }

        return Result.failure(workDataOf(DataKey.ERROR_TYPE to errorType))
    }
}
