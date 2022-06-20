package app.accrescent.client.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.R
import app.accrescent.client.data.RepoDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repoDataRepository: RepoDataRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val appId = savedStateHandle.get<String>("appId")!!
    var uiState by mutableStateOf(AppDetailsUiState(appId = appId))
        private set

    init {
        viewModelScope.launch {
            uiState = uiState.copy(isFetchingData = true, error = null)

            val trustedInfo = repoDataRepository.getApp(appId)
            if (trustedInfo == null) {
                uiState = uiState.copy(appExists = false, isFetchingData = false)
                return@launch
            } else {
                uiState = uiState.copy(appName = trustedInfo.name)
            }

            uiState = try {
                val untrustedInfo = repoDataRepository.getAppRepoData(appId)
                uiState.copy(
                    versionName = untrustedInfo.version,
                    versionCode = untrustedInfo.versionCode,
                )
            } catch (e: ConnectException) {
                uiState.copy(
                    error = context.getString(R.string.network_error, e.message),
                    appExists = false,
                )
            } catch (e: FileNotFoundException) {
                uiState.copy(
                    error = context.getString(R.string.failed_download_repodata, e.message),
                    appExists = false,
                )
            } catch (e: SerializationException) {
                uiState.copy(
                    error = context.getString(R.string.failed_decode_repodata, e.message),
                    appExists = false,
                )
            } catch (e: UnknownHostException) {
                uiState.copy(
                    error = context.getString(R.string.unknown_host_error, e.message),
                    appExists = false,
                )
            }

            uiState = uiState.copy(isFetchingData = false)
        }
    }
}
