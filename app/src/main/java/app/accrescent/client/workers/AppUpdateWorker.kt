// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.workers

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
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

private const val LOG_TAG = "AppUpdateWorker"

@HiltWorker
class AppUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appManager: AppManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val appId = inputData.getString(DataKey.APP_ID) ?: run {
            Log.e(LOG_TAG, "app ID expected in input data but was not found")
            return Result.failure(workDataOf(DataKey.ERROR_TYPE to ErrorType.INTERNAL))
        }
        val currentVersionCode = try {
            context.packageManager.getPackageInfo(appId, 0).longVersionCode
        } catch (_: PackageManager.NameNotFoundException) {
            Log.w(LOG_TAG, "Could not get current app version. Perhaps it was uninstalled?")
            return Result.failure(
                workDataOf(DataKey.ERROR_TYPE to ErrorType.NOT_ALREADY_INSTALLED)
            )
        }

        val result = appManager
            .downloadAndInstall(
                params = InstallTaskParams.Update(appId, currentVersionCode),
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
