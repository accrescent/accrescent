package app.accrescent.client.util

import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpConnection(private val connection: HttpURLConnection) : AutoCloseable {
    fun downloadTo(out: OutputStream) = connection.inputStream.copyTo(out)

    fun downloadTo(fd: FileDescriptor) = FileOutputStream(fd).use { this.downloadTo(it) }

    override fun close() = connection.disconnect()
}

fun URL.openHttpConnection(): HttpConnection {
    val connection = this.openConnection() as HttpURLConnection
    connection.connect()

    return HttpConnection(connection)
}
