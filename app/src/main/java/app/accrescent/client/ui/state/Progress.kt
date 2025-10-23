package app.accrescent.client.ui.state

sealed class Progress {
    data object Indeterminate : Progress()
    data class Determinate(val progress: Float) : Progress()
}
