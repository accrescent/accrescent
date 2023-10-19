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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration

@HiltWorker
class RepositoryRefreshWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val workerParams: WorkerParameters,
    private val repoDataRepository: RepoDataRepository,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            repoDataRepository.fetchRepoData()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount >= MAX_RUN_ATTEMPTS) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    companion object {
        private const val UPDATER_WORK_NAME = "REFRESH_REPOSITORY"
        private const val MAX_RUN_ATTEMPTS = 5

        fun enqueue(context: Context, networkType: NetworkType) {
            val updateRequest = PeriodicWorkRequestBuilder<RepositoryRefreshWorker>(Duration.ofHours(4))
                .setConstraints(Constraints(requiredNetworkType = networkType))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateRequest,
            )
        }
    }
}
