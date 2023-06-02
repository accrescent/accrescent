package app.accrescent.client.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.accrescent.client.ui.MainActivity

object NotificationUtil {
    fun createPendingIntentForAppId(context: Context, appId: String): PendingIntent = PendingIntent.getActivity(
        context,
        appId.hashCode(),
        Intent(context, MainActivity::class.java).putExtra(Intent.EXTRA_PACKAGE_NAME, appId),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}
