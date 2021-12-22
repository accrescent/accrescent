package net.lberrymage.accrescent.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.lberrymage.accrescent.data.DeveloperRepository

@HiltWorker
class RefreshDevelopersWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val developerRepository: DeveloperRepository,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        try {
            developerRepository.fetchLatestDevelopers()
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }
}
