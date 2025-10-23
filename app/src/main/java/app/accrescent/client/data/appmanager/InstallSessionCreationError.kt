package app.accrescent.client.data.appmanager

sealed class InstallSessionCreationError {
    data object InstallationServicesUnavailable : InstallSessionCreationError()
    data object ParametersUnsatisfiable : InstallSessionCreationError()
    data object SessionParamsInvalid : InstallSessionCreationError()
}
