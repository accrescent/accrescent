package app.accrescent.client.data.appmanager

sealed class OpenSessionReadError {
    data object SessionCommittedOrAbandoned : OpenSessionReadError()
    data object IoError : OpenSessionReadError()
}
