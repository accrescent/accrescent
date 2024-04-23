// See LICENSE.grapheneos for additional copyright information for this file.

package app.accrescent.client.util

import android.os.ParcelFileDescriptor
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants.O_CREAT
import android.system.OsConstants.O_RDWR
import android.system.OsConstants.O_TRUNC
import android.system.OsConstants.SEEK_SET
import android.system.OsConstants.S_IRUSR
import android.system.OsConstants.S_IWUSR
import android.util.Log
import app.accrescent.client.Accrescent
import java.io.File
import java.io.FileDescriptor

private const val TAG = "TemporaryFile"
private val fileForTemporaryDescriptor = File(Accrescent.appContext.cacheDir, "tmp_fd")

// A file descriptor not backed by any specific file. This allows for automatically and reliably
// discarding the contents when the descriptor is closed, including when the app process dies
// unexpectedly.
class TemporaryFile(val descriptor: FileDescriptor) : AutoCloseable {
    // There's no non-deprecated way to get the raw fd from FileDescriptor, but there is from
    // ParcelFileDescriptor for some reason, so duplicate it.
    fun getFd() = ParcelFileDescriptor.dup(descriptor).fd

    fun seekToStart() {
        check(Os.lseek(descriptor, 0, SEEK_SET) == 0L)
    }

    override fun close() {
        try {
            Os.close(descriptor)
        } catch (e: ErrnoException) {
            Log.d(TAG, "", e)
        }
    }
}

fun newTemporaryFile(): TemporaryFile {
    val fd: FileDescriptor
    val flags = O_CREAT or O_RDWR or O_TRUNC
    val mode = S_IRUSR or S_IWUSR

    val file = fileForTemporaryDescriptor

    synchronized(file) {
        fd = Os.open(file.path, flags, mode)
        // There's no wrapper for unlink()
        if (!file.delete()) {
            Os.close(fd)
        }
    }

    return TemporaryFile(fd)
}
