package app.accrescent.client.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.os.Build
import android.os.UserManager
import app.accrescent.client.R
import app.accrescent.client.di.IoDispatcher
import app.accrescent.client.receivers.AppInstallBroadcastReceiver
import app.accrescent.client.receivers.AppUninstallBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InvalidObjectException
import javax.inject.Inject

class UserRestrictionException(message: String) : Exception(message)

class PackageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apkDownloader: ApkDownloader,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun downloadAndInstall(appId: String) {
        withContext(dispatcher) {
            installApp(apkDownloader.downloadApp(appId))
        }
    }

    fun uninstallApp(appId: String) {
        // Honor UserManager restrictions in privileged mode
        val uninstallBlockedByAdmin = context
            .getSystemService(UserManager::class.java)
            .hasUserRestriction(UserManager.DISALLOW_UNINSTALL_APPS)
        if (uninstallBlockedByAdmin) {
            throw UserRestrictionException(context.getString(R.string.uninstall_blocked))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            Intent(context.applicationContext, AppUninstallBroadcastReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )
        context.packageManager.packageInstaller.uninstall(appId, pendingIntent.intentSender)
    }

    private fun installApp(apks: List<File>) {
        val um = context.getSystemService(UserManager::class.java)
        val installBlockedByAdmin = if (context.isPrivileged()) {
            // We're in privileged mode, so check that installing apps is allowed since the OS
            // package manager won't check for us.
            um.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS)
        } else {
            um.hasUserRestriction(UserManager.DISALLOW_INSTALL_APPS) ||
                    um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES) ||
                    um.hasUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
        }
        if (installBlockedByAdmin) {
            throw UserRestrictionException(context.getString(R.string.install_blocked))
        }

        val packageInstaller = context.packageManager.packageInstaller

        // We assume base.apk is always the first APK passed
        val pkgInfo = context.packageManager.getPackageArchiveInfoCompat(apks[0].absolutePath, 0)
            ?: throw InvalidObjectException(context.getString(R.string.base_apk_not_valid))

        val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
        sessionParams.setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
        sessionParams.setInstallLocation(pkgInfo.installLocation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sessionParams.setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
        }
        val sessionId = packageInstaller.createSession(sessionParams)
        val session = packageInstaller.openSession(sessionId)

        for (apk in apks) {
            val sessionStream = session.openWrite(apk.name, 0, apk.length())
            val fileStream = FileInputStream(apk)

            fileStream.copyTo(sessionStream)

            fileStream.close()
            session.fsync(sessionStream)
            sessionStream.close()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            Intent(context.applicationContext, AppInstallBroadcastReceiver::class.java),
            PendingIntent.FLAG_MUTABLE
        )
        session.commit(pendingIntent.intentSender)
        session.close()
    }
}
