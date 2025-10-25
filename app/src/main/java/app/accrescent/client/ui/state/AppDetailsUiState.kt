// SPDX-FileCopyrightText: © 2022 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

import android.content.Context
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.accrescent.client.BuildConfig
import app.accrescent.client.R
import app.accrescent.client.data.appmanager.InstallSessionResult
import java.text.NumberFormat

@Immutable
sealed class AppDetailsUiState {
    data class Loaded(
        val appDetails: AppDetails,
        val installTaskState: InstallTaskState,
        val installationState: AppInstallationState,
    ) : AppDetailsUiState() {
        val progressToShow: Progress? = when (installTaskState) {
            is InstallTaskState.Completed -> when (installTaskState.result) {
                InstallSessionResult.PendingUserAction -> Progress.Indeterminate
                else -> null
            }

            InstallTaskState.DownloadEnqueued -> Progress.Indeterminate
            is InstallTaskState.DownloadError -> null
            is InstallTaskState.DownloadRunning -> {
                val progress = installTaskState
                    .progress
                    .let { it.downloadedBytes.toFloat() / it.totalBytes }
                Progress.Determinate(progress)
            }

            InstallTaskState.Installing -> Progress.Indeterminate
            InstallTaskState.NotRunning -> null
        }

        val buttonsToShow: List<AppActionButton> = run {
            val whenNotCancelButtons = when (installationState) {
                is AppInstallationState.Installed.UpToDate -> when {
                    installationState.enabled && appDetails.appId == BuildConfig.APPLICATION_ID ->
                        listOf(AppActionButton.Uninstall)

                    installationState.enabled -> listOf(AppActionButton.Uninstall, AppActionButton.Open)
                    else -> listOf(AppActionButton.Uninstall, AppActionButton.Enable)
                }

                is AppInstallationState.Installed.UpdateAvailable -> when {
                    installationState.enabled && installationState.compatible -> listOf(
                        AppActionButton.Uninstall,
                        AppActionButton.Update,
                    )

                    installationState.enabled -> listOf(AppActionButton.Uninstall)
                    else -> listOf(AppActionButton.Uninstall, AppActionButton.Enable)
                }

                is AppInstallationState.NotInstalled -> when {
                    installationState.compatible && installationState.archived ->
                        listOf(AppActionButton.Unarchive)

                    installationState.compatible -> listOf(AppActionButton.Install)
                    else -> emptyList()
                }
            }

            when (installTaskState) {
                is InstallTaskState.Completed -> when (installTaskState.result) {
                    InstallSessionResult.PendingUserAction ->
                        listOf(AppActionButton.Cancel(enabled = false))

                    else -> whenNotCancelButtons
                }

                InstallTaskState.DownloadEnqueued -> listOf(AppActionButton.Cancel(enabled = true))
                is InstallTaskState.DownloadError -> whenNotCancelButtons
                is InstallTaskState.DownloadRunning ->
                    listOf(AppActionButton.Cancel(enabled = true))

                InstallTaskState.Installing -> listOf(AppActionButton.Cancel(enabled = false))
                InstallTaskState.NotRunning -> whenNotCancelButtons
            }
        }

        @Composable
        fun getDisplayText(): String? = when (installTaskState) {
            is InstallTaskState.Completed -> when (installTaskState.result) {
                InstallSessionResult.PendingUserAction -> stringResource(R.string.installing)
                else -> null
            }

            InstallTaskState.DownloadEnqueued -> stringResource(R.string.pending)
            is InstallTaskState.DownloadError,
            InstallTaskState.NotRunning -> null

            is InstallTaskState.DownloadRunning -> {
                val normalizedProgress = installTaskState.progress.downloadedBytes.toFloat() /
                        installTaskState.progress.totalBytes
                val percentString = normalizedProgress.toPercentString(LocalContext.current)
                val roundedTotalMegabytes = installTaskState.progress.totalBytes / 1024 / 1024

                stringResource(
                    R.string.downloading_with_progress,
                    percentString,
                    roundedTotalMegabytes,
                )
            }

            InstallTaskState.Installing -> stringResource(R.string.installing)
        }

        @Composable
        fun getErrorText(): String? = when (installTaskState) {
            is InstallTaskState.Completed -> when (installTaskState.result) {
                is InstallSessionResult.Failure.Blocked ->
                    when (val pkg = installTaskState.result.blockingPackage) {
                        null -> stringResource(R.string.install_failed_notif_body_blocked)
                        else -> stringResource(
                            R.string.install_failed_notif_body_blocked_by_package,
                            pkg,
                        )
                    }

                is InstallSessionResult.Failure.Conflict ->
                    when (val pkg = installTaskState.result.conflictingPackage) {
                        null -> stringResource(R.string.install_failed_notif_body_conflict)
                        else -> stringResource(
                            R.string.install_failed_notif_body_conflict_with_package,
                            pkg,
                        )
                    }

                is InstallSessionResult.Failure.Generic ->
                    stringResource(R.string.install_failed_notif_body_generic)

                is InstallSessionResult.Failure.Incompatible ->
                    stringResource(R.string.install_failed_notif_body_app_incompatible)

                is InstallSessionResult.Failure.Invalid ->
                    stringResource(R.string.install_failed_notif_body_app_invalid)

                is InstallSessionResult.Failure.Storage ->
                    stringResource(R.string.install_failed_notif_body_storage)

                is InstallSessionResult.Failure.Timeout ->
                    stringResource(R.string.install_failed_notif_body_timeout)
                // Don't show an error for Aborted since it's user-initiated and thus expected
                is InstallSessionResult.Failure.Aborted,
                InstallSessionResult.PendingUserAction,
                InstallSessionResult.Success -> null
            }

            is InstallTaskState.DownloadRunning,
            InstallTaskState.DownloadEnqueued,
            InstallTaskState.Installing,
            InstallTaskState.NotRunning -> null

            InstallTaskState.DownloadError.ApkDownloadError ->
                stringResource(R.string.app_details_error_apk_download_failed)

            InstallTaskState.DownloadError.AppIncompatible ->
                stringResource(R.string.app_details_error_app_incompatible)

            InstallTaskState.DownloadError.AppNotFound ->
                stringResource(R.string.app_details_error_app_not_found)

            InstallTaskState.DownloadError.AppStoreUnreachable ->
                stringResource(R.string.app_details_error_app_store_unreachable)

            InstallTaskState.DownloadError.Canceled ->
                stringResource(R.string.app_details_error_canceled)

            InstallTaskState.DownloadError.FileOpenFailed ->
                stringResource(R.string.app_details_error_file_open_failed)

            InstallTaskState.DownloadError.InstallationRequirementsNotMet ->
                stringResource(R.string.app_details_error_install_reqs_not_met)

            InstallTaskState.DownloadError.InstallationUnavailable ->
                stringResource(R.string.app_details_error_installation_unavailable)

            InstallTaskState.DownloadError.Internal ->
                stringResource(R.string.app_details_error_internal)

            InstallTaskState.DownloadError.InvalidServerResponse ->
                stringResource(R.string.app_details_error_invalid_server_response)

            InstallTaskState.DownloadError.IoError ->
                stringResource(R.string.app_details_error_io_error)

            InstallTaskState.DownloadError.NotAlreadyInstalled ->
                stringResource(R.string.app_details_error_not_already_installed)

            InstallTaskState.DownloadError.PackageVerificationFailed ->
                stringResource(R.string.app_details_error_package_verification_failed)

            InstallTaskState.DownloadError.Timeout ->
                stringResource(R.string.app_details_error_timeout)

            InstallTaskState.DownloadError.Unknown ->
                stringResource(R.string.app_details_error_unknown)
        }
    }

    data object Loading : AppDetailsUiState()
    data class LoadingError(val error: AppDetailsLoadState.Error) : AppDetailsUiState()
}

private fun Float.toPercentString(context: Context): String {
    val locale = context.resources.configuration.locales.get(0)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        NumberFormatter
            .withLocale(locale)
            .unit(MeasureUnit.PERCENT)
            .precision(Precision.fixedFraction(1))
            .format(this * 100)
            .toString()
    } else {
        NumberFormat
            .getPercentInstance(locale)
            .apply {
                minimumFractionDigits = 1
                maximumIntegerDigits = 1
            }
            .format(this)
    }
}
