package app.accrescent.client.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.Accrescent
import app.accrescent.client.data.AppInstallStatuses
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.getPackageInstallStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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
    @ApplicationContext context: Context,
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager,
    appInstallStatuses: AppInstallStatuses,
) : AndroidViewModel(context as Application) {
    // Initialize install status for apps as they're added
    val apps = repoDataRepository.getApps().onEach { apps ->
        for (app in apps) {
            val versionCode = repoDataRepository.getAppRepoData(app.id).versionCode
            appInstallStatuses.statuses[app.id] = context
                .packageManager
                .getPackageInstallStatus(app.id, versionCode)
        }
    }
    val installStatuses = appInstallStatuses.statuses
    var isRefreshing by mutableStateOf(false)
        private set
    var error: String? by mutableStateOf(null)

    fun refreshRepoData() {
        viewModelScope.launch {
            error = null
            isRefreshing = true

            try {
                repoDataRepository.fetchRepoData()
            } catch (e: ConnectException) {
                error = "Network error: ${e.message}"
            } catch (e: FileNotFoundException) {
                error = "Failed to download repodata: ${e.message}"
            } catch (e: GeneralSecurityException) {
                error = "Failed to verify repodata: ${e.message}"
            } catch (e: SerializationException) {
                error = "Failed to decode repodata: ${e.message}"
            } catch (e: UnknownHostException) {
                error = "Unknown host error: ${e.message}"
            }

            isRefreshing = false
        }
    }

    fun refreshInstallStatuses() {
        viewModelScope.launch {
            apps.collect()
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
                error = "Failed to download necessary files: ${e.message}"
            } catch (e: GeneralSecurityException) {
                error = "App verification failed: ${e.message}"
            } catch (e: InvalidObjectException) {
                error = "Error parsing app files: ${e.message}"
            } catch (e: NoSuchElementException) {
                error = "App does not support your device: ${e.message}"
            } catch (e: SerializationException) {
                error = "Failed to decode repodata: ${e.message}"
            } catch (e: UnknownHostException) {
                error = "Unknown host error: ${e.message}"
            }
        }
    }

    fun openApp(appId: String) {
        error = null

        val context = getApplication<Accrescent>().applicationContext

        val intent = context.packageManager.getLaunchIntentForPackage(appId)
        if (intent == null) {
            error = "Could not open app"
            return
        } else {
            context.startActivity(intent)
        }
    }
}
