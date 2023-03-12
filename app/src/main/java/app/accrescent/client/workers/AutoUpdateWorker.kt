package app.accrescent.client.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import app.accrescent.client.util.getInstalledPackagesCompat
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration

@HiltWorker
class AutoUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            repoDataRepository.fetchRepoData()

            context.packageManager.getInstalledPackagesCompat(0)
                .filter { repoDataRepository.appExists(it.packageName) }
                .filter {
                    repoDataRepository
                        .getAppRepoData(it.packageName)
                        .versionCode > it.longVersionCode
                }.forEach { packageManager.downloadAndInstall(it.packageName) }
        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
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
