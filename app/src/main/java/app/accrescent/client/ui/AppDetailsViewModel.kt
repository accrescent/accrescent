package app.accrescent.client.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import java.io.InvalidObjectException
import java.net.ConnectException
import java.net.UnknownHostException
import java.security.GeneralSecurityException
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repoDataRepository: RepoDataRepository,
    appInstallStatuses: AppInstallStatuses,
    private val packageManager: PackageManager,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(context as Application) {
    private val appId = savedStateHandle.get<String>("appId")!!
    val installStatuses = appInstallStatuses.statuses
    var downloadProgresses = appInstallStatuses.downloadProgresses
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
                if (appInstallStatuses.statuses[appId] == null) {
                    appInstallStatuses.statuses[appId] =
                        try {
                            context
                                .packageManager
                                .getPackageInstallStatus(appId, untrustedInfo.versionCode)
                        } catch (e: Exception) {
                            InstallStatus.UNKNOWN
                        }
                }
                uiState.copy(
                    versionName = untrustedInfo.version,
                    versionCode = untrustedInfo.versionCode,
                    shortDescription = untrustedInfo.shortDescription
                        ?: context.getString(R.string.no_description_provided),
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

    fun installApp(appId: String) {
        viewModelScope.launch {
            uiState.error = null

            val context = getApplication<Accrescent>().applicationContext

            try {
                packageManager.downloadAndInstall(
                    appId = appId,
                    onProgressUpdate = { downloadProgresses[appId] = it },
                    onDownloadComplete = { downloadProgresses.remove(appId) },
                )
            } catch (e: ConnectException) {
                uiState.error = context.getString(R.string.network_error, e.message)
            } catch (e: FileNotFoundException) {
                uiState.error = context.getString(R.string.failed_download_files, e.message)
            } catch (e: GeneralSecurityException) {
                uiState.error = context.getString(R.string.app_verification_failed, e.message)
            } catch (e: UserRestrictionException) {
                uiState.error = context.getString(R.string.user_restriction, e.message)
            } catch (e: InvalidObjectException) {
                uiState.error = context.getString(R.string.error_parsing_files, e.message)
            } catch (e: NoSuchElementException) {
                uiState.error = context.getString(R.string.app_doesnt_support_device, e.message)
            } catch (e: SerializationException) {
                uiState.error = context.getString(R.string.failed_decode_repodata, e.message)
            } catch (e: UnknownHostException) {
                uiState.error = context.getString(R.string.unknown_host_error, e.message)
            }
        }
    }

    fun uninstallApp(appId: String) {
        uiState.error = null

        val context = getApplication<Accrescent>().applicationContext

        try {
            packageManager.uninstallApp(appId)
        } catch (e: UserRestrictionException) {
            uiState.error = context.getString(R.string.user_restriction, e.message)
        }
    }

    fun openApp(appId: String) {
        uiState.error = null

        val context = getApplication<Accrescent>().applicationContext

        val intent = context.packageManager.getLaunchIntentForPackage(appId)
        if (intent == null) {
            uiState.error = context.getString(R.string.couldnt_open_app)
            return
        } else {
            context.startActivity(intent)
        }
    }

    fun openAppInfo(appId: String) {
        uiState.error = null

        val context = getApplication<Accrescent>().applicationContext
        val uri = "package:$appId".toUri()

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent.resolveActivity(context.packageManager) == null) {
            uiState.error = context.getString(R.string.couldnt_open_appinfo)
            return
        } else {
            context.startActivity(intent)
        }
    }
}
