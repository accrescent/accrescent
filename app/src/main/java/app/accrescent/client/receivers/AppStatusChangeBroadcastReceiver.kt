package app.accrescent.client.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.accrescent.client.data.AppInstallStatuses
import app.accrescent.client.util.getPackageInstallStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppStatusChangeBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var appInstallStatuses: AppInstallStatuses

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_FULLY_REMOVED -> {
                val appId = intent.data?.schemeSpecificPart ?: return
                val status = context.packageManager.getPackageInstallStatus(appId)
                appInstallStatuses.statuses[appId] = status
            }
        }
    }
}
