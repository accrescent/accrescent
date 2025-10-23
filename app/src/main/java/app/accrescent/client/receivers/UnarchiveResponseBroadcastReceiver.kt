package app.accrescent.client.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.IntentCompat

private const val LOG_TAG = "UnarchiveResponseBroadcastReceiver"
private const val STATUS_UNSET = -999

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class UnarchiveResponseBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_UNARCHIVE_STATUS, STATUS_UNSET)
        if (status == STATUS_UNSET) {
            Log.e(LOG_TAG, "EXTRA_UNARCHIVE_STATUS expected but not set")
            return
        }

        when (status) {
            // All of the UNARCHIVAL_* constants are expected. Although undocumented, we also need
            // to handle STATUS_PENDING_USER_ACTION because of
            // https://issuetracker.google.com/issues/454082768.
            PackageInstaller.UNARCHIVAL_ERROR_INSTALLER_DISABLED,
            PackageInstaller.UNARCHIVAL_ERROR_INSTALLER_UNINSTALLED,
            PackageInstaller.UNARCHIVAL_ERROR_INSUFFICIENT_STORAGE,
            PackageInstaller.UNARCHIVAL_ERROR_NO_CONNECTIVITY,
            PackageInstaller.UNARCHIVAL_ERROR_USER_ACTION_NEEDED,
            PackageInstaller.UNARCHIVAL_GENERIC_ERROR,
            PackageInstaller.UNARCHIVAL_OK,
            PackageInstaller.STATUS_PENDING_USER_ACTION -> Unit

            else -> run {
                Log.e(LOG_TAG, "unexpected status code received: $status")
                return
            }
        }

        if (status != PackageInstaller.UNARCHIVAL_OK) {
            val dialogIntent = IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_INTENT,
                Intent::class.java,
            )
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ?: run {
                    Log.e(LOG_TAG, "confirmation intent expected but not set for status $status")
                    return
                }
            context.startActivity(dialogIntent)
        }
    }
}
