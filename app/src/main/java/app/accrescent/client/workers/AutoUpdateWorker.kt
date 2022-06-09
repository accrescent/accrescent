package app.accrescent.client.workers

import android.content.Context
import android.content.pm.PackageManager as OsPackageManager
import android.os.Build
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

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                context.packageManager.getInstalledPackages(0)
            } else {
                context.packageManager.getInstalledPackages(OsPackageManager.PackageInfoFlags.of(0))
            }.filter { repoDataRepository.appExists(it.packageName) }
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
}
