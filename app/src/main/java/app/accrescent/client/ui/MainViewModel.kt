package app.accrescent.client.ui

import androidx.compose.material.SnackbarHostState
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
class MainViewModel @Inject constructor(
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager
) : ViewModel() {
    val apps = repoDataRepository.getApps()
    val snackbarHostState = SnackbarHostState()
    var isRefreshing by mutableStateOf(false)
        private set

    fun refreshRepoData() {
        viewModelScope.launch {
            isRefreshing = true

            try {
                repoDataRepository.fetchRepoData()
            } catch (e: ConnectException) {
                snackbarHostState.showSnackbar("Network error: ${e.message}")
            } catch (e: FileNotFoundException) {
                snackbarHostState.showSnackbar("Failed to download repodata")
            } catch (e: GeneralSecurityException) {
                snackbarHostState.showSnackbar("Failed to verify repodata")
            } catch (e: SerializationException) {
                snackbarHostState.showSnackbar("Failed to decode repodata")
            } catch (e: UnknownHostException) {
                snackbarHostState.showSnackbar("Unknown host error: ${e.message}")
            }

            isRefreshing = false
        }
    }

    fun installApp(appId: String) {
        viewModelScope.launch {
            try {
                packageManager.downloadAndInstall(appId)
            } catch (e: ConnectException) {
                snackbarHostState.showSnackbar("Network error: ${e.message}")
            } catch (e: FileNotFoundException) {
                snackbarHostState.showSnackbar("Failed to download necessary files")
            } catch (e: GeneralSecurityException) {
                snackbarHostState.showSnackbar("App verification failed: ${e.message}")
            } catch (e: InvalidObjectException) {
                snackbarHostState.showSnackbar("Error parsing app files: ${e.message}")
            } catch (e: NoSuchElementException) {
                snackbarHostState.showSnackbar("App does not support your device")
            } catch (e: SerializationException) {
                snackbarHostState.showSnackbar("Failed to decode repodata")
            } catch (e: UnknownHostException) {
                snackbarHostState.showSnackbar("Unknown host error: ${e.message}")
            }
        }
    }
}
