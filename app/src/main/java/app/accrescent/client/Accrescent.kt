// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.NetworkType
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.data.appmanager.InstallSessionRepository
import app.accrescent.client.workers.AutoUpdateWorker
import app.accrescent.client.workers.RepositoryRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class Accrescent : Application(), Configuration.Provider {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var installSessionRepository: InstallSessionRepository

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    DOWNLOADING_APP_CHANNEL,
                    getString(R.string.downloading_app),
                    NotificationManager.IMPORTANCE_LOW,
                ),
                NotificationChannel(
                    INSTALLATION_FAILED_CHANNEL,
                    getString(R.string.installation_failed),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
                NotificationChannel(
                    INSTALLATION_FINISHED_CHANNEL,
                    getString(R.string.installation_finished),
                    NotificationManager.IMPORTANCE_LOW,
                ),
                NotificationChannel(
                    UPDATE_AVAILABLE_CHANNEL,
                    getString(R.string.update_available),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
                NotificationChannel(
                    UPDATE_FAILED_CHANNEL,
                    getString(R.string.update_failed),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
                NotificationChannel(
                    UPDATE_FINISHED_CHANNEL,
                    getString(R.string.update_finished),
                    NotificationManager.IMPORTANCE_LOW,
                ),
                NotificationChannel(
                    USER_ACTION_REQUIRED_CHANNEL,
                    getString(R.string.user_action_required),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        )

        val networkType = runBlocking { preferencesManager.networkType.firstOrNull() }
            ?.let { NetworkType.valueOf(it) }
            ?: NetworkType.UNMETERED
        RepositoryRefreshWorker.enqueue(applicationContext, networkType)
        AutoUpdateWorker.enqueue(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()
        installSessionRepository.close()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appContext = base
    }

    companion object {
        lateinit var appContext: Context
        const val DOWNLOADING_APP_CHANNEL = "DownloadingApp"
        const val INSTALLATION_FAILED_CHANNEL = "InstallationFailed"
        const val INSTALLATION_FINISHED_CHANNEL = "InstallationFinished"
        const val UPDATE_AVAILABLE_CHANNEL = "UpdateAvailable"
        const val UPDATE_FAILED_CHANNEL = "UpdateFailed"
        const val UPDATE_FINISHED_CHANNEL = "UpdateFinished"
        const val USER_ACTION_REQUIRED_CHANNEL = "UserActionRequired"
    }
}
