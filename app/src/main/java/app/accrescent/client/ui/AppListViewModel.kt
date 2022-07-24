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
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.UserRestrictionException
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
    val apps = repoDataRepository.getApps()

    // Initialize install status for apps as they're added
    init {
        val flow = apps.onEach { apps ->
            for (app in apps) {
                try {
                    val versionCode = repoDataRepository.getAppRepoData(app.id).versionCode
                    appInstallStatuses.statuses[app.id] = context
                        .packageManager
                        .getPackageInstallStatus(app.id, versionCode)
                } catch (e: Exception) {
                    appInstallStatuses.statuses[app.id] = InstallStatus.UNKNOWN
                }
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

    fun refreshInstallStatuses() {
        viewModelScope.launch {
            apps.collect()
        }
    }

    fun installApp(appId: String) {
        viewModelScope.launch {
            error = null

            val context = getApplication<Accrescent>().applicationContext

            try {
                packageManager.downloadAndInstall(appId)
            } catch (e: ConnectException) {
                error = context.getString(R.string.network_error, e.message)
            } catch (e: FileNotFoundException) {
                error = context.getString(R.string.failed_download_files, e.message)
            } catch (e: GeneralSecurityException) {
                error = context.getString(R.string.app_verification_failed, e.message)
            } catch (e: UserRestrictionException) {
                error = context.getString(R.string.user_restriction, e.message)
            } catch (e: InvalidObjectException) {
                error = context.getString(R.string.error_parsing_files, e.message)
            } catch (e: NoSuchElementException) {
                error = context.getString(R.string.app_doesnt_support_device, e.message)
            } catch (e: SerializationException) {
                error = context.getString(R.string.failed_decode_repodata, e.message)
            } catch (e: UnknownHostException) {
                error = context.getString(R.string.unknown_host_error, e.message)
            }
        }
    }

    fun uninstallApp(appId: String) {
        error = null

        val context = getApplication<Accrescent>().applicationContext

        try {
            packageManager.uninstallApp(appId)
        } catch (e: UserRestrictionException) {
            error = context.getString(R.string.user_restriction, e.message)
        }
    }

    fun openApp(appId: String) {
        error = null

        val context = getApplication<Accrescent>().applicationContext

        val intent = context.packageManager.getLaunchIntentForPackage(appId)
        if (intent == null) {
            error = context.getString(R.string.couldnt_open_app)
            return
        } else {
            context.startActivity(intent)
        }
    }
}
