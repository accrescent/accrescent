// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import app.accrescent.client.BuildConfig
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.receivers.AppUninstallBroadcastReceiver
import app.accrescent.client.receivers.InstallerSessionCommitBroadcastReceiver
import app.accrescent.client.receivers.UnarchiveResponseBroadcastReceiver
import app.accrescent.client.util.copyToWithProgress
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import build.buf.gen.accrescent.appstore.v1.AppServiceGrpcKt
import build.buf.gen.accrescent.appstore.v1.appUpdateInfoOrNull
import build.buf.gen.accrescent.appstore.v1.getAppDownloadInfoRequest
import build.buf.gen.accrescent.appstore.v1.getAppUpdateInfoRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.Status
import io.grpc.StatusException
import jakarta.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

// 3 days
private const val GENTLE_UPDATE_CONSTRAINT_TIMEOUT_MILLIS = 259200000L
private const val LOG_TAG = "AppManager"

class AppManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appService: AppServiceGrpcKt.AppServiceCoroutineStub,
    private val deviceAttributesRepository: DeviceAttributesRepository,
    private val okHttpClient: OkHttpClient,
    private val repoDataRepository: RepoDataRepository,
) {
    companion object {
        const val EXTRA_INSTALL_APP_REQUEST_BUNDLE =
            "app.accrescent.client.pm.extra.INSTALL_APP_REQUEST_BUNDLE"
    }

    /**
     * Downloads and installs the app specified by [params].
     *
     * @param params the parameters for the installation task.
     * @param onProgress the callback used for reporting download progress.
     * @return true if the app was successfully installed, false if it was already up-to-date.
     */
    suspend fun downloadAndInstall(
        params: InstallTaskParams,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): Either<InstallTaskError, Boolean> = either {
        val appDownloadInfo = when (params) {
            is InstallTaskParams.InitialInstall,
            is InstallTaskParams.Unarchive -> getAppDownloadInfo(params.appId)

            is InstallTaskParams.Update -> getAppUpdateInfo(params.appId, params.currentVersionCode)
        }
            .mapLeft { InstallTaskError.DownloadInfoFetch(it) }
            .bind()
            ?: return@either false

        val totalDownloadSize = appDownloadInfo.splitDownloadInfo.sumOf { it.size }
        val sessionParams = createSessionParams(
            appId = params.appId,
            sessionSize = totalDownloadSize,
            unarchiveId = if (params is InstallTaskParams.Unarchive) params.unarchiveId else null,
        )
        val sessionId = createInstallSession(sessionParams)
            .mapLeft { InstallTaskError.InstallSessionCreation(it) }
            .bind()

        // Attempt to update signing certificates before installation
        try {
            repoDataRepository.fetchRepoData()
        } catch (_: Throwable) {
        }
        val requiredSigner = repoDataRepository
            .getAppSigner(params.appId)
            ?: raise(InstallTaskError.NoSignerInfo)
        val minVersionCode = repoDataRepository
            .getAppMinVersionCode(params.appId)
            ?: raise(InstallTaskError.NoMinVersionCode)

        openInstallSession(sessionId)
            .mapLeft { InstallTaskError.InstallSessionOpen(it) }
            .bind()
            .use { session ->
                try {
                    val apkNames = downloadApksToSession(
                        session,
                        appDownloadInfo.splitDownloadInfo,
                        onProgress,
                    ).mapLeft { InstallTaskError.ApkDownload(it) }.bind()

                    verifyPackageInfo(session, apkNames, requiredSigner, minVersionCode)
                        .mapLeft { InstallTaskError.PackageVerification(it) }
                        .bind()

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        sessionId,
                        Intent(context, InstallerSessionCommitBroadcastReceiver::class.java)
                            .putExtra(
                                EXTRA_INSTALL_APP_REQUEST_BUNDLE,
                                InstallAppRequest(
                                    appId = params.appId,
                                    sessionId = sessionId,
                                    installType = when (params) {
                                        is InstallTaskParams.InitialInstall -> InstallType.INSTALL
                                        is InstallTaskParams.Unarchive -> InstallType.UNARCHIVE
                                        is InstallTaskParams.Update -> InstallType.UPDATE
                                    },
                                ).toBundle(),
                            ),
                        // Required by PackageInstaller.Session.commit()
                        PendingIntent.FLAG_MUTABLE,
                    )

                    // We don't distribute Accrescent via Google Play, so this lint is irrelevant to
                    // us
                    @SuppressLint("RequestInstallPackagesPolicy")
                    if (
                        params is InstallTaskParams.Update &&
                        params.isGentle &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                        // GENTLE_UPDATE is too passive for us to feel safe using it for self
                        // updates, so commit normally when performing a self update
                        params.appId != BuildConfig.APPLICATION_ID
                    ) {
                        try {
                            context
                                .packageManager
                                .packageInstaller
                                .commitSessionAfterInstallConstraintsAreMet(
                                    sessionId,
                                    pendingIntent.intentSender,
                                    PackageInstaller.InstallConstraints.GENTLE_UPDATE,
                                    GENTLE_UPDATE_CONSTRAINT_TIMEOUT_MILLIS,
                                )
                        } catch (_: SecurityException) {
                            // We're not the installer of record, possibly because the app was
                            // uninstalled before we attempted to update it, so fall back to
                            // committing the usual way
                            session.commit(pendingIntent.intentSender)
                        }
                    } else {
                        session.commit(pendingIntent.intentSender)
                    }
                } catch (t: Throwable) {
                    session.abandon()
                    throw t
                }
            }

        true
    }

    fun uninstall(appId: String) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AppUninstallBroadcastReceiver::class.java),
            // Required by PackageInstaller.uninstall()
            PendingIntent.FLAG_MUTABLE,
        )
        context.packageManager.packageInstaller.uninstall(appId, pendingIntent.intentSender)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun unarchive(appId: String): Either<UnarchiveError, Unit> {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, UnarchiveResponseBroadcastReceiver::class.java),
            PendingIntent.FLAG_MUTABLE,
        )

        val result = try {
            context
                .packageManager
                .packageInstaller
                .requestUnarchive(appId, pendingIntent.intentSender)
            Unit.right()
        } catch (_: PackageManager.NameNotFoundException) {
            UnarchiveError.PackageOrInstallerNotFound.left()
        } catch (_: IOException) {
            UnarchiveError.ParametersUnsatisfiable.left()
        }

        return result
    }

    suspend fun isAppCompatible(appId: String): Boolean = try {
        val request = getAppDownloadInfoRequest {
            this.appId = appId
            deviceAttributes = deviceAttributesRepository.getDeviceAttributes()
        }
        appService.getAppDownloadInfo(request)
        true
    } catch (e: StatusException) {
        when (e.status.code) {
            Status.Code.FAILED_PRECONDITION -> false
            else -> throw e
        }
    }

    suspend fun isUpdateAvailable(
        appId: String,
    ): Either<DownloadInfoFetchError, Boolean> = either {
        val currentVersionCode = try {
            context.packageManager.getPackageInfo(appId, 0).longVersionCode
        } catch (_: PackageManager.NameNotFoundException) {
            return@either false
        }
        val updateInfo = getAppUpdateInfo(appId, currentVersionCode).bind()

        updateInfo != null
    }

    fun selfResponsibleForUpdatingPackage(appId: String): Boolean {
        val responsibleInstaller = getResponsibleInstaller(appId)

        // We should attempt to update the package if either 1) Accrescent is the responsible
        // installer for the package or 2) the package is Accrescent itself.
        return responsibleInstaller == BuildConfig.APPLICATION_ID
                || appId == BuildConfig.APPLICATION_ID
    }

    fun shouldAutoUpdatePackage(packageInfo: PackageInfo): Boolean {
        // Auto update the package if and only if we can do so without implicitly enabling it from a
        // disabled state. On SDK 34+, we use SessionParams.setApplicationEnabledSettingPersistent()
        // to update disabled apps without enabling them, but before SDK 34, we must simply not auto
        // update disabled apps.
        val wouldReenable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            false
        } else {
            packageInfo.applicationInfo?.enabled == false
        }

        return !wouldReenable && selfResponsibleForUpdatingPackage(packageInfo.packageName)
    }

    private fun getResponsibleInstaller(appId: String): String? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(appId).installingPackageName
        } else {
            // This is the only method available before API 30
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(appId)
        }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun verifyPackageInfo(
        session: PackageInstaller.Session,
        apkNames: List<String>,
        requiredSigner: String,
        minVersionCode: Long,
    ): Either<PackageVerificationError, Unit> = either {
        for (apkName in apkNames) {
            openSessionRead(session, apkName)
                .mapLeft { PackageVerificationError.OpenSessionRead(it) }
                .bind()
                .use { apkStream ->
                    // This method will never throw IllegalArgumentException since apkName is a
                    // SHA-256 hash, which is always longer than 2 characters. We can also assume
                    // that this method doesn't throw SecurityException because Android has no
                    // SecurityManager according to
                    // https://developer.android.com/reference/java/lang/SecurityManager. Thus, we
                    // need to handle only IOException here.
                    val tmpFile = try {
                        File.createTempFile(apkName, null, context.cacheDir)
                    } catch (_: IOException) {
                        raise(PackageVerificationError.IoError)
                    }
                    try {
                        tmpFile.outputStream().use { apkStream.copyTo(it) }
                        // We MUST OR GET_SIGNING_CERTIFICATES (the modern flag) with
                        // GET_SIGNATURES (the deprecated flag) because of
                        // https://issuetracker.google.com/issues/243324820 or else package
                        // verification completely breaks on devices still on the initial release of
                        // Android 13.
                        @Suppress("DEPRECATION")
                        val flags = PackageManager.GET_SIGNING_CERTIFICATES or
                                PackageManager.GET_SIGNATURES
                        val packageInfo = context
                            .packageManager
                            .getPackageArchiveInfo(tmpFile.toString(), flags)
                            ?: continue
                        val signingInfo = packageInfo
                            .signingInfo
                            ?: raise(PackageVerificationError.SigningInfoNotPresent)

                        // First, verify the app's signing certificates
                        if (signingInfo.hasMultipleSigners()) {
                            raise(PackageVerificationError.MultipleSigners)
                        } else {
                            val signedByRequiredCert = signingInfo
                                .signingCertificateHistory
                                .map { hexSha256Sum(it.toByteArray()) }
                                .contains(requiredSigner)
                            if (!signedByRequiredCert) {
                                raise(PackageVerificationError.NotSignedByRequiredSigner)
                            }
                        }

                        // Finally, verify the app's minimum version code
                        if (packageInfo.longVersionCode < minVersionCode) {
                            raise(PackageVerificationError.MinimumVersionNotMet)
                        }

                        // Skip processing other APKs once we know one is verified
                        return@either
                    } catch (_: IOException) {
                        raise(PackageVerificationError.IoError)
                    } finally {
                        if (!tmpFile.delete()) {
                            Log.w(LOG_TAG, "failed to delete temporary file $tmpFile")
                        }
                    }
                }
        }

        raise(PackageVerificationError.PackageParsingFailed)
    }

    suspend fun getAppDownloadInfo(
        appId: String,
    ): Either<DownloadInfoFetchError, AppDownloadInfo> = try {
        val request = getAppDownloadInfoRequest {
            this.appId = appId
            deviceAttributes = deviceAttributesRepository.getDeviceAttributes()
        }
        appService
            .getAppDownloadInfo(request)
            .appDownloadInfo
            .let { AppDownloadInfo.from(it).right() }
    } catch (e: StatusException) {
        DownloadInfoFetchError.from(e).left()
    }

    private suspend fun getAppUpdateInfo(
        appId: String,
        baseVersionCode: Long,
    ): Either<DownloadInfoFetchError, AppDownloadInfo?> = try {
        val request = getAppUpdateInfoRequest {
            this.appId = appId
            deviceAttributes = deviceAttributesRepository.getDeviceAttributes()
            this.baseVersionCode = baseVersionCode
        }
        appService
            .getAppUpdateInfo(request)
            .appUpdateInfoOrNull
            .let { appUpdateInfo ->
                if (appUpdateInfo == null) {
                    null.right()
                } else {
                    AppDownloadInfo.from(appUpdateInfo).right()
                }
            }
    } catch (e: StatusException) {
        DownloadInfoFetchError.from(e).left()
    }

    private fun createSessionParams(
        appId: String,
        sessionSize: Long,
        unarchiveId: Int?
    ): PackageInstaller.SessionParams {
        val sessionParams =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        sessionParams.setAppPackageName(appId)
        sessionParams.setSize(sessionSize)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // This call is effectively a no-op for initial installations, but could be applicable
            // if the user installs the same app from another source before this session is
            // committed, in which case we're actually updating it.
            sessionParams.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sessionParams.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            sessionParams.setApplicationEnabledSettingPersistent()

            // This call is effectively a no-op for updates, but could be applicable if the user
            // uninstalls the app before this session is commited, in which case it's actually an
            // initial installation.
            sessionParams.setRequestUpdateOwnership(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && unarchiveId != null) {
            sessionParams.setUnarchiveId(unarchiveId)
        }

        return sessionParams
    }

    private fun createInstallSession(
        sessionParams: PackageInstaller.SessionParams,
    ): Either<InstallSessionCreationError, Int> = try {
        context.packageManager.packageInstaller.createSession(sessionParams).right()
    } catch (_: IOException) {
        InstallSessionCreationError.ParametersUnsatisfiable.left()
    } catch (_: SecurityException) {
        InstallSessionCreationError.InstallationServicesUnavailable.left()
    } catch (_: IllegalArgumentException) {
        InstallSessionCreationError.SessionParamsInvalid.left()
    }

    private fun openInstallSession(
        sessionId: Int,
    ): Either<InstallSessionOpenError, PackageInstaller.Session> = try {
        context.packageManager.packageInstaller.openSession(sessionId).right()
    } catch (_: IOException) {
        InstallSessionOpenError.ParametersUnsatisfiable.left()
    } catch (_: SecurityException) {
        InstallSessionOpenError.SessionInvalidOrNotOwned.left()
    }

    private suspend fun downloadApksToSession(
        session: PackageInstaller.Session,
        splitDownloadInfo: List<SplitDownloadInfo>,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): Either<ApkDownloadError, List<String>> = either {
        val apkNames = mutableListOf<String>()

        val totalDownloadSize = splitDownloadInfo.sumOf { it.size }
        val apkNameToDownloadedBytes = mutableMapOf<String, Long>()

        for (downloadInfo in splitDownloadInfo) {
            val httpUrl = downloadInfo
                .apkUrl
                .toHttpUrlOrNull()
                ?: raise(ApkDownloadError.InvalidApkUrl)
            val apkName = hexSha256Sum(downloadInfo.apkUrl.toByteArray())
            apkNames.add(apkName)

            try {
                okHttpClient
                    .newCall(Request(httpUrl))
                    .executeAsync()
                    .use { response ->
                        if (!response.isSuccessful) {
                            raise(ApkDownloadError.UnsuccessfulResponseCode)
                        }

                        response.body.byteStream().use { apkStream ->
                            openSessionWrite(session, apkName, downloadInfo.size)
                                .mapLeft { ApkDownloadError.OpenSessionWrite(it) }
                                .bind()
                                .use { sessionStream ->
                                    apkStream
                                        .copyToWithProgress(sessionStream)
                                        .collect { bytesDownloaded ->
                                            apkNameToDownloadedBytes.put(apkName, bytesDownloaded)
                                            val totalDownloadedBytes = apkNameToDownloadedBytes
                                                .values
                                                .sum()
                                            val downloadProgress = DownloadProgress(
                                                totalDownloadSize,
                                                totalDownloadedBytes,
                                            )

                                            session.setStagingProgress(
                                                totalDownloadedBytes.toFloat() / totalDownloadSize
                                            )
                                            onProgress(downloadProgress)
                                        }
                                    session.fsync(sessionStream)
                                }
                        }
                    }
            } catch (_: IOException) {
                raise(ApkDownloadError.IoException)
            }
        }

        apkNames
    }

    private fun openSessionRead(
        session: PackageInstaller.Session,
        apkName: String,
    ): Either<OpenSessionReadError, InputStream> = try {
        session.openRead(apkName).right()
    } catch (_: SecurityException) {
        OpenSessionReadError.SessionCommittedOrAbandoned.left()
    } catch (_: IOException) {
        OpenSessionReadError.IoError.left()
    }

    private fun openSessionWrite(
        session: PackageInstaller.Session,
        apkName: String,
        apkSize: Long,
    ): Either<OpenSessionWriteError, OutputStream> = try {
        session.openWrite(apkName, 0, apkSize).right()
    } catch (_: IOException) {
        OpenSessionWriteError.FileOpenFailed.left()
    } catch (_: SecurityException) {
        OpenSessionWriteError.SessionSealedOrAbandoned.left()
    }

    private fun hexSha256Sum(data: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(data).toHexString()
    }
}
