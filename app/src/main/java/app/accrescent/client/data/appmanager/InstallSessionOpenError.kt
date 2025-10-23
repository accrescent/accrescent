package app.accrescent.client.data.appmanager

sealed class InstallSessionOpenError {
    data object ParametersUnsatisfiable : InstallSessionOpenError()
    data object SessionInvalidOrNotOwned : InstallSessionOpenError()
}
