package app.accrescent.client.receivers

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import app.accrescent.client.workers.UnarchiveWorker

private const val TAG = "AppUnarchiveBroadcastReceiver"

@TargetApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class AppUnarchiveBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            Intent.ACTION_UNARCHIVE_PACKAGE -> {
                val packageName = intent
                    .getStringExtra(PackageInstaller.EXTRA_UNARCHIVE_PACKAGE_NAME)
                    ?: run {
                        Log.e(TAG, "No package name specified. Returning early.")
                        return
                    }
                val unarchiveId = intent.getIntExtra(PackageInstaller.EXTRA_UNARCHIVE_ID, -999)

                UnarchiveWorker.enqueue(context, packageName, unarchiveId)
            }
        }
    }
}
