package app.accrescent.client.receivers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import app.accrescent.client.Accrescent
import app.accrescent.client.R

class AppInstallBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                if (isInForeground()) {
                    context.startActivity(confirmationIntent)
                } else {
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        confirmationIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                    )
                    val notification =
                        Notification.Builder(context, Accrescent.USER_ACTION_REQUIRED_CHANNEL)
                            .setSmallIcon(R.drawable.ic_baseline_touch_app_24)
                            .setContentTitle("User action required")
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build()

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(0, notification)
                }
            }
        }
    }

    private fun isInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
}
