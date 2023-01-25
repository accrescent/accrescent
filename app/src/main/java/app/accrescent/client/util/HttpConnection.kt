package app.accrescent.client.util

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpConnection(private val connection: HttpURLConnection) : AutoCloseable {
    fun downloadTo(out: OutputStream) = connection.inputStream.copyTo(out)

    override fun close() = connection.disconnect()
}

fun URL.openHttpConnection(): HttpConnection {
    val connection = this.openConnection() as HttpURLConnection
    connection.connect()

    return HttpConnection(connection)
}
