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
import app.accrescent.client.util.NotificationUtil
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.getInstalledPackagesCompat
import app.accrescent.client.util.getPackageInstallStatus
import build.buf.gen.accrescent.directory.v1.DirectoryServiceGrpcKt
import build.buf.gen.accrescent.directory.v1.getAppPackageInfoRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration

@HiltWorker
class AutoUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    private val directoryService: DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub,
    @Assisted workerParams: WorkerParameters,
    private val preferencesManager: PreferencesManager,
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            val packagesToUpdate = applicationContext.packageManager.getInstalledPackagesCompat()
                .filter { repoDataRepository.appExists(it.packageName) }
                .filter {
                    applicationContext.packageManager.getPackageInstallStatus(
                        it.packageName,
                        null,
                    ) != InstallStatus.INSTALLED_FROM_ANOTHER_SOURCE
                }
                .filter {
                    directoryService
                        .getAppPackageInfo(getAppPackageInfoRequest { appId = it.packageName })
                        .packageInfo
                        .versionCode > it.longVersionCode
                }

            if (preferencesManager.automaticUpdates.first()) {
                packagesToUpdate.forEach { packageManager.downloadAndInstall(it.packageName) }
            } else {
                packagesToUpdate.forEachIndexed { index, packageInfo ->
                    val request = getAppPackageInfoRequest { appId = packageInfo.packageName }
                    val newVersionName = directoryService
                        .getAppPackageInfo(request)
                        .packageInfo
                        .versionName
                    showUpdateNotification(index, packageInfo, newVersionName)
                }
            }
        } catch (e: Exception) {
            Result.failure()
        }

        return Result.success()
    }

    private fun showUpdateNotification(index: Int, packageInfo: PackageInfo, newVersionName: String) {
        val notificationManager = applicationContext.getSystemService<NotificationManager>()!!

        val packageLabel = packageInfo.applicationInfo
            ?.let { applicationContext.packageManager.getApplicationLabel(it) }
            ?: packageInfo.packageName
        val pendingIntent = NotificationUtil
            .createPendingIntentForAppId(applicationContext, packageInfo.packageName)
        val notification = NotificationCompat.Builder(applicationContext, UPDATE_AVAILABLE_CHANNEL)
            .setContentTitle(packageLabel)
            .setContentText("${packageInfo.versionName} -> $newVersionName")
            .setSmallIcon(R.drawable.ic_baseline_update_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        @Suppress("MissingPermission")
        notificationManager.notify(index + 1000, notification.build())
    }

    companion object {
        private const val UPDATER_WORK_NAME = "UPDATE_APPS"

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
