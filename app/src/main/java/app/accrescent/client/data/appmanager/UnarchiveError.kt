package app.accrescent.client.data.appmanager

sealed class UnarchiveError {
    data object PackageOrInstallerNotFound : UnarchiveError()
    data object ParametersUnsatisfiable : UnarchiveError()
}
