package app.accrescent.client.data

enum class InstallStatus {
    INSTALLABLE,
    UPDATABLE,
    DISABLED,
    INSTALLED,
    INSTALLED_FROM_ANOTHER_SOURCE,
    LOADING,
    UNKNOWN,
}
