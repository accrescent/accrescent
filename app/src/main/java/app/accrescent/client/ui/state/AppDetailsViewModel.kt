// SPDX-FileCopyrightText: © 2022 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.LocaleList
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.work.WorkInfo
import app.accrescent.client.data.appmanager.AppManager
import app.accrescent.client.data.appmanager.DeviceAttributesRepository
import app.accrescent.client.data.appmanager.InstallSessionRepository
import app.accrescent.client.data.appmanager.InstallSessionState
import app.accrescent.client.data.appmanager.InstallWorkRepository
import app.accrescent.client.ui.navigation.Route
import app.accrescent.client.workers.DataKey
import app.accrescent.client.workers.ErrorType
import app.accrescent.client.workers.WorkerTag
import build.buf.gen.accrescent.appstore.v1.AppServiceGrpcKt
import build.buf.gen.accrescent.appstore.v1.getAppListingRequest
import build.buf.gen.accrescent.appstore.v1.getAppPackageInfoRequest
import build.buf.gen.accrescent.appstore.v1.getAppUpdateInfoRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.Status
import io.grpc.StatusException
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val ERROR_TYPE_INVALID = -999

// Five seconds
private const val FLOW_SUBSCRIPTION_TIMEOUT_DELAY: Long = 5000
private const val LOG_TAG = "AppDetailsViewModel"

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appManager: AppManager,
    private val appService: AppServiceGrpcKt.AppServiceCoroutineStub,
    private val deviceAttributesRepository: DeviceAttributesRepository,
    private val installSessionRepository: InstallSessionRepository,
    private val installWorkRepository: InstallWorkRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val appId = savedStateHandle.toRoute<Route.AppDetails>().appId
    private val appDetailsLoadState = MutableStateFlow<AppDetailsLoadState>(AppDetailsLoadState.Loading)
    private val appInstallationState = MutableStateFlow<AppInstallationState>(
        AppInstallationState.NotInstalled(compatible = false, archived = false)
    )
    private val installWorkInfos = installWorkRepository.getInstallWorkInfosForAppId(appId)
    private val installSession = installSessionRepository.getSessionStateForApp(appId)
    private val packageEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedAppId = intent.data?.schemeSpecificPart ?: run {
                Log.e(LOG_TAG, "no package data for intent $intent")
                return
            }
            if (receivedAppId != appId) {
                // We're not interested in other apps
                return
            }

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_CHANGED,
                Intent.ACTION_PACKAGE_FULLY_REMOVED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED -> viewModelScope.launch {
                    appInstallationState.value = getAppInstallationState()
                }
            }
        }

    }

    val screenUiState = combine(
        appDetailsLoadState,
        installWorkInfos,
        installSession,
        appInstallationState,
    ) { appListingLoadState, installWorkInfos, installSession, installState ->
        mergeToUiState(appListingLoadState, installWorkInfos, installSession, installState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(FLOW_SUBSCRIPTION_TIMEOUT_DELAY),
        initialValue = AppDetailsUiState.Loading,
    )

    init {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            context,
            packageEventReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        loadData()
    }

    override fun onCleared() {
        context.unregisterReceiver(packageEventReceiver)
    }

    fun loadData() {
        appDetailsLoadState.value = AppDetailsLoadState.Loading

        val viewModelAppId = appId
        val getAppListingRequest = getAppListingRequest {
            appId = viewModelAppId
            preferredLanguages.addAll(getPreferredLanguages())
        }
        val getAppPackageInfoRequest = getAppPackageInfoRequest { appId = viewModelAppId }

        viewModelScope.launch {
            val (listing, packageInfo, installationState) = try {
                val listing = appService.getAppListing(getAppListingRequest).listing
                val packageInfo = appService
                    .getAppPackageInfo(getAppPackageInfoRequest)
                    .packageInfo
                val installationState = getAppInstallationState()

                Triple(listing, packageInfo, installationState)
            } catch (e: StatusException) {
                appDetailsLoadState.value = when (e.status.code) {
                    Status.Code.DEADLINE_EXCEEDED -> AppDetailsLoadState.Error.Timeout
                    Status.Code.NOT_FOUND -> AppDetailsLoadState.Error.AppNotFound(appId)
                    Status.Code.UNIMPLEMENTED,
                    Status.Code.INTERNAL -> {
                        Log.e(LOG_TAG, "internal status code received", e)
                        AppDetailsLoadState.Error.Internal
                    }

                    Status.Code.UNAVAILABLE -> AppDetailsLoadState.Error.Network
                    else -> {
                        Log.e(LOG_TAG, "unexpected status code received", e)
                        AppDetailsLoadState.Error.Unknown
                    }
                }
                return@launch
            }

            appDetailsLoadState.value = AppDetailsLoadState.Loaded(
                AppDetails(
                    appId = listing.appId,
                    name = listing.name,
                    version = packageInfo.versionName,
                    shortDescription = listing.shortDescription,
                    iconUrl = listing.icon.url,
                )
            )
            appInstallationState.value = installationState
        }
    }

    fun installApp() {
        installWorkRepository.enqueueInstallWorker(appId)
    }

    fun updateApp() {
        installWorkRepository.enqueueUpdateWorker(appId = appId, userInitiated = true)
    }

    fun openApp() {
        context.packageManager.getLaunchIntentForPackage(appId)?.let { context.startActivity(it) }
    }

    /**
     * Unarchives the app associated with the current [ViewModel] instance.
     *
     * This method is a no-op on Android versions below Android 15.
     */
    fun unarchiveApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            appManager.unarchive(appId)
        }
    }

    fun uninstallApp() {
        appManager.uninstall(appId)
    }

    fun cancelAppDownload() {
        installWorkRepository.cancelAppInstallWork(appId)
    }

    fun openAppDetailsSettings() {
        val uri = "package:$appId".toUri()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun clearInstallationResult() {
        installWorkRepository.clearAppInstallWork(appId)
        installSessionRepository.clearSessionsForApp(appId)
        installSessionRepository.clearSessionResultsForApp(appId)
    }

    private fun getPreferredLanguages(): List<String> {
        return LocaleList.getDefault().toLanguageTags().split(',')
    }

    private suspend fun getAppInstallationState(): AppInstallationState {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                context.packageManager.getPackageInfo(
                    appId,
                    PackageManager.PackageInfoFlags.of(PackageManager.MATCH_ARCHIVED_PACKAGES),
                )
            } else {
                context.packageManager.getPackageInfo(appId, 0)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        return if (packageInfo == null) {
            // Not installed and not archived
            AppInstallationState.NotInstalled(
                compatible = appManager.isAppCompatible(appId),
                archived = false,
            )
        } else {
            val archived = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                packageInfo.applicationInfo?.isArchived == true
            } else {
                false
            }

            if (archived) {
                AppInstallationState.NotInstalled(
                    compatible = appManager.isAppCompatible(appId),
                    archived = true,
                )
            } else {
                // Must be installed
                val (updateAvailable, updateCompatible) = try {
                    val viewModelAppId = appId
                    val request = getAppUpdateInfoRequest {
                        this.appId = viewModelAppId
                        deviceAttributes = deviceAttributesRepository.getDeviceAttributes()
                        baseVersionCode = packageInfo.longVersionCode
                    }
                    val updateAvailable = appService.getAppUpdateInfo(request).hasAppUpdateInfo()

                    Pair(updateAvailable, true)
                } catch (e: StatusException) {
                    when (e.status.code) {
                        Status.Code.FAILED_PRECONDITION -> Pair(false, false)
                        else -> throw e
                    }
                }

                if (updateAvailable) {
                    AppInstallationState.Installed.UpdateAvailable(
                        enabled = packageInfo.applicationInfo?.enabled == true,
                        compatible = updateCompatible,
                    )
                } else {
                    AppInstallationState.Installed.UpToDate(
                        enabled = packageInfo.applicationInfo?.enabled == true,
                    )
                }
            }
        }
    }

    private fun mergeToUiState(
        appDetailsLoadState: AppDetailsLoadState,
        installWorkInfos: List<WorkInfo>,
        installSessionState: InstallSessionState?,
        installationState: AppInstallationState,
    ): AppDetailsUiState = when (appDetailsLoadState) {
        is AppDetailsLoadState.Error -> AppDetailsUiState.LoadingError(appDetailsLoadState)
        is AppDetailsLoadState.Loaded -> {
            val workInfo = installWorkInfos.firstOrNull()
            // Order of checks:
            //
            // 1. If install result available, show result
            // 2. If user-initiated worker exists:
            //    a. If enqueued or blocked, show download enqueued
            //    b. If running:
            //        i. If progress not available, show download enqueued
            //        ii. If progress available, show download running w/progress
            //    c. If succeeded:
            //        i. If install session exists and is active, show installing
            //        ii. Else, show not running
            //    d. If failed, show download error
            //    e. If canceled, show not running
            // 3. Else show not running
            val installTaskState = when {
                installSessionState != null -> when (installSessionState) {
                    is InstallSessionState.Completed -> InstallTaskState.Completed(
                        installSessionState.result,
                    )

                    InstallSessionState.InProgress -> InstallTaskState.Installing
                }

                workInfo?.tags?.contains(WorkerTag.USER_INITIATED) == true -> when (workInfo.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.BLOCKED -> InstallTaskState.DownloadEnqueued

                    WorkInfo.State.RUNNING -> {
                        val bytesDownloaded = workInfo
                            .progress
                            .getLong(DataKey.BYTES_DOWNLOADED, -1)
                        val totalBytesToDownload = workInfo
                            .progress
                            .getLong(DataKey.TOTAL_BYTES_TO_DOWNLOAD, -1)
                        if (bytesDownloaded == -1L || totalBytesToDownload == -1L) {
                            InstallTaskState.DownloadEnqueued
                        } else {
                            InstallTaskState.DownloadRunning(
                                progress = DownloadProgress(
                                    downloadedBytes = bytesDownloaded,
                                    totalBytes = totalBytesToDownload,
                                )
                            )
                        }
                    }

                    WorkInfo.State.SUCCEEDED,
                    WorkInfo.State.CANCELLED -> InstallTaskState.NotRunning

                    WorkInfo.State.FAILED -> {
                        val errorCode = workInfo
                            .outputData
                            .getInt(DataKey.ERROR_TYPE, ERROR_TYPE_INVALID)
                        when (errorCode) {
                            ErrorType.INVALID_APK_URL,
                            ErrorType.UNRECOGNIZED_SERVER_RESPONSE ->
                                InstallTaskState.DownloadError.InvalidServerResponse

                            ErrorType.INPUT_OUTPUT -> InstallTaskState.DownloadError.IoError
                            ErrorType.FILE_OPEN_FAILED -> InstallTaskState.DownloadError.FileOpenFailed
                            ErrorType.SESSION_SEALED_OR_ABANDONED,
                            ErrorType.INSTALL_SESSION_PARAMS_INVALID,
                            ErrorType.INSTALL_SESSION_INVALID_OR_NOT_OWNED,
                            ErrorType.INTERNAL -> InstallTaskState.DownloadError.Internal

                            ErrorType.UNSUCCESSFUL_RESPONSE_CODE ->
                                InstallTaskState.DownloadError.ApkDownloadError

                            ErrorType.APP_INCOMPATIBLE ->
                                InstallTaskState.DownloadError.AppIncompatible

                            ErrorType.APP_NOT_FOUND -> InstallTaskState.DownloadError.AppNotFound
                            ErrorType.REQUEST_CANCELED -> InstallTaskState.DownloadError.Canceled
                            ErrorType.REQUEST_TIMEOUT -> InstallTaskState.DownloadError.Timeout
                            ErrorType.APP_STORE_SERVICE_UNAVAILABLE ->
                                InstallTaskState.DownloadError.AppStoreUnreachable

                            ErrorType.INSTALLATION_SERVICES_UNAVAILABLE ->
                                InstallTaskState.DownloadError.InstallationUnavailable

                            ErrorType.INSTALL_SESSION_PARAMS_UNSATISFIABLE ->
                                InstallTaskState.DownloadError.InstallationRequirementsNotMet

                            ErrorType.NOT_ALREADY_INSTALLED ->
                                InstallTaskState.DownloadError.NotAlreadyInstalled

                            ErrorType.NO_MIN_VERSION_CODE,
                            ErrorType.NO_SIGNER_INFO,
                            ErrorType.MINIMUM_VERSION_NOT_MET,
                            ErrorType.APP_HAS_MULTIPLE_SIGNERS,
                            ErrorType.NOT_SIGNED_BY_REQUIRED_SIGNER,
                            ErrorType.SESSION_COMMITTED_OR_ABANDONED,
                            ErrorType.PACKAGE_PARSING_FAILED,
                            ErrorType.SIGNING_INFO_NOT_PRESENT ->
                                InstallTaskState.DownloadError.PackageVerificationFailed

                            else -> InstallTaskState.DownloadError.Unknown
                        }
                    }
                }

                else -> InstallTaskState.NotRunning
            }

            AppDetailsUiState.Loaded(
                appDetails = appDetailsLoadState.appDetails,
                installTaskState = installTaskState,
                installationState = installationState,
            )
        }

        AppDetailsLoadState.Loading -> AppDetailsUiState.Loading
    }
}
