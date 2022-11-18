package app.accrescent.client.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

fun Context.isPrivileged(): Boolean {
    return this.checkSelfPermission(Manifest.permission.INSTALL_PACKAGES) ==
            PackageManager.PERMISSION_GRANTED
}
