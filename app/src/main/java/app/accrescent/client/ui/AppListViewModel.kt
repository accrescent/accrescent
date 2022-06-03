package app.accrescent.client.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import java.io.InvalidObjectException
import java.net.ConnectException
import java.net.UnknownHostException
import java.security.GeneralSecurityException
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager
) : ViewModel() {
    val apps = repoDataRepository.getApps()
    var isRefreshing by mutableStateOf(false)
        private set
    var error: String? by mutableStateOf(null)
        private set

    fun refreshRepoData() {
        viewModelScope.launch {
            error = null
            isRefreshing = true

            try {
                repoDataRepository.fetchRepoData()
            } catch (e: ConnectException) {
                error = "Network error: ${e.message}"
            } catch (e: FileNotFoundException) {
                error = "Failed to download repodata"
            } catch (e: GeneralSecurityException) {
                error = "Failed to verify repodata"
            } catch (e: SerializationException) {
                error = "Failed to decode repodata"
            } catch (e: UnknownHostException) {
                error = "Unknown host error: ${e.message}"
            }

            isRefreshing = false
        }
    }

    fun installApp(appId: String) {
        viewModelScope.launch {
            error = null

            try {
                packageManager.downloadAndInstall(appId)
            } catch (e: ConnectException) {
                error = "Network error: ${e.message}"
            } catch (e: FileNotFoundException) {
                error = "Failed to download necessary files"
            } catch (e: GeneralSecurityException) {
                error = "App verification failed: ${e.message}"
            } catch (e: InvalidObjectException) {
                error = "Error parsing app files: ${e.message}"
            } catch (e: NoSuchElementException) {
                error = "App does not support your device"
            } catch (e: SerializationException) {
                error = "Failed to decode repodata"
            } catch (e: UnknownHostException) {
                error = "Unknown host error: ${e.message}"
            }
        }
    }
}
