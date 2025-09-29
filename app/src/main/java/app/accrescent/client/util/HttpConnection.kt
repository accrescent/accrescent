package app.accrescent.client.util

import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpConnection(private val connection: HttpURLConnection) : AutoCloseable {
    fun downloadTo(out: OutputStream, onProgressUpdate: (Long) -> Unit = {}) {
        connection.inputStream.copyTo(out, onProgressUpdate)
    }

    fun downloadTo(fd: FileDescriptor, onProgressUpdate: (Long) -> Unit = {}) {
        FileOutputStream(fd).use { this.downloadTo(it, onProgressUpdate) }
    }

    override fun close() = connection.disconnect()
}

fun URL.openHttpConnection(): HttpConnection {
    val connection = this.openConnection() as HttpURLConnection
    connection.connect()

    return HttpConnection(connection)
}
