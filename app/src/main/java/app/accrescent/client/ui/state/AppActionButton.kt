package app.accrescent.client.ui.state

sealed class AppActionButton(val enabled: Boolean) {
    class Install(enabled: Boolean) : AppActionButton(enabled)
    class Update(enabled: Boolean) : AppActionButton(enabled)
    class Open(enabled: Boolean) : AppActionButton(enabled)
    class Unarchive(enabled: Boolean) : AppActionButton(enabled)
    class Enable(enabled: Boolean) : AppActionButton(enabled)
    class Uninstall(enabled: Boolean) : AppActionButton(enabled)
}
