package app.accrescent.client.data.appmanager

sealed class OpenSessionWriteError {
    data object FileOpenFailed : OpenSessionWriteError()
    data object SessionSealedOrAbandoned : OpenSessionWriteError()
}
