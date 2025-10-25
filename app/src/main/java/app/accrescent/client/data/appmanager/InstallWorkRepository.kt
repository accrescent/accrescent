// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.accrescent.client.workers.AppInstallWorker
import app.accrescent.client.workers.AppUpdateWorker
import app.accrescent.client.workers.DataKey
import app.accrescent.client.workers.NoopWorker
import app.accrescent.client.workers.UnarchiveWorker
import app.accrescent.client.workers.WorkerTag
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class InstallWorkRepository @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val INSTALL_WORK_PREFIX = "InstallApp"
        private fun uniqueInstallWorkNameForApp(appId: String): String {
            return "$INSTALL_WORK_PREFIX-$appId"
        }
    }

    private val workManager = WorkManager.getInstance(context)

    fun enqueueInstallWorker(appId: String) {
        val workRequest = OneTimeWorkRequestBuilder<AppInstallWorker>()
            .setInputData(workDataOf(DataKey.APP_ID to appId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uniqueInstallWorkNameForApp(appId),
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest,
        )
    }

    fun enqueueUnarchiveWorker(appId: String, unarchiveId: Int) {
        val workRequest = OneTimeWorkRequestBuilder<UnarchiveWorker>()
            .setInputData(workDataOf(DataKey.APP_ID to appId, DataKey.UNARCHIVE_ID to unarchiveId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uniqueInstallWorkNameForApp(appId),
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest,
        )
    }

    fun enqueueUpdateWorker(
        appId: String,
        preferExpedited: Boolean,
        networkType: NetworkType = NetworkType.CONNECTED,
    ) {
        val workRequest = OneTimeWorkRequestBuilder<AppUpdateWorker>()
            .setInputData(workDataOf(DataKey.APP_ID to appId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType).build())
            .apply {
                if (preferExpedited) {
                    setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                }
            }
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uniqueInstallWorkNameForApp(appId),
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest,
        )
    }

    fun cancelAppInstallWork(appId: String) {
        workManager.cancelUniqueWork(uniqueInstallWorkNameForApp(appId))
    }

    fun clearAppInstallWork(appId: String) {
        // Clear the app install job status with a clever trick: enqueue a no-op one-time work item
        // with the same unique ID, effectively overriding its status. We also give it a special
        // NOOP tag so we can distinguish it from a real install job since it may not be scheduled
        // immediately and we don't want the no-op job to appear as an in-progress install job
        val workRequest = OneTimeWorkRequestBuilder<NoopWorker>().addTag(WorkerTag.NOOP).build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = uniqueInstallWorkNameForApp(appId),
            existingWorkPolicy = ExistingWorkPolicy.REPLACE,
            request = workRequest
        )
    }

    fun getInstallWorkInfosForAppId(appId: String): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(uniqueInstallWorkNameForApp(appId))
    }
}