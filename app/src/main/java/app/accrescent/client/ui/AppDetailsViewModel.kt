package app.accrescent.client.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.data.RepoDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    private val repoDataRepository: RepoDataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val appId = savedStateHandle.get<String>(EXTRA_APPID)!!
    var uiState by mutableStateOf(AppDetailsUiState(appId = appId))
        private set

    init {
        viewModelScope.launch {
            uiState = uiState.copy(isFetchingData = true)

            val trustedInfo = repoDataRepository.getApp(appId)
            if (trustedInfo == null) {
                uiState = uiState.copy(appExists = false, isFetchingData = false)
                return@launch
            } else {
                uiState = uiState.copy(appName = trustedInfo.name)
            }
            val untrustedInfo = repoDataRepository.getAppRepoData(appId)
            uiState = uiState.copy(
                versionName = untrustedInfo.version,
                versionCode = untrustedInfo.versionCode,
            )

            uiState = uiState.copy(isFetchingData = false)
        }
    }
}
