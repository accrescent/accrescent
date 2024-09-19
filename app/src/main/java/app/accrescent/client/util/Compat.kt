package app.accrescent.client.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import app.accrescent.client.BuildConfig
import app.accrescent.client.data.InstallStatus

fun Context.registerReceiverNotExported(receiver: BroadcastReceiver, filter: IntentFilter) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        @Suppress("UnspecifiedRegisterReceiverFlag")
        registerReceiver(receiver, filter)
    } else {
        registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
    }
}

fun <T : Any> Intent.getParcelableExtraCompat(name: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(name)
    } else {
        this.getParcelableExtra(name, clazz)
    }
}

fun PackageManager.getInstalledPackagesCompat(flags: Int = 0): List<PackageInfo> {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        this.getInstalledPackages(flags)
    } else {
        this.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
    }
}

fun PackageManager.getPackageArchiveInfoCompat(archiveFilePath: String, flags: Int): PackageInfo? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        this.getPackageArchiveInfo(archiveFilePath, flags)
    } else {
        this.getPackageArchiveInfo(
            archiveFilePath,
            PackageManager.PackageInfoFlags.of(flags.toLong())
        )
    }
}

fun PackageManager.getPackageArchiveInfoForFd(fd: Int, flags: Int): PackageInfo? {
    return getPackageArchiveInfoCompat("/proc/self/fd/$fd", flags)
}

fun PackageManager.getPackageInstallStatus(appId: String, versionCode: Long?): InstallStatus {
    return try {
        val pkgInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            this.getPackageInfo(appId, 0)
        } else {
            this.getPackageInfo(appId, PackageManager.PackageInfoFlags.of(0))
        }
        val installerOfRecord = getInstallerOfRecord(pkgInfo.packageName)

        // This package is considered installed from another source if and only if all of the
        // following statements hold:
        //
        // 1. The installer of record is not Accrescent
        // 2. The installer of record is not null
        // 3. The package is not Accrescent
        //
        // Note that Accrescent never considers itself installed by another source. This behavior
        // ensures that Accrescent is always responsible for updating itself.
        if (
            installerOfRecord != BuildConfig.APPLICATION_ID &&
            installerOfRecord != null &&
            pkgInfo.packageName != BuildConfig.APPLICATION_ID
        ) {
            InstallStatus.INSTALLED_FROM_ANOTHER_SOURCE
        } else if (versionCode?.let { it > pkgInfo.longVersionCode } == true) {
            InstallStatus.UPDATABLE
        } else if (pkgInfo.applicationInfo?.enabled == false) {
            InstallStatus.DISABLED
        } else {
            InstallStatus.INSTALLED
        }
    } catch (e: PackageManager.NameNotFoundException) {
        InstallStatus.INSTALLABLE
    }
}

/**
 * Returns the installer of record for the given [appId]
 *
 * @return the installer of record for the given app, or null if the app was not installed by a
 * package or if the installing package itself has been uninstalled
 */
private fun PackageManager.getInstallerOfRecord(appId: String): String? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        try {
            @Suppress("DEPRECATION")
            getInstallerPackageName(appId)
        } catch (e: IllegalArgumentException) {
            null
        }
    } else {
        // getInstallSourceInfo should never throw because we hold QUERY_ALL_PACKAGES
        getInstallSourceInfo(appId).installingPackageName
    }
}
