package app.accrescent.client.data.appmanager

sealed class PackageVerificationError {
    data object IoError : PackageVerificationError()
    data object MinimumVersionNotMet : PackageVerificationError()
    data object MultipleSigners : PackageVerificationError()
    data object NotSignedByRequiredSigner : PackageVerificationError()
    data class OpenSessionRead(val source: OpenSessionReadError) : PackageVerificationError()
    data object PackageParsingFailed : PackageVerificationError()
    data object SigningInfoNotPresent : PackageVerificationError()
}
