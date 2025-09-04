package app.accrescent.client.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.accrescent.client.Accrescent
import app.accrescent.client.R
import app.accrescent.client.data.AppInstallStatuses
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.getPackageInstallStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.UnknownHostException
import java.security.GeneralSecurityException

@HiltViewModel
class AppListViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repoDataRepository: RepoDataRepository,
    appInstallStatuses: AppInstallStatuses,
) : AndroidViewModel(context as Application) {
    val apps = repoDataRepository.getApps()

    // Initialize install status for apps as they're added
    init {
        val flow = apps.onEach { apps ->
            for (app in apps) {
                val latestVersionCode = try {
                    repoDataRepository.getAppRepoData(app.id).versionCode
                } catch (e: Exception) {
                    null
                }
                appInstallStatuses.statuses[app.id] =
                    context.packageManager.getPackageInstallStatus(app.id, latestVersionCode)
            }
        }
        viewModelScope.launch {
            flow.collect()
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

            val context = getApplication<Accrescent>().applicationContext

            try {
                repoDataRepository.fetchRepoData()
            } catch (e: ConnectException) {
                error = context.getString(R.string.network_error, e.message)
            } catch (e: FileNotFoundException) {
                error = context.getString(R.string.failed_download_repodata, e.message)
            } catch (e: GeneralSecurityException) {
                error = context.getString(R.string.failed_verify_repodata, e.message)
            } catch (e: SerializationException) {
                error = context.getString(R.string.failed_decode_repodata, e.message)
            } catch (e: UnknownHostException) {
                error = context.getString(R.string.unknown_host_error, e.message)
            }

            isRefreshing = false
        }
    }
}
