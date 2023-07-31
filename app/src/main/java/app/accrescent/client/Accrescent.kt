package app.accrescent.client

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.NetworkType
import app.accrescent.client.data.PreferencesManager
import app.accrescent.client.receivers.AppStatusChangeBroadcastReceiver
import app.accrescent.client.util.registerReceiverNotExported
import app.accrescent.client.workers.AutoUpdateWorker
import app.accrescent.client.workers.RepositoryRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class Accrescent : Application(), Configuration.Provider {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    USER_ACTION_REQUIRED_CHANNEL,
                    getString(R.string.user_action_required),
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    UPDATE_AVAILABLE_CHANNEL,
                    getString(R.string.update_available),
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    UPDATE_FINISHED_CHANNEL,
                    getString(R.string.update_finished),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        )

        val networkType = runBlocking { preferencesManager.networkType.firstOrNull() }
            ?.let { NetworkType.valueOf(it) }
            ?: NetworkType.CONNECTED
        RepositoryRefreshWorker.enqueue(applicationContext, networkType)
        AutoUpdateWorker.enqueue(applicationContext, networkType)

        val br = AppStatusChangeBroadcastReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addDataScheme("package")
        }
        registerReceiverNotExported(br, filter)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        appContext = base
    }

    companion object {
        lateinit var appContext: Context
        const val USER_ACTION_REQUIRED_CHANNEL = "UserActionRequired"
        const val UPDATE_AVAILABLE_CHANNEL = "UpdateAvailable"
        const val UPDATE_FINISHED_CHANNEL = "UpdateFinished"
    }
}
