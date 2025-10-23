package app.accrescent.client.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import androidx.core.content.IntentCompat

private const val LOG_TAG = "AppUninstallBroadcastReceiver"
private const val STATUS_UNSET = -999

class AppUninstallBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, STATUS_UNSET)
        if (status == STATUS_UNSET) {
            Log.e(LOG_TAG, "EXTRA_STATUS expected but not set")
            return
        }

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = IntentCompat.getParcelableExtra(
                    intent,
                    Intent.EXTRA_INTENT,
                    Intent::class.java,
                )
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ?: run {
                        Log.e(LOG_TAG, "STATUS_PENDING_USER_ACTION had no confirmation intent")
                        return
                    }

                context.startActivity(confirmationIntent)
            }

            else -> Log.w(LOG_TAG, "unhandled status $status")
        }
    }
}
