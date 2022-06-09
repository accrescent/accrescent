package app.accrescent.client.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

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
