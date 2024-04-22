package app.accrescent.client.workers

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageInfo
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
import app.accrescent.client.R
import app.accrescent.client.data.InstallStatus
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.data.net.AppRepoData
import app.accrescent.client.util.NotificationUtil
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.getInstalledPackagesCompat
import app.accrescent.client.util.getPackageInstallStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration

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
                    context.packageManager.getPackageInstallStatus(
                        it.packageName,
                        null,
                    ) != InstallStatus.INSTALLED_FROM_ANOTHER_SOURCE
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
