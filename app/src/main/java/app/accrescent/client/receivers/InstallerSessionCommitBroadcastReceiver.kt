package app.accrescent.client.receivers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import app.accrescent.client.Accrescent
import app.accrescent.client.R
import app.accrescent.client.data.appmanager.AppManager
import app.accrescent.client.data.appmanager.InstallAppRequest
import app.accrescent.client.data.appmanager.InstallSessionRepository
import app.accrescent.client.data.appmanager.InstallSessionResult
import app.accrescent.client.data.appmanager.InstallType
import app.accrescent.client.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

private const val STATUS_UNSET = -999
private const val LOG_TAG = "InstallerSessionCommitBroadcastReceiver"

@AndroidEntryPoint
class InstallerSessionCommitBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var installSessionRepository: InstallSessionRepository

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, STATUS_UNSET)
        if (status == STATUS_UNSET) {
            Log.e(LOG_TAG, "EXTRA_STATUS expected but not set")
            return
        }
        val bundle = intent.getBundleExtra(AppManager.EXTRA_INSTALL_APP_REQUEST_BUNDLE) ?: run {
            Log.e(LOG_TAG, "EXTRA_INSTALL_APP_REQUEST_BUNDLE expected but not set")
            return
        }
        val installRequest = InstallAppRequest.fromBundle(bundle) ?: run {
            Log.e(LOG_TAG, "received bundle $bundle, but could not parse into InstallAppRequest")
            return
        }

        when (status) {
            PackageInstaller.STATUS_FAILURE -> {
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Generic(statusMessage),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = context.getString(R.string.install_failed_notif_body_generic),
                )
            }

            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Aborted(statusMessage),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = context.getString(R.string.install_failed_notif_body_aborted),
                )
            }

            PackageInstaller.STATUS_FAILURE_BLOCKED -> {
                val blockingPackage =
                    intent.getStringExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME)
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Blocked(
                        blockingPackage = blockingPackage,
                        message = statusMessage,
                    ),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = if (blockingPackage != null) {
                        context.getString(
                            R.string.install_failed_notif_body_blocked_by_package,
                            blockingPackage,
                        )
                    } else {
                        context.getString(R.string.install_failed_notif_body_blocked)
                    }
                )
            }

            PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                val conflictingPackage =
                    intent.getStringExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME)
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Conflict(
                        conflictingPackage = conflictingPackage,
                        message = statusMessage,
                    ),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = if (conflictingPackage != null) {
                        context.getString(
                            R.string.install_failed_notif_body_conflict_with_package,
                            conflictingPackage,
                        )
                    } else {
                        context.getString(R.string.install_failed_notif_body_conflict)
                    }
                )
            }

            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Incompatible(statusMessage),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = context
                        .getString(R.string.install_failed_notif_body_app_incompatible),
                )
            }

            PackageInstaller.STATUS_FAILURE_INVALID -> {
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Invalid(statusMessage),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = context.getString(R.string.install_failed_notif_body_app_invalid),
                )
            }

            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                val storagePath = intent.getStringExtra(PackageInstaller.EXTRA_STORAGE_PATH)
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Storage(
                        storagePath = storagePath,
                        message = statusMessage,
                    ),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = context.getString(R.string.install_failed_notif_body_storage),
                )
            }

            PackageInstaller.STATUS_FAILURE_TIMEOUT -> {
                val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Failure.Timeout(statusMessage),
                )
                sendFailureNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                    errorMessage = context.getString(R.string.install_failed_notif_body_timeout),
                )
            }

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

                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.PendingUserAction,
                )
                if (isInForeground()) {
                    context.startActivity(confirmationIntent)
                } else {
                    sendPendingUserActionNotification(
                        context,
                        installRequest.appId,
                        confirmationIntent,
                    )
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                installSessionRepository.setSessionResult(
                    installRequest.sessionId,
                    InstallSessionResult.Success,
                )
                sendSuccessNotification(
                    context = context,
                    appId = installRequest.appId,
                    installType = installRequest.installType,
                )
            }

            else -> Log.e(LOG_TAG, "invalid status code received: $status")
        }
    }

    private fun isInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    private fun getAppDetailsIntent(context: Context, appId: String): PendingIntent {
        return PendingIntent.getActivity(
            context,
            appId.hashCode(),
            Intent(context, MainActivity::class.java).putExtra(Intent.EXTRA_PACKAGE_NAME, appId),
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun sendFailureNotification(
        context: Context,
        appId: String,
        installType: InstallType,
        errorMessage: String,
    ) {
        val (channelId, title) = when (installType) {
            InstallType.INSTALL -> Pair(
                Accrescent.INSTALLATION_FAILED_CHANNEL,
                context.getString(R.string.installation_failed),
            )

            InstallType.UPDATE -> Pair(
                Accrescent.UPDATE_FAILED_CHANNEL,
                context.getString(R.string.update_failed),
            )

            InstallType.UNARCHIVE -> Pair(
                Accrescent.INSTALLATION_FAILED_CHANNEL,
                context.getString(R.string.unarchive_failed),
            )
        }
        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_baseline_error_outline_24)
            .setContentTitle(title)
            .setContentText(errorMessage)
            .setContentIntent(getAppDetailsIntent(context, appId))
            .setAutoCancel(true)
            .build()

        sendNotificationForApp(context, appId, notification)
    }

    private fun sendPendingUserActionNotification(
        context: Context,
        appId: String,
        confirmationIntent: Intent,
    ) {
        val contentIntent = PendingIntent.getActivity(
            context,
            appId.hashCode(),
            confirmationIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, Accrescent.USER_ACTION_REQUIRED_CHANNEL)
            .setSmallIcon(R.drawable.ic_baseline_touch_app_24)
            .setContentTitle(context.getString(R.string.user_action_required))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        sendNotificationForApp(context, appId, notification)
    }

    private fun sendSuccessNotification(context: Context, appId: String, installType: InstallType) {
        val appName = try {
            context.packageManager.getApplicationInfo(appId, 0).loadLabel(context.packageManager)
        } catch (_: PackageManager.NameNotFoundException) {
            appId
        }
        val (channelId, title, text) = when (installType) {
            InstallType.INSTALL -> Triple(
                Accrescent.INSTALLATION_FINISHED_CHANNEL,
                context.getString(R.string.installation_finished),
                context.getString(R.string.install_success_notif_body, appName),
            )

            InstallType.UPDATE -> Triple(
                Accrescent.UPDATE_FINISHED_CHANNEL,
                context.getString(R.string.update_finished),
                context.getString(R.string.update_success, appName),
            )

            InstallType.UNARCHIVE -> Triple(
                Accrescent.INSTALLATION_FINISHED_CHANNEL,
                context.getString(R.string.unarchive_finished),
                context.getString(R.string.unarchive_success_notif_body, appName)
            )
        }
        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_baseline_file_download_done_24)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(getAppDetailsIntent(context, appId))
            .setAutoCancel(true)
            .build()

        sendNotificationForApp(context, appId, notification)
    }

    private fun sendNotificationForApp(context: Context, appId: String, notification: Notification) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(appId.hashCode(), notification)
    }
}
