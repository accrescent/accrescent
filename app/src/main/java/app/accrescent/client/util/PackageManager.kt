package app.accrescent.client.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Build
import app.accrescent.client.di.IoDispatcher
import app.accrescent.client.receivers.AppInstallBroadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InvalidObjectException
import javax.inject.Inject

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

    private fun installApp(apks: List<File>) {
        val packageInstaller = context.packageManager.packageInstaller

        // We assume base.apk is always the first APK passed
        val pkgInfo = context.packageManager.getPackageArchiveInfoCompat(apks[0].absolutePath, 0)
            ?: throw InvalidObjectException("base.apk is not a valid APK")

        val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
        sessionParams.setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
        sessionParams.setInstallLocation(pkgInfo.installLocation)
        val sessionId = packageInstaller.createSession(sessionParams)
        val session = packageInstaller.openSession(sessionId)

        for (apk in apks) {
            val sessionStream = session.openWrite(apk.name, 0, apk.length())
            val fileStream = FileInputStream(apk)
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytesRead: Int

            while (fileStream.read(buffer).also { bytesRead = it } != -1) {
                sessionStream.write(buffer, 0, bytesRead)
            }

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
