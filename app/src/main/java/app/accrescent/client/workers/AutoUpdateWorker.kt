package app.accrescent.client.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoUpdateWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val repoDataRepository: RepoDataRepository,
    private val packageManager: PackageManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            repoDataRepository.fetchRepoData()
            context
                .packageManager
                .getInstalledPackages(0)
                .map { it.packageName }
                .filter { repoDataRepository.appExists(it) }
                .forEach { packageManager.downloadAndInstall(it) }
        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
    }
}
