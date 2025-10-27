// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.workers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.accrescent.client.Accrescent
import app.accrescent.client.R
import app.accrescent.client.core.Outcome
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.data.appmanager.AppManager
import app.accrescent.client.data.appmanager.InstallWorkRepository
import app.accrescent.client.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.time.Duration

@HiltWorker
class AutoUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appManager: AppManager,
    private val installWorkRepository: InstallWorkRepository,
    private val preferencesManager: PreferencesManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val packagesToCheck = applicationContext
            .packageManager
            .getInstalledPackages(0)
            .filter { appManager.shouldAutoUpdatePackage(it) }
        val autoUpdatesEnabled = preferencesManager.automaticUpdates.firstOrNull() ?: true

        if (autoUpdatesEnabled) {
            val networkType = preferencesManager.networkType.firstOrNull()
                ?.let { NetworkType.valueOf(it) }
                ?: NetworkType.CONNECTED
            for (pkg in packagesToCheck) {
                installWorkRepository.enqueueUpdateWorker(
                    appId = pkg.packageName,
                    preferExpedited = false,
                    networkType = networkType,
                )
            }
        } else {
            for (pkg in packagesToCheck) {
                val result = appManager.isUpdateAvailable(pkg.packageName)
                val isUpdateAvailable = when (result) {
                    is Outcome.Err -> continue
                    is Outcome.Ok -> result.value
                }
                if (isUpdateAvailable) {
                    showUpdateNotification(applicationContext, pkg)
                }
            }
        }

        return Result.success()
    }

    private fun showUpdateNotification(context: Context, packageInfo: PackageInfo) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val packageLabel = packageInfo
            .applicationInfo
            ?.loadLabel(context.packageManager)
            ?: packageInfo.packageName
        val pendingIntent = getAppDetailsIntent(packageInfo.packageName)
        val notification = Notification.Builder(applicationContext, Accrescent.UPDATE_AVAILABLE_CHANNEL)
            .setContentTitle(packageLabel)
            .setContentText(
                applicationContext.getString(R.string.update_available_for_app, packageLabel)
            )
            .setSmallIcon(R.drawable.update_rounded_24px)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(packageInfo.packageName.hashCode(), notification)
    }

    private fun getAppDetailsIntent(appId: String): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext,
            appId.hashCode(),
            Intent(applicationContext, MainActivity::class.java)
                .putExtra(Intent.EXTRA_PACKAGE_NAME, appId),
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val UPDATER_WORK_NAME = "UPDATE_APPS"

        fun enqueue(context: Context) {
            val updateRequest = PeriodicWorkRequestBuilder<AutoUpdateWorker>(Duration.ofHours(4))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UPDATER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateRequest,
            )
        }
    }
}
