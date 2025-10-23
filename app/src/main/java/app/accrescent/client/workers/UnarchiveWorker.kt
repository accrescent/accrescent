package app.accrescent.client.workers

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.accrescent.client.Accrescent
import app.accrescent.client.R
import app.accrescent.client.data.appmanager.AppManager
import app.accrescent.client.data.appmanager.InstallTaskParams
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val LOG_TAG = "UnarchiveWorker"
private const val UNARCHIVE_ID_UNSET = -999

@HiltWorker
class UnarchiveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appManager: AppManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val appId = inputData.getString(DataKey.APP_ID) ?: run {
            Log.e(LOG_TAG, "app ID expected in input data but was not found")
            return Result.failure(workDataOf(DataKey.ERROR_TYPE to ErrorType.INTERNAL))
        }
        val unarchiveId = inputData.getInt(DataKey.UNARCHIVE_ID, UNARCHIVE_ID_UNSET)
        if (unarchiveId == UNARCHIVE_ID_UNSET) {
            Log.e(LOG_TAG, "unarchive ID expected in input data but was not found")
            return Result.failure(workDataOf(DataKey.ERROR_TYPE to ErrorType.INTERNAL))
        }

        val result = appManager
            .downloadAndInstall(
                params = InstallTaskParams.Unarchive(appId, unarchiveId),
                onProgress = {
                    setProgress(
                        workDataOf(
                            DataKey.TOTAL_BYTES_TO_DOWNLOAD to it.totalBytes,
                            DataKey.BYTES_DOWNLOADED to it.downloadedBytes,
                        )
                    )
                },
            )
            .mapOrElse({ ResultExt.from(it) }, { Result.success() })

        return result
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationId = id.hashCode()
        val notification = Notification.Builder(
            applicationContext,
            Accrescent.DOWNLOADING_APP_CHANNEL,
        )
            .setSmallIcon(R.drawable.ic_baseline_download_24)
            .setContentTitle(applicationContext.getString(R.string.downloading_app))
            .build()

        return ForegroundInfo(
            notificationId,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }
}
