package app.accrescent.client.workers

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.accrescent.client.Accrescent.Companion.UPDATE_AVAILABLE_CHANNEL
import app.accrescent.client.BuildConfig
import app.accrescent.client.R
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.data.net.AppRepoData
import app.accrescent.client.util.NotificationUtil
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.getInstalledPackagesCompat
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration

private const val PACKAGE_INSTALLER_APP_ID = "com.google.android.packageinstaller"

@HiltWorker
class AutoUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val preferencesManager: PreferencesManager,
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            val packagesToUpdate = context.packageManager.getInstalledPackagesCompat()
                .filter { repoDataRepository.appExists(it.packageName) }
                .filter {
                    val installerOfRecord = getInstallerOfRecord(it.packageName)

                    // Only attempt to update apps which either:
                    //
                    // 1. We are the installer of record for
                    // 2. Have no installer of record
                    // 3. Were installed by the system PackageInstaller app, such as when the user
                    // installs from a downloaded APK
                    installerOfRecord == BuildConfig.APPLICATION_ID ||
                            installerOfRecord == null ||
                            installerOfRecord == PACKAGE_INSTALLER_APP_ID
                }
                .filter {
                    repoDataRepository
                        .getAppRepoData(it.packageName)
                        .versionCode > it.longVersionCode
                }

            if (preferencesManager.automaticUpdates.first()) {
                packagesToUpdate.forEach { packageManager.downloadAndInstall(it.packageName) }
            } else {
                packagesToUpdate.forEachIndexed { index, packageInfo ->
                    val repoData = repoDataRepository.getAppRepoData(packageInfo.packageName)
                    showUpdateNotification(index, packageInfo, repoData)
                }
            }
        } catch (e: Exception) {
            if (runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        return Result.success()
    }

    /**
     * Returns the installer of record for the given [appId]
     *
     * @return the installer of record for the given app, or null if the app was not installed by a
     * package or if the installing package itself has been uninstalled
     */
    private fun getInstallerOfRecord(appId: String): String? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            try {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(appId)
            } catch (e: IllegalArgumentException) {
                null
            }
        } else {
            // getInstallSourceInfo should never throw because we hold QUERY_ALL_PACKAGES
            context.packageManager.getInstallSourceInfo(appId).installingPackageName
        }
    }

    private fun showUpdateNotification(index: Int, packageInfo: PackageInfo, repoData: AppRepoData) {
        val notificationManager = context.getSystemService<NotificationManager>()!!

        val packageLabel = context.packageManager.getApplicationLabel(packageInfo.applicationInfo)
        val pendingIntent = NotificationUtil.createPendingIntentForAppId(context, packageInfo.packageName)
        val notification = NotificationCompat.Builder(context, UPDATE_AVAILABLE_CHANNEL)
            .setContentTitle(packageLabel)
            .setContentText("${packageInfo.versionName} -> ${repoData.version}")
            .setSmallIcon(R.drawable.ic_baseline_update_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        @Suppress("MissingPermission")
        notificationManager.notify(index + 1000, notification.build())
    }

    companion object {
        private const val UPDATER_WORK_NAME = "UPDATE_APPS"
        private const val MAX_RUN_ATTEMPTS = 5

        fun enqueue(context: Context, networkType: NetworkType) {
            val constraints = Constraints(
                requiredNetworkType = networkType,
                requiresDeviceIdle = true,
                requiresStorageNotLow = true,
            )
            val updateRequest = PeriodicWorkRequestBuilder<AutoUpdateWorker>(Duration.ofHours(4))
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateRequest,
            )
        }
    }
}
