package app.accrescent.client.ui

import androidx.lifecycle.ViewModel
import app.accrescent.client.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesManager: PreferencesManager) :
    ViewModel() {
    val dynamicColor = preferencesManager.dynamicColor
    val requireUserAction = preferencesManager.requireUserAction

    suspend fun setDynamicColor(dynamicColor: Boolean) =
        preferencesManager.setDynamicColor(dynamicColor)

    suspend fun setRequireUserAction(requireUserAction: Boolean) =
        preferencesManager.setRequireUserAction(requireUserAction)
}
