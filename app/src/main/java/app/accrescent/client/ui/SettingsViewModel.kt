package app.accrescent.client.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.NetworkType
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.workers.AutoUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesManager: PreferencesManager) :
    ViewModel() {
    val dynamicColor = preferencesManager.dynamicColor
    val requireUserAction = preferencesManager.requireUserAction
    var automaticUpdates = preferencesManager.automaticUpdates
    val updaterNetworkType = preferencesManager.networkType

    suspend fun setDynamicColor(dynamicColor: Boolean) =
        preferencesManager.setDynamicColor(dynamicColor)

    suspend fun setRequireUserAction(requireUserAction: Boolean) =
        preferencesManager.setRequireUserAction(requireUserAction)

    suspend fun setUpdaterNetworkType(context: Context, networkType: NetworkType) {
        preferencesManager.setNetworkType(networkType.name)
        AutoUpdateWorker.enqueue(context, networkType, true)
    }

    suspend fun setAutomaticUpdates(automaticUpdates: Boolean) =
        preferencesManager.setAutomaticUpdates(automaticUpdates)
}
