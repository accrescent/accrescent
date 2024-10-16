package app.accrescent.client.workers

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager.MATCH_ARCHIVED_PACKAGES
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.PackageManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.content.pm.PackageManager as AndroidPackageManager

private const val TAG = "UnarchiveWorker"

@HiltWorker
@TargetApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class UnarchiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val packageManager: PackageManager,
    private val repoDataRepository: RepoDataRepository,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val packageName = inputData.getString(PackageInstaller.EXTRA_UNARCHIVE_PACKAGE_NAME) ?: run {
            Log.e(TAG, "No package name specified. Returning early.")
            return Result.failure()
        }
        val unarchiveId = inputData.getInt(PackageInstaller.EXTRA_UNARCHIVE_ID, -999)

        try {
            applicationContext.packageManager
                .getPackageInfo(packageName, PackageInfoFlags.of(MATCH_ARCHIVED_PACKAGES))
                .takeIf { repoDataRepository.appExists(it.packageName) }
                ?.let { packageManager.downloadAndInstall(it.packageName, unarchiveId = unarchiveId) }
                ?: run {
                    Log.e(TAG, "Package $packageName not found in repository")
                    return Result.failure()
                }
        } catch (e: AndroidPackageManager.NameNotFoundException) {
            Log.e(TAG, "Package $packageName not found on device")
            return Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error: ${e.message}")
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        fun enqueue(context: Context, packageName: String, unarchiveId: Int) {
            val data = Data.Builder()
                .putString(PackageInstaller.EXTRA_UNARCHIVE_PACKAGE_NAME, packageName)
                .putInt(PackageInstaller.EXTRA_UNARCHIVE_ID, unarchiveId)
                .build()
            val unarchiveRequest = OneTimeWorkRequestBuilder<UnarchiveWorker>().setInputData(data).build()
            WorkManager.getInstance(context).enqueue(unarchiveRequest)
        }
    }
}
