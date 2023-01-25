package app.accrescent.client.util

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpConnection(private val connection: HttpURLConnection) : AutoCloseable {
    val inputStream: InputStream
        get() = connection.inputStream

    override fun close() = connection.disconnect()
}

fun URL.openHttpConnection(): HttpConnection {
    val connection = this.openConnection() as HttpURLConnection
    connection.connect()

    return HttpConnection(connection)
}
