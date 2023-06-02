package app.accrescent.client.receivers

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import app.accrescent.client.Accrescent
import app.accrescent.client.R
import app.accrescent.client.data.RepoDataRepository
import app.accrescent.client.util.NotificationUtil
import app.accrescent.client.util.getParcelableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AppInstallBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repoDataRepository: RepoDataRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -999)
        val packageName = intent.getCharSequenceExtra(PackageInstaller.EXTRA_PACKAGE_NAME).toString()
        val appName = runBlocking { repoDataRepository.getApp(packageName) }?.name

        val notificationManager = context.getSystemService<NotificationManager>()!!

        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent
                    .getParcelableExtraCompat(Intent.EXTRA_INTENT, Intent::class.java)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Note we don't currently handle the case where the app is in the background and
                // the notification permission is denied.
                if (isInForeground()) {
                    context.startActivity(confirmationIntent)
                } else if (notificationManager.areNotificationsEnabled()) {
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        sessionId,
                        confirmationIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    val notification =
                        NotificationCompat.Builder(context, Accrescent.USER_ACTION_REQUIRED_CHANNEL)
                            .setSmallIcon(R.drawable.ic_baseline_touch_app_24)
                            .setContentTitle(context.getString(R.string.user_action_required))
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                    @Suppress("MissingPermission")
                    notificationManager.notify(sessionId, notification)
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                if (!context.hasNotificationPrivileges()) return

                val pendingIntent = NotificationUtil.createPendingIntentForAppId(context, packageName)
                val notification =
                    NotificationCompat.Builder(context, Accrescent.UPDATE_FINISHED_CHANNEL)
                        .setSmallIcon(R.drawable.ic_baseline_file_download_done_24)
                        .setContentTitle(context.getString(R.string.update_finished))
                        .setContentText(context.getString(R.string.update_success, appName))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

                @Suppress("MissingPermission")
                notificationManager.notify(sessionId, notification)
            }
            PackageInstaller.STATUS_FAILURE -> {
                if (!context.hasNotificationPrivileges()) return

                val pendingIntent = NotificationUtil.createPendingIntentForAppId(context, packageName)
                val notification =
                    NotificationCompat.Builder(context, Accrescent.UPDATE_FINISHED_CHANNEL)
                        .setSmallIcon(R.drawable.ic_baseline_error_outline_24)
                        .setContentTitle(context.getString(R.string.update_failure))
                        .setContentText(appName)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

                @Suppress("MissingPermission")
                notificationManager.notify(sessionId, notification)
            }
        }
    }

    private fun isInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    private fun Context.hasNotificationPrivileges(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }
}
