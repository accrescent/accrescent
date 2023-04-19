package app.accrescent.client.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_NOT_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import app.accrescent.client.data.InstallStatus

fun Context.registerReceiverNotExported(receiver: BroadcastReceiver, filter: IntentFilter) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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
        @Suppress("DEPRECATION")
        this.getInstalledPackages(flags)
    } else {
        this.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
    }
}

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

fun PackageManager.getPackageArchiveInfoForFd(fd: Int, flags: Int): PackageInfo? {
    return getPackageArchiveInfoCompat("/proc/self/fd/$fd", flags)
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
