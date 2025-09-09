package app.accrescent.client.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.NetworkType
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.data.Theme
import app.accrescent.client.workers.AutoUpdateWorker
import app.accrescent.client.workers.RepositoryRefreshWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferencesManager: PreferencesManager) :
    ViewModel() {
    val dynamicColor = preferencesManager.dynamicColor
    val theme = preferencesManager.theme
    var automaticUpdates = preferencesManager.automaticUpdates
    val updaterNetworkType = preferencesManager.networkType

    suspend fun setDynamicColor(dynamicColor: Boolean) =
        preferencesManager.setDynamicColor(dynamicColor)

    suspend fun setTheme(theme: Theme) = preferencesManager.setTheme(theme)

    suspend fun setUpdaterNetworkType(context: Context, networkType: NetworkType) {
        preferencesManager.setNetworkType(networkType.name)
        RepositoryRefreshWorker.enqueue(context, networkType)
        AutoUpdateWorker.enqueue(context, networkType)
    }

    suspend fun setAutomaticUpdates(automaticUpdates: Boolean) =
        preferencesManager.setAutomaticUpdates(automaticUpdates)
}
