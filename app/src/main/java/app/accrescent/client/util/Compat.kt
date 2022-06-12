package app.accrescent.client.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import app.accrescent.client.data.InstallStatus

fun PackageManager.getPackageArchiveInfoCompat(archiveFilePath: String, flags: Int): PackageInfo? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        @Suppress("DEPRECATION")
        this.getPackageArchiveInfo(archiveFilePath, flags)
    } else {
        this.getPackageArchiveInfo(
            archiveFilePath,
            PackageManager.PackageInfoFlags.of(flags.toLong())
        )
    }
}

fun PackageManager.getPackageInstallStatus(appId: String, versionCode: Long): InstallStatus {
    return try {
        val pkgInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            this.getPackageInfo(appId, 0)
        } else {
            this.getPackageInfo(appId, PackageManager.PackageInfoFlags.of(0))
        }
        if (versionCode > pkgInfo.longVersionCode) {
            InstallStatus.UPDATABLE
        } else {
            InstallStatus.INSTALLED
        }
    } catch (e: PackageManager.NameNotFoundException) {
        InstallStatus.INSTALLABLE
    }
}
