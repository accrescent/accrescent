package app.accrescent.client.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import java.io.InputStream
import java.io.OutputStream

fun Context.isPrivileged(): Boolean {
    return this.checkSelfPermission(Manifest.permission.INSTALL_PACKAGES) ==
            PackageManager.PERMISSION_GRANTED
}

fun InputStream.copyTo(
    out: OutputStream,
    onProgressUpdate: (Long) -> Unit,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    onProgressUpdate(bytes.toLong())

    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
        onProgressUpdate(bytes.toLong())
    }

    return bytesCopied
}
