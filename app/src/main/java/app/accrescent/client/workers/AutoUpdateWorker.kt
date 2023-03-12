package app.accrescent.client.workers

import android.content.Context
import android.content.pm.PackageInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.data.net.AppRepoData
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.getInstalledPackagesCompat
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
    private val packageManager: PackageManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            repoDataRepository.fetchRepoData()

            val packagesToUpdate = context.packageManager.getInstalledPackagesCompat(0)
                .filter { repoDataRepository.appExists(it.packageName) }
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
            return Result.failure()
        }

        return Result.success()
    }

    private fun showUpdateNotification(index: Int, packageInfo: PackageInfo, repoData: AppRepoData) {
        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, UPDATE_AVAILABLE_CHANNEL)
            .setContentTitle(packageInfo.packageName)
            .setContentText("${packageInfo.versionName} -> ${repoData.version}")
            .setSmallIcon(R.drawable.ic_baseline_update_24)
            .setAutoCancel(true)

        notificationManager.notify(index + 1000, notification.build())
    }

    companion object {
        private const val UPDATER_WORK_NAME = "UPDATE_APPS"

        fun enqueue(context: Context, networkType: NetworkType, forceUpdate: Boolean = true) {
            val constraints = Constraints(
                requiredNetworkType = networkType,
                requiresDeviceIdle = true,
                requiresStorageNotLow = true
            )
            val updateRequest = PeriodicWorkRequestBuilder<AutoUpdateWorker>(Duration.ofHours(4))
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATER_WORK_NAME,
                if (forceUpdate) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        }
    }
}
