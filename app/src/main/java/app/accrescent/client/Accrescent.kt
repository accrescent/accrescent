package app.accrescent.client

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.accrescent.client.receivers.AppStatusChangeBroadcastReceiver
import app.accrescent.client.util.registerReceiverNotExported
import app.accrescent.client.workers.AutoUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import java.time.Duration
import javax.inject.Inject

@HiltAndroidApp
class Accrescent : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(
                USER_ACTION_REQUIRED_CHANNEL,
                getString(R.string.user_action_required),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val constraints = Constraints(
            requiredNetworkType = NetworkType.CONNECTED,
            requiresDeviceIdle = true,
            requiresStorageNotLow = true,
        )
        val updateRequest = PeriodicWorkRequestBuilder<AutoUpdateWorker>(Duration.ofHours(4))
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "UPDATE_APPS",
            ExistingPeriodicWorkPolicy.KEEP,
            updateRequest,
        )

        val br = AppStatusChangeBroadcastReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
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
    }
}
