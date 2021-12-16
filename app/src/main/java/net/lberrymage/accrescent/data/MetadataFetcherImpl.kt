package net.lberrymage.accrescent.data

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.inject.Inject
import javax.net.ssl.HttpsURLConnection

class MetadataFetcherImpl @Inject constructor() : MetadataFetcher {
    private val connection =
        URL(REPOSITORY_URL + METADATA_PATH).openConnection() as HttpsURLConnection

    override suspend fun fetchLatestMetadata(): Metadata {
        connection.connect()

        val data = connection.inputStream
        val buf = ByteArray(DEFAULT_BUFFER_SIZE)
        val outBuf = ByteArrayOutputStream()

        var bytes = data.read(buf)
        while (bytes >= 0) {
            outBuf.write(buf, 0, bytes)
            bytes = data.read(buf)
        }

        connection.disconnect()

        val json = JSONObject(outBuf.toString())

        return Metadata(json.getString("data"))
    }

    companion object {
        const val REPOSITORY_URL = "https://staging.lberrymage.net"
        const val METADATA_PATH = "/metadata.json"
    }
}
