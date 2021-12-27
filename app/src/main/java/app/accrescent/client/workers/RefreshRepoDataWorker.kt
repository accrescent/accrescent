package app.accrescent.client.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.accrescent.client.data.RepoDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RefreshRepoDataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repoDataRepository: RepoDataRepository,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            repoDataRepository.fetchLatestRepoData()
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}
